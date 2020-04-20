package com.atom.wyz.worldwind

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.atom.wyz.worldwind.frame.BasicFrameController
import com.atom.wyz.worldwind.frame.Frame
import com.atom.wyz.worldwind.frame.FrameController
import com.atom.wyz.worldwind.frame.FrameMetrics
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.gesture.GestureGroup
import com.atom.wyz.worldwind.gesture.GestureRecognizer
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.globe.GlobeWgs84
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.MessageListener
import com.atom.wyz.worldwind.util.RenderResourceCache
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.SynchronizedPool
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class WorldWindow : GLSurfaceView, GLSurfaceView.Renderer, MessageListener, FrameCallback {

    companion object {
        const val DEFAULT_MEMORY_CLASS = 16

        const val MAX_FRAME_QUEUE_SIZE = 2
    }

    var globe: Globe = GlobeWgs84()

    var layers: LayerList = LayerList()

    var verticalExaggeration: Double = 1.0

    var navigator: Navigator = BasicNavigator()


     var navigatorEvents = NavigatorEventSupport(this)

    var currentMvpMatrix: Matrix4? = null

    var frameController: FrameController = BasicFrameController()
    var frameMetrics = FrameMetrics()

    var worldWindowController: WorldWindowController = BasicWorldWindowController()
        set(value) {
            field.worldWindow = (null)
            field = value
            field.worldWindow = (this)
        }

    var gestureGroup: GestureGroup = GestureGroup()

    var viewport = Rect()

    var renderResourceCache: RenderResourceCache? = null

    var rc = RenderContext()
    var dc = DrawContext()

    protected var framePool: Pool<Frame> = SynchronizedPool()

    protected var frameQueue =
        ConcurrentLinkedQueue<Frame>()

    protected var currentFrame: Frame? = null

    protected var waitingForRedraw = false

    protected var mainLoopHandler =
        Handler(Looper.getMainLooper(), Handler.Callback {
            requestRedraw()
            false

        })

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
        renderResourceCache = RenderResourceCache(gpuCacheSize)

        this.setEGLConfigChooser(configChooser)
        this.setEGLContextClientVersion(2) // must be called before setRenderer
        this.setRenderer(this)
        this.renderMode = RENDERMODE_WHEN_DIRTY // must be called after setRenderer

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        super.surfaceCreated(holder)
        WorldWind.messageService.addListener(this)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        super.surfaceDestroyed(holder)
        WorldWind.messageService.removeListener(this)
        // Reset this WorldWindow's internal state.
        reset()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, w: Int, h: Int) {
        super.surfaceChanged(holder, format, w, h)
        viewport.set(0, 0, w, h)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (super.onTouchEvent(event)) {
            return true
        }

        // Let the WorldWindow's gestures handle the event.
        if (gestureGroup.onTouchEvent(event)) {
            navigatorEvents.onTouchEvent(event)
        }
        return true
    }

    override fun onDrawFrame(gl: GL10?) {
        val nextFrame = frameQueue.poll()
        if (nextFrame != null) {
            currentFrame?.recycle()
            currentFrame = nextFrame

            super.requestRender()
        }
        // Process and display the Drawables accumulated during the render phase.
        if (currentFrame != null) {
            beforeDrawFrame()
            drawFrame(currentFrame!!)
            afterDrawFrame()
        }

        // Continue processing the frame queue on the OpenGL thread until the queue is empty.
        if (!frameQueue.isEmpty()) {
            super.requestRender()
        }
    }

    protected fun renderFrame(frame: Frame) {

        rc.globe = globe
        rc.layers.addAllLayers(layers)
        rc.verticalExaggeration = verticalExaggeration
        rc.eyePosition.set(
            this.navigator.getLatitude(),
            this.navigator.getLongitude(),
            this.navigator.getAltitude()
        )
        rc.heading = navigator.getHeading()
        rc.tilt = navigator.getTilt()
        rc.roll = navigator.getRoll()
        rc.fieldOfView = navigator.getFieldOfView()
        rc.horizonDistance = globe.horizonDistance(this.navigator.getAltitude())
        rc.viewport.set(viewport)
        rc.renderResourceCache = this.renderResourceCache
        rc.renderResourceCache?.resources = (this.context.resources)
        rc.resources = this.context.resources
        rc.drawableQueue = frame.drawableQueue
        rc.drawableTerrain = frame.drawableTerrain

        frameController.renderFrame(rc)

        // Assign the frame's Cartesian modelview matrix and eye coordinate projection matrix.
        frame.viewport.set(viewport)
        frame.modelview.set(rc.modelview)
        frame.projection.set(rc.projection)
    }

    private fun afterRenderFrame() {
        if (rc.redrawRequested) {
            requestRender() // inherited from GLSurfaceView
        }

        navigatorEvents.onFrameRendered(rc)
        rc.reset()
        //绘制数据
        frameMetrics.endRendering()
    }

    private fun beforeRenderFrame() {
        frameMetrics.beginRendering()
    }

    protected fun drawFrame(frame: Frame) {
        dc.modelview.set(frame.modelview)
        dc.projection.set(frame.projection)
        dc.modelview.extractEyePoint(dc.eyePoint)
        dc.modelviewProjection.setToMultiply(frame.projection, frame.modelview)
        dc.screenProjection.setToScreenProjection(
            frame.viewport.width().toDouble(),
            frame.viewport.height().toDouble()
        )
        dc.drawableQueue = frame.drawableQueue
        dc.drawableTerrain = frame.drawableTerrain
        frameController.drawFrame(dc)
    }

    protected fun beforeDrawFrame() {
        frameMetrics.beginDrawing()
    }

    protected fun afterDrawFrame() {
        renderResourceCache!!.releaseEvictedResources(dc)
        dc.reset()
        frameMetrics.endDrawing()
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        requestRedraw()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND)
        // 背面裁切
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        // 深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // 混合因子
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 深度小或相等的时候也渲染 （GL_LESS = 深度小的时候才渲染）
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        //在世界窗口渲染期间默认情况下启用GL顶点属性数组0
        GLES20.glEnableVertexAttribArray(0)

        this.dc.contextLost()

    }

    /**
     * distance = 视角的高度
     * 获取到可视的水平面长度和手机屏幕的比例
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
     */
    fun distanceToViewGlobeExtents(): Double {
        val fovyDegrees: Double = navigator.getFieldOfView()
        val sinfovy_2 = Math.sin(Math.toRadians(fovyDegrees * 0.5))

        val radius: Double = globe.equatorialRadius
        return radius / sinfovy_2 - radius
    }

    override fun onMessage(name: String?, sender: Any?, userProperties: Map<Any?, Any?>?) {
        if (name == WorldWind.REQUEST_REDRAW) {
            requestRender() // inherited from GLSurfaceView; may be called on any thread
        }
    }

    fun addGestureRecognizer(recognizer: GestureRecognizer?) {
        if (recognizer == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "addGestureRecognizer", "missingRecognizer")
            )
        }
        gestureGroup.addRecognizer(recognizer)
    }

    fun removeGestureRecognizer(recognizer: GestureRecognizer?) {
        if (recognizer == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "removeGestureRecognizer", "missingRecognizer")
            )
        }
        gestureGroup.removeRecognizer(recognizer)
    }

    fun getGestureRecognizers(): List<GestureRecognizer?>? {
        return gestureGroup.getRecognizers()
    }

    fun addNavigatorListener(listener: NavigatorListener?) {
        requireNotNull(listener) {
            Logger.logMessage(
                Logger.ERROR,
                "WorldWindow",
                "addNavigatorListener",
                "missingListener"
            )
        }
        navigatorEvents.addNavigatorListener(listener)
    }

    fun removeNavigatorListener(listener: NavigatorListener?) {
        requireNotNull(listener) {
            Logger.logMessage(
                Logger.ERROR,
                "WorldWindow",
                "removeNavigatorListener",
                "missingListener"
            )
        }
        navigatorEvents.addNavigatorListener(listener)
    }

    fun getNavigatorStoppedDelay(): Long {
        return navigatorEvents.stoppedEventDelay
    }

    fun setNavigatorStoppedDelay(delay: Long, unit: TimeUnit?) {
        navigatorEvents.setNavigatorStoppedDelay(delay, unit!!)
    }

    fun requestRedraw() { // Forward calls to requestRedraw to the main thread.
        if (Thread.currentThread() !== Looper.getMainLooper().thread) {
            mainLoopHandler.sendEmptyMessage(0);
            return
        }
        // Suppress duplicate requests for redraw.
        if (!waitingForRedraw &&  !this.viewport.isEmpty()) {
            Choreographer.getInstance().postFrameCallback(this)
            waitingForRedraw = true
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (frameQueue.size >= MAX_FRAME_QUEUE_SIZE) {
            Choreographer.getInstance().postFrameCallback(this)
            return
        }
        waitingForRedraw = false

        val frame = Frame.obtain(framePool)
        this.beforeRenderFrame()
        renderFrame(frame)
        frameQueue.offer(frame)
        super.requestRender()
        this.afterRenderFrame()

    }
    /**
     * Resets this WorldWindow to its initial internal state.
     */
    protected fun reset() { // Reset any state associated with navigator events.
        navigatorEvents.reset()
        // Clear the render resource cache; it's entries are now invalid.
        renderResourceCache?.clear()
        // Clear the viewport dimensions.
        viewport.setEmpty()
        // Clear the frame queue and recycle pending frames back into the frame pool.
        clearFrameQueue()
        // Cancel any outstanding request redraw messages.
        Choreographer.getInstance().removeFrameCallback(this)
        this.mainLoopHandler.removeMessages(0 /*what*/)
        waitingForRedraw = false
    }

    protected fun clearFrameQueue() { // Clear the frame queue and recycle pending frames back into the frame pool.
        var frame = frameQueue.poll()
        while (frame != null) {
            frame.recycle()
            frame = frameQueue.poll()
        }
        frameQueue.clear()
        // Recycle the current frame back into the frame pool.
        if (currentFrame != null) {
            currentFrame!!.recycle()
            currentFrame = null
        }
    }
}