package com.atom.wyz.worldwind

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.gesture.GestureGroup
import com.atom.wyz.worldwind.gesture.GestureRecognizer
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.globe.GlobeWgs84
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.render.*
import com.atom.wyz.worldwind.util.RenderResourceCache
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.MessageListener
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class WorldWindow : GLSurfaceView, GLSurfaceView.Renderer, MessageListener {

    val DEFAULT_MEMORY_CLASS = 16

    var globe: Globe = GlobeWgs84()

    var layers: LayerList = LayerList()

    var verticalExaggeration: Double = 1.0

    var navigator: Navigator = BasicNavigator()

    var frameController: FrameController = BasicFrameController(FrameStatistics())

    var worldWindowController: WorldWindowController = BasicWorldWindowController()
        set(value) {
            field.worldWindow = (null)
            field = value
            field.worldWindow = (this)
        }

    var gestureGroup: GestureGroup = GestureGroup()

    var viewport = Rect()

    var gpuObjectCache: RenderResourceCache? = null

    var dc: DrawContext = DrawContext()

    var surfaceTileRenderer: SurfaceTileRenderer = BasicSurfaceTileRenderer()


    constructor(context: Context) : super(context) {
        this.init(null)
    }

    constructor(context: Context, configChooser: EGLConfigChooser) : super(context) {
        this.init(configChooser)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.init(null)
    }

    private fun init(configChooser: EGLConfigChooser?) {
        val initLocation: Location = Location.fromTimeZone(TimeZone.getDefault())
        val initAltitude: Double = this.distanceToViewGlobeExtents() * 1.1

        navigator.setLatitude(initLocation.latitude)
        navigator.setLongitude(initLocation.longitude)
        navigator.setAltitude(initAltitude)

        this.worldWindowController.worldWindow = this

        val am = this.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryClass = am.memoryClass as Int? ?: DEFAULT_MEMORY_CLASS // default to 16 MB class
        val gpuCacheSize = memoryClass / 2 * 1024 * 1024
        gpuObjectCache = RenderResourceCache(gpuCacheSize)

        this.setEGLConfigChooser(configChooser)
        this.setEGLContextClientVersion(2) // must be called before setRenderer
        this.setRenderer(this)
        this.renderMode = RENDERMODE_WHEN_DIRTY // must be called after setRenderer

        WorldWind.messageService.addListener(this)
    }

    fun getFrameStatistics(): FrameStatistics {
        return frameController.frameStatistics
    }

    protected fun prepareToDrawFrame() {
        this.dc.resources = this.context.resources
        this.dc.globe = globe
        this.dc.layers = layers
        this.dc.verticalExaggeration = verticalExaggeration
        this.dc.horizonDistance = globe.horizonDistance(dc.eyePosition.altitude)

        this.dc.eyePosition.set(this.navigator.getLatitude(), this.navigator.getLongitude(), this.navigator.getAltitude())
        this.dc.heading = this.navigator.getHeading()
        this.dc.tilt = this.navigator.getTilt()
        this.dc.roll = this.navigator.getRoll()
        this.dc.fieldOfView = this.navigator.getFieldOfView()
        this.dc.viewport = viewport
        this.dc.renderResourceCache = gpuObjectCache
        this.dc.renderResourceCache?.resources = this.context.resources

        this.dc.surfaceTileRenderer = surfaceTileRenderer
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event) || this.gestureGroup.onTouchEvent(event)
    }

    override fun onDrawFrame(gl: GL10?) {

        prepareToDrawFrame() // 准备数据

        frameController.drawFrame(dc) //绘制帧

        gpuObjectCache?.releaseEvictedResources(dc)

        if (dc.renderRequested) {
            requestRender()
        }

        dc.resetFrameProperties()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        this.viewport.set(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND)
        // 背面裁切
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        // 深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        //在世界窗口渲染期间默认情况下启用GL顶点属性数组0
        //Enabled GL vertex attrib array 0 by default during World Window rendering
        GLES20.glEnableVertexAttribArray(0)
        // 混合因子
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 深度小或相等的时候也渲染 （GL_LESS = 深度小的时候才渲染）
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        this.dc.contextLost()
        this.gpuObjectCache?.contextLost(this.dc);
    }

    /**
     * distance = 视角的高度
     * distance * 视角的一半的tan *2 == 该高度下视角的可使范围
     * 可视范围的高度和 此时视窗的高度之比
     */
    fun pixelSizeAtDistance(distance: Double): Double {
        val fovyDegrees: Double = navigator.getFieldOfView()
        // 视角一半的tan
        val tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5))

        val frustumHeight = 2 * distance * tanfovy_2
        return frustumHeight / this.height
    }

    /**
     * 返回使地球范围在此世界窗口中可见所需的距地球表面的最小距离。
     *
     */
    fun distanceToViewGlobeExtents(): Double {
        val fovyDegrees: Double = navigator.getFieldOfView()
        val sinfovy_2 = Math.sin(Math.toRadians(fovyDegrees * 0.5))

        val radius: Double = globe.equatorialRadius
        return radius / sinfovy_2 - radius
    }

    override fun onMessage(name: String?, sender: Any?, userProperties: Map<Any?, Any?>?) {
        if (name == WorldWind.REQUEST_RENDER) {
            requestRender() // inherited from GLSurfaceView; may be called on any thread
        }
    }

    fun addGestureRecognizer(recognizer: GestureRecognizer?) {
        if (recognizer == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WorldWindow", "addGestureRecognizer", "missingRecognizer"))
        }
        gestureGroup.addRecognizer(recognizer)
    }

    fun removeGestureRecognizer(recognizer: GestureRecognizer?) {
        if (recognizer == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WorldWindow", "removeGestureRecognizer", "missingRecognizer"))
        }
        gestureGroup.removeRecognizer(recognizer)
    }

    fun getGestureRecognizers(): List<GestureRecognizer?>? {
        return gestureGroup.getRecognizers()
    }
}