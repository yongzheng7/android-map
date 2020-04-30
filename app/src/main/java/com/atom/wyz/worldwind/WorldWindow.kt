package com.atom.wyz.worldwind

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.PointF
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
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.globe.GlobeWgs84
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.pick.PickedObjectList
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

    var navigator = Navigator()

    var fieldOfView = 45.0
        get() = field
        set(value) {
            require(!(value <= 0 || value >= 180)) {
                Logger.logMessage(
                    Logger.ERROR,
                    "WorldWindow",
                    "setFieldOfView",
                    "invalidFieldOfView"
                )
            }
            field = value
        }

    var navigatorEvents = NavigatorEventSupport(this)

    var frameController: FrameController = BasicFrameController()
    var frameMetrics = FrameMetrics()

    var worldWindowController: WorldWindowController = BasicWorldWindowController()
        set(value) {
            field.worldWindow = (null)
            field = value
            field.worldWindow = (this)
        }

    var viewport = Viewport()

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

        navigator.latitude = initLocation.latitude
        navigator.longitude = initLocation.longitude
        navigator.altitude = initAltitude

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
        // Give the World Window's controller an opportunity to handle the event
        try {
            if (worldWindowController.onTouchEvent(event)) {
                navigatorEvents.onTouchEvent(event)
            }
        } catch (e: Exception) {
            Logger.logMessage(
                Logger.ERROR, "WorldWindow", "onTouchEvent",
                "Exception while handling touch event \'$event\'", e
            )
        }
        return true
    }

    override fun onDrawFrame(gl: GL10?) {

        var nextFrame: Frame?
        while (frameQueue.poll().also { nextFrame = it } != null && nextFrame!!.pickMode) {
            nextFrame?.let {
                drawFrame(it)
                it.signalDone()
                it.recycle()
            }
        }

        if (nextFrame != null) {
            currentFrame?.recycle()
            currentFrame = nextFrame
            super.requestRender()
        }

        currentFrame?.let {
            drawFrame(it)
            it.signalDone()
        }
    }

    protected fun computeViewingTransform(
        projection: Matrix4,
        modelview: Matrix4?
    ) {
        val near = navigator.altitude * 0.75
        val far = globe.horizonDistance(navigator.altitude, 160000.0)
        viewport.set(this.viewport)
        projection.setToPerspectiveProjection(
            this.viewport.width.toDouble(),
            this.viewport.height.toDouble(),
            fieldOfView,
            near,
            far
        )
        navigator.getAsCamera(globe, scratchCamera)
        globe.cameraToCartesianTransform(scratchCamera, modelview)!!.invertOrthonormal()
    }

    protected fun renderFrame(frame: Frame) {

        val pickMode = frame.pickMode
        if (!pickMode) {
            frameMetrics.beginRendering()
        }

        rc.globe = globe
        rc.layers = layers
        rc.verticalExaggeration = verticalExaggeration
        rc.fieldOfView = fieldOfView
        rc.horizonDistance = globe.horizonDistance(this.navigator.altitude)
        rc.viewport.set(viewport)
        rc.camera = this.navigator.getAsCamera(this.globe, this.rc.camera);
        rc.cameraPoint = globe.geographicToCartesian(
            rc.camera.latitude,
            rc.camera.longitude,
            rc.camera.altitude,
            rc.cameraPoint
        )
        rc.renderResourceCache = this.renderResourceCache
        rc.renderResourceCache?.resources = (this.context.resources)
        rc.resources = this.context.resources

        frame.viewport.set(this.viewport)
        computeViewingTransform(frame.projection, frame.modelview)
        rc.viewport.set(frame.viewport)
        rc.projection.set(frame.projection)
        rc.modelview.set(frame.modelview)
        rc.modelviewProjection.setToMultiply(frame.projection, frame.modelview)
        rc.frustum.setToProjectionMatrix(frame.projection)
        rc.frustum.transformByMatrix(scratchModelview.transposeMatrix(frame.modelview))
        rc.frustum.normalize()

        rc.drawableQueue = frame.drawableQueue
        rc.drawableTerrain = frame.drawableTerrain
        rc.pickedObjects = frame.pickedObjects
        rc.pickPoint = frame.pickPoint;
        rc.pickRay = frame.pickRay;
        rc.pickMode = frame.pickMode

        frameController.renderFrame(rc)

        frameQueue.offer(frame)
        super.requestRender()

        if (!pickMode && rc.redrawRequested) {
            requestRedraw()
        }

        if (!pickMode) {
            navigatorEvents.onFrameRendered(rc)
        }

        rc.reset()

        // Mark the end of a frame render.
        if (!pickMode) {
            frameMetrics.endRendering()
        }

    }


    protected fun drawFrame(frame: Frame) {
        val pickMode = frame.pickMode
        if (!pickMode) {
            frameMetrics.beginDrawing()
        }
        frame.modelview.extractEyePoint(dc.eyePoint)
        dc.projection.set(frame.projection)
        dc.modelview.set(frame.modelview)
        dc.modelviewProjection.setToMultiply(frame.projection, frame.modelview)
        dc.screenProjection.setToScreenProjection(
            frame.viewport.width.toDouble(),
            frame.viewport.height.toDouble()
        )

        dc.drawableQueue = frame.drawableQueue
        dc.drawableTerrain = frame.drawableTerrain

        dc.pickedObjects = frame.pickedObjects
        dc.pickPoint = frame.pickPoint
        dc.pickMode = frame.pickMode

        frameController.drawFrame(dc)

        if (!pickMode) {
            renderResourceCache?.releaseEvictedResources(dc)
        }

        dc.reset()

        if (!pickMode) {
            frameMetrics.endDrawing()
        }
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

        GLES20.glDisable(GLES20.GL_DITHER)
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
        // 视角一半的tan
        val tanfovy_2 = Math.tan(Math.toRadians(fieldOfView * 0.5))

        val frustumHeight = 2 * distance * tanfovy_2
        return frustumHeight / this.height
    }

    /**
     * 返回使地球范围在此世界窗口中可见所需的距地球表面的最小距离。
     */
    fun distanceToViewGlobeExtents(): Double {
        val sinfovy_2 = Math.sin(Math.toRadians(fieldOfView * 0.5))

        val radius: Double = globe.equatorialRadius
        return radius / sinfovy_2 - radius
    }

    override fun onMessage(name: String?, sender: Any?, userProperties: Map<Any?, Any?>?) {
        if (name == WorldWind.REQUEST_REDRAW) {
            requestRender() // inherited from GLSurfaceView; may be called on any thread
        }
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

    fun requestRedraw() {
        if (Thread.currentThread() !== Looper.getMainLooper().thread) {
            mainLoopHandler.sendEmptyMessage(0);
            return
        }
        if (!waitingForRedraw && !this.viewport.isEmpty()) {
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
        renderFrame(frame)
    }

    protected fun reset() {
        navigatorEvents.reset()
        renderResourceCache?.clear()
        viewport.setEmpty()
        clearFrameQueue()
        Choreographer.getInstance().removeFrameCallback(this)
        this.mainLoopHandler.removeMessages(0 /*what*/)
        waitingForRedraw = false
    }

    protected fun clearFrameQueue() {
        var frame: Frame?
        while (frameQueue.poll().also { frame = it } != null) {
            frame?.signalDone()
            frame?.recycle()
        }

        frameQueue.clear()
        // Recycle the current frame back into the frame pool. Mark the frame as done to ensure that threads waiting for
        // the frame to finish don't deadlock.
        currentFrame?.signalDone()
        currentFrame?.recycle()
        currentFrame = null
    }

    fun pick(
        x: Float,
        y: Float
    ): PickedObjectList {
        val pickedObjects = PickedObjectList()
        val pickPoint = Vec2(x.toDouble(), (this.height - y).toDouble())

        // Nothing can be picked if the pick point is outside of the World Window's viewport.
        if (!viewport.contains(pickPoint.x.toInt(), pickPoint.y.toInt())) {
            return pickedObjects
        }
        // Nothing can be picked if a ray through the pick point cannot be constructed.
        val pickRay = Line()
        if (!rayThroughScreenPoint(x, y, pickRay)) {
            return pickedObjects
        }
        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        val frame = Frame.obtain(framePool)
        frame.pickedObjects = pickedObjects
        frame.pickPoint = pickPoint
        frame.pickRay = pickRay
        frame.pickMode = true
        renderFrame(frame)
        // Wait until the OpenGL thread is done processing the frame and resolving the picked objects.
        frame.awaitDone()
        return pickedObjects
    }

    private val scratchCamera = Camera()
    private val scratchModelview = Matrix4()
    private val scratchProjection = Matrix4()
    private val scratchPoint: Vec3 = Vec3()


    fun cartesianToScreenPoint(
        x: Double,
        y: Double,
        z: Double,
        result: PointF?
    ): Boolean {
        requireNotNull(result) {
            Logger.logMessage(
                Logger.ERROR,
                "WorldWindow",
                "cartesianToScreenPoint",
                "missingResult"
            )
        }
        this.computeViewingTransform(this.scratchProjection, this.scratchModelview)
        scratchProjection.multiplyByMatrix(scratchModelview)
        val m = scratchProjection.m
        var sx = m[0] * x + m[1] * y + m[2] * z + m[3]
        var sy = m[4] * x + m[5] * y + m[6] * z + m[7]
        var sz = m[8] * x + m[9] * y + m[10] * z + m[11]
        val sw = m[12] * x + m[13] * y + m[14] * z + m[15]
        if (sw == 0.0) {
            return false
        }
        sx /= sw
        sy /= sw
        sz /= sw
        if (sz < -1 || sz > 1) {
            return false
        }
        sx = sx * 0.5 + 0.5
        sy = sy * 0.5 + 0.5
        sy = 1 - sy
        sx = sx * this.width
        sy = sy * this.height
        result.x = sx.toFloat()
        result.y = sy.toFloat()
        return true
    }

    /**
     * 经纬度转屏幕点位
     */
    fun geographicToScreenPoint(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        result: PointF?
    ): Boolean {
        requireNotNull(result) {
            Logger.logMessage(
                Logger.ERROR,
                "WorldWindow",
                "geographicToScreenPoint",
                "missingResult"
            )
        }
        globe.geographicToCartesian(latitude, longitude, altitude, scratchPoint)
        return cartesianToScreenPoint(scratchPoint.x, scratchPoint.y, scratchPoint.z, result)
    }

    /**
     * 根据屏幕的点获取一条射线
     */
    fun rayThroughScreenPoint(
        x: Float,
        y: Float,
        result: Line?
    ): Boolean {
        requireNotNull(result) {
            Logger.logMessage(
                Logger.ERROR,
                "WorldWindow",
                "rayThroughScreenPoint",
                "missingResult"
            )
        }
        this.computeViewingTransform(this.scratchProjection, this.scratchModelview)
        scratchProjection.multiplyByMatrix(scratchModelview).invert()
        var sx = x / this.width
        var sy = y / this.height
        sy = 1 - sy
        sx = sx * 2 - 1
        sy = sy * 2 - 1
        val m = scratchProjection.m
        val mx = m[0] * sx + m[1] * sy + m[3]
        val my = m[4] * sx + m[5] * sy + m[7]
        val mz = m[8] * sx + m[9] * sy + m[11]
        val mw = m[12] * sx + m[13] * sy + m[15]

        var nx = mx - m[2]
        var ny = my - m[6]
        var nz = mz - m[10]
        val nw = mw - m[14]

        var fx = mx + m[2]
        var fy = my + m[6]
        var fz = mz + m[10]
        val fw = mw + m[14]
        if (nw == 0.0 || fw == 0.0) {
            return false
        }
        nx = nx / nw
        ny = ny / nw
        nz = nz / nw

        fx = fx / fw
        fy = fy / fw
        fz = fz / fw
        result.origin.set(nx, ny, nz)
        result.direction.set(fx - nx, fy - ny, fz - nz).normalize()
        return true
    }


}