package com.atom.wyz.worldwind

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.os.Message
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

        const val MSG_ID_CLEAR_CACHE = 1

        const val MSG_ID_REQUEST_REDRAW = 2

        const val MSG_ID_SET_VIEWPORT = 3
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

    protected var frameQueue: Queue<Frame> = ConcurrentLinkedQueue()

    protected var pickQueue: Queue<Frame> = ConcurrentLinkedQueue()

    protected var currentFrame: Frame? = null

    protected var isPaused = false
    protected var isWaitingForRedraw = false

    protected var mainLoopHandler =
        Handler(Looper.getMainLooper(), Handler.Callback {
            if (it.what == MSG_ID_CLEAR_CACHE) {
                renderResourceCache!!.clear()
            } else if (it.what == MSG_ID_REQUEST_REDRAW) {
                requestRedraw()
            } else if (it.what == MSG_ID_SET_VIEWPORT) {
                viewport.set((it.obj as Viewport))
            }
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

        val pickFrame = pickQueue.poll()
        if (pickFrame != null) {
            drawFrame(pickFrame)
            pickFrame.signalDone()
            pickFrame.recycle()
            super.requestRender()
        }

        var nextFrame = frameQueue.poll()
        if (nextFrame != null) {
            currentFrame?.recycle()
            currentFrame = nextFrame
            super.requestRender()
        }

        currentFrame?.let {
            drawFrame(it)
        }
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        // Reset the World Window's internal state. The OpenGL thread is paused, so frames in the queue will not be
        // processed. Clear the frame queue and recycle pending frames back into the frame pool. We also don't know
        // whether or not the render resources are valid, so we reset and let the GLSurfaceView establish the new
        // EGL context and viewport.
        reset()
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
        if (pickMode) {
            rc.frustum.setToModelviewProjection(frame.projection, frame.modelview, frame.viewport, frame.pickViewport)
        } else {
            rc.frustum.setToModelviewProjection(frame.projection, frame.modelview, frame.viewport)
        }

        rc.drawableQueue = frame.drawableQueue
        rc.drawableTerrain = frame.drawableTerrain
        rc.pickedObjects = frame.pickedObjects
        rc.pickPoint = frame.pickPoint;
        rc.pickRay = frame.pickRay;
        rc.pickMode = frame.pickMode

        frameController.renderFrame(rc)

        if (pickMode) {
            pickQueue.offer(frame)
            super.requestRender()
        } else {
            frameQueue.offer(frame)
            super.requestRender()
        }
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
        // Set the World Window's new viewport dimensions.
        val newViewport = Viewport(0, 0, width, height)
        this.mainLoopHandler.sendMessage(
            Message.obtain(this.mainLoopHandler, MSG_ID_SET_VIEWPORT /*msg.what*/, newViewport /*msg.obj*/)
        )
        // Redraw this World Window with the new viewport.
        this.mainLoopHandler.sendEmptyMessage(MSG_ID_REQUEST_REDRAW /*msg.what*/)
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
        // Clear the render resource cache on the main thread.
        this.mainLoopHandler.sendEmptyMessage(MSG_ID_CLEAR_CACHE /*msg.what*/)

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
            mainLoopHandler.sendEmptyMessage(MSG_ID_REQUEST_REDRAW /*what*/);
            return
        }
        if (!this.isWaitingForRedraw && !this.isPaused && !this.viewport.isEmpty()) {
            Choreographer.getInstance().postFrameCallback(this)
            isWaitingForRedraw = true
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (frameQueue.size >= MAX_FRAME_QUEUE_SIZE) {
            Choreographer.getInstance().postFrameCallback(this)
            return
        }
        isWaitingForRedraw = false

        val frame = Frame.obtain(framePool)
        renderFrame(frame)
    }

    protected fun reset() {
        navigatorEvents.reset()
        renderResourceCache?.clear()
        viewport.setEmpty()
        clearFrameQueue()
        Choreographer.getInstance().removeFrameCallback(this)
        this.mainLoopHandler.removeMessages(MSG_ID_REQUEST_REDRAW)
        isWaitingForRedraw = false
    }

    protected fun clearFrameQueue() {

        var pickFrame: Frame?
        while (pickQueue.poll().also { pickFrame = it } != null) {
            pickFrame?.signalDone()
            pickFrame?.recycle()
        }
        var frame: Frame?
        while (frameQueue.poll().also { frame = it } != null) {
            frame?.recycle()
        }

        frameQueue.clear()
        currentFrame?.recycle()
        currentFrame = null
    }

    /**
     * Determines the World Wind objects displayed at a screen point. The screen point is interpreted as coordinates in
     * Android screen pixels relative to this View.
     * <p/>
     * If the screen point intersects any number of World Wind shapes, the returned list contains a picked object
     * identifying the top shape at the screen point. This picked object includes the shape renderable or its non-null
     * pick delegate, the shape's geographic position, and the World Wind layer that displayed the shape. Shapes which
     * are either hidden behind another shape at the screen point or hidden behind terrain at the screen point are
     * omitted from the returned list. Therefore if the returned list contains a picked object identifying a shape, it
     * is always marked as 'on top'.
     * <p/>
     * If the screen point intersects the World Wind terrain, the returned list contains a picked object identifying the
     * associated geographic position. If there are no shapes in the World Wind scene between the terrain and the screen
     * point, the terrain picked object is marked as 'on top'.
     * <p/>
     * This returns an empty list when nothing in the World Wind scene intersects the screen point, when the screen
     * point is outside this View's bounds, or if the OpenGL thread displaying the World Window scene is paused (or
     * becomes paused while this method is executing).
     *
     * @param x the screen point's X coordinate in Android screen pixels
     * @param y the screen point's Y coordinate in Android screen pixels
     *
     * @return A list of World Wind objects at the screen point
     */
    fun pick(
        x: Float,
        y: Float
    ): PickedObjectList {
        val pickedObjects = PickedObjectList()

        // Nothing can be picked if this World Window's OpenGL thread is paused.
        if (isPaused) {
            return pickedObjects
        }
        // Compute the pick point in OpenGL screen coordinates, rounding to the nearest whole pixel. Nothing can be picked
        // if pick point is outside the World Window's viewport.
        val px = Math.round(x)
        val py = Math.round(this.height - y)
        // Nothing can be picked if the pick point is outside of the World Window's viewport.
        if (!viewport.contains(px, py)) {
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
        frame.pickViewport = Viewport(px - 1, py - 1, 3, 3)
        frame.pickViewport!!.intersect(viewport)
        frame.pickPoint = Vec2(px.toDouble(), py.toDouble())
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
        // Transform the Cartesian point to OpenGL screen coordinates. Complete the transformation by converting to
        // Android screen coordinates and discarding the screen Z component.
        if (scratchProjection.project(x, y, z, viewport, scratchPoint)) {
            result.x = scratchPoint.x.toFloat()
            result.y = (this.height - scratchPoint.y).toFloat()
            return true
        }

        return false
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

        // Convert from Android screen coordinates to OpenGL screen coordinates by inverting the Y axis.
        val sx = x.toDouble()
        val sy = this.height - y.toDouble()

        // Compute the inverse modelview-projection matrix corresponding to the World Window's current Navigator state.
        computeViewingTransform(scratchProjection, scratchModelview)
        scratchProjection.multiplyByMatrix(scratchModelview).invert()

        // Transform the screen point to Cartesian coordinates at the near and far clip planes, store the result in the
        // ray's origin and direction, respectively. Complete the ray direction by subtracting the near point from the
        // far point and normalizing.
        if (scratchProjection.unProject(sx, sy, viewport, result.origin /*near*/, result.direction /*far*/)) {
            result.direction.subtract(result.origin).normalize()
            return true
        }

        return false
    }


}