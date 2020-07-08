package com.atom.wyz.worldwind

import android.annotation.SuppressLint
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
        const val MAX_FRAME_QUEUE_SIZE = 2

        const val MSG_ID_CLEAR_CACHE = 1

        const val MSG_ID_REQUEST_REDRAW = 2

        const val MSG_ID_SET_VIEWPORT = 3

        const val MSG_ID_SET_DEPTH_BITS = 4
    }

    var globe: Globe = GlobeWgs84()

    var layers: LayerList = LayerList()

    var verticalExaggeration: Double = 1.0

    var navigator = Navigator()

    var fieldOfView = 45.0
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
    var renderResourceCache: RenderResourceCache? = null

    var rc = RenderContext()

    var dc = DrawContext()

    var viewport = Viewport()

    var depthBits = 0

    protected var framePool: Pool<Frame> = SynchronizedPool()

    protected var frameQueue: Queue<Frame> = ConcurrentLinkedQueue()

    protected var pickQueue: Queue<Frame> = ConcurrentLinkedQueue()

    protected var currentFrame: Frame? = null

    protected var isPaused = false

    protected var isWaitingForRedraw = false

    protected var mainLoopHandler =
        Handler(Looper.getMainLooper(), Handler.Callback {
            if (it.what == MSG_ID_CLEAR_CACHE) {
                renderResourceCache?.clear()
            } else if (it.what == MSG_ID_REQUEST_REDRAW) {
                requestRedraw()
            } else if (it.what == MSG_ID_SET_VIEWPORT) {
                viewport.set((it.obj as Viewport))
            } else if (it.what == MSG_ID_SET_DEPTH_BITS) {
                depthBits = it.obj as Int
            }
            false
        })



    private val scratchCamera = Camera()

    private val scratchModelview = Matrix4()

    private val scratchProjection = Matrix4()

    private val scratchPoint: Vec3 = Vec3()

    private val scratchRay = Line()

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

        navigator.latitude  = 39.916527
        navigator.longitude = 116.397128
        navigator.altitude  = initAltitude

        this.worldWindowController.worldWindow = this

        // Initialize the World Window's render resource cache.
        val cacheCapacity = RenderResourceCache.recommendedCapacity(this.context)
        renderResourceCache = RenderResourceCache(cacheCapacity)

        this.setEGLConfigChooser(configChooser)
        this.setEGLContextClientVersion(2) // must be called before setRenderer
        this.setRenderer(this)
        this.renderMode = RENDERMODE_WHEN_DIRTY // must be called after setRenderer
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
            try {
                drawFrame(pickFrame)
            } catch (e: java.lang.Exception) {
                Logger.logMessage(
                    Logger.ERROR, "WorldWindow", "onDrawFrame",
                    "Exception while processing pick in OpenGL thread", e
                )
            } finally {
                pickFrame.signalDone()
                pickFrame.recycle()
                super.requestRender()
            }
        }

        var nextFrame = frameQueue.poll()
        if (nextFrame != null) {
            currentFrame?.recycle()
            currentFrame = nextFrame
            super.requestRender()
        }
        try {
            currentFrame?.let {
                drawFrame(it)
            }
        } catch (e: java.lang.Exception) {
            Logger.logMessage(
                Logger.ERROR, "WorldWindow", "onDrawFrame",
                "Exception while drawing frame in OpenGL thread", e
            )
        }
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        reset()
    }

    protected fun computeViewingTransform(
        projection: Matrix4,
        modelview: Matrix4?
    ) {
        var near = navigator.altitude * 0.5
        val far = globe.horizonDistance(navigator.altitude, 160000.0)
        viewport.set(this.viewport)

        if (depthBits != 0) {
            val maxDepthValue = (1 shl depthBits) - 1.toDouble()
            val farResolution = 10.0
            val nearDistance = far / (maxDepthValue / (1 - farResolution / far) - maxDepthValue + 1)
            // Use the computed near distance only when it's less than our default distance.
            if (near > nearDistance) {
                near = nearDistance
            }
        }

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
            frameMetrics.beginRendering(this.rc)
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

        computeViewingTransform(frame.projection, frame.modelview)
        frame.viewport.set(this.viewport)
        frame.infiniteProjection.setToInfiniteProjection(viewport.width.toDouble(), viewport.height.toDouble(), fieldOfView, 1.0)
        frame.infiniteProjection.multiplyByMatrix(frame.modelview)
        rc.viewport.set(frame.viewport)
        rc.projection.set(frame.projection)
        rc.modelview.set(frame.modelview)
        rc.modelviewProjection.setToMultiply(frame.projection, frame.modelview)
        if (pickMode) {
            frame.pickViewport ?.let {
                rc.frustum.setToModelviewProjection(frame.projection, frame.modelview, frame.viewport, it)
            }
        } else {
            rc.frustum.setToModelviewProjection(frame.projection, frame.modelview, frame.viewport)
        }

        rc.drawableQueue = frame.drawableQueue
        rc.drawableTerrain = frame.drawableTerrain
        rc.pickedObjects = frame.pickedObjects
        rc.pickViewport = frame.pickViewport
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
        // Mark the end of a frame render.
        if (!pickMode) {
            frameMetrics.endRendering(this.rc)
        }
        rc.reset()
    }


    protected fun drawFrame(frame: Frame) {
        val pickMode = frame.pickMode
        if (!pickMode) {
            frameMetrics.beginDrawing(this.dc)
        }
        dc.eyePoint = frame.modelview.extractEyePoint(dc.eyePoint)
        dc.viewport.set(frame.viewport)
        dc.projection.set(frame.projection)
        dc.modelview.set(frame.modelview)
        dc.modelviewProjection.setToMultiply(frame.projection, frame.modelview)
        dc.infiniteProjection.set(frame.infiniteProjection)
        dc.screenProjection.setToScreenProjection(
            frame.viewport.width.toDouble(),
            frame.viewport.height.toDouble()
        )

        dc.drawableQueue = frame.drawableQueue
        dc.drawableTerrain = frame.drawableTerrain

        dc.pickViewport = frame.pickViewport
        dc.pickedObjects = frame.pickedObjects
        dc.pickPoint = frame.pickPoint
        dc.pickMode = frame.pickMode

        frameController.drawFrame(dc)

        renderResourceCache?.releaseEvictedResources(dc)

        if (!pickMode) {
            frameMetrics.endDrawing(this.dc)
        }

        dc.reset()
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
        //在世界窗口渲染期间默认情况下启用GL顶点属性数组0
        GLES20.glEnableVertexAttribArray(0)

        GLES20.glDisable(GLES20.GL_DITHER)
        // 混合因子
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 深度小或相等的时候也渲染 （GL_LESS = 深度小的时候才渲染）
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        this.dc.contextLost()
        // Set the World Window's depth bits.
        val depthBits = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_DEPTH_BITS, depthBits, 0)
        this.mainLoopHandler.sendMessage(
            Message.obtain(
                this.mainLoopHandler,
                MSG_ID_SET_DEPTH_BITS /*msg.what*/,
                depthBits[0] /*msg.obj*/
            )
        )
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
            requestRedraw() // inherited from GLSurfaceView; may be called on any thread
        }
    }


    fun addNavigatorListener(listener: NavigatorListener) {
        navigatorEvents.addNavigatorListener(listener)
    }

    fun removeNavigatorListener(listener: NavigatorListener) {
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

        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        // The frame is recycled by the OpenGL thread.
        try {
            val frame = Frame.obtain(framePool)
            renderFrame(frame)
        } catch (e: java.lang.Exception) {
            Logger.logMessage(
                Logger.ERROR, "WorldWindow", "doFrame",
                "Exception while rendering frame in Choreographer callback \'$frameTimeNanos\'", e
            )
        }
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
    /**
     * Converts a screen point to the geographic coordinates on the globe.
     *
     * @param screenX X coordinate in Android screen coordinates
     * @param screenY Y coordinate in Android screen coordinates
     * @param result  Pre-allocated Position receives the geographic coordinates
     *
     * @return true if the screen point could be converted; false if the screen point is not on the globe
     */
    fun screenPointToGeographic(screenX: Float, screenY: Float, result: Position?): Boolean {
        val ray = Line()
        val intersection = Vec3()
        if (rayThroughScreenPoint(screenX, screenY, scratchRay)) {
            if (globe.intersect(scratchRay, scratchPoint)) {
                globe.cartesianToGeographic(scratchPoint.x, scratchPoint.y, scratchPoint.z, result)
                return true
            }
        }
        return false
    }

    fun pickShapesInRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ): PickedObjectList {
        // Allocate a list in which to collect and return the picked objects.
        val pickedObjects = PickedObjectList()
        // Nothing can be picked if the World Window's OpenGL thread is paused.
        if (isPaused) {
            return pickedObjects
        }
        val px = Math.floor(x.toDouble()).toInt()
        val py = Math.floor(height - (y + height).toDouble()).toInt()
        val pw = Math.ceil(width.toDouble()).toInt()
        val ph = Math.ceil(height.toDouble()).toInt()
        if (!viewport.intersects(px, py, pw, ph)) {
            return pickedObjects
        }
        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        val frame = Frame.obtain(framePool)
        frame.pickedObjects = pickedObjects
        frame.pickViewport = Viewport(px, py, pw, ph) // caller-specified pick rectangle
        frame.pickViewport!!.intersect(viewport) // limit the pick viewport to the screen viewport
        frame.pickMode = true
        renderFrame(frame)
        // Wait until the OpenGL thread is done processing the frame and resolving the picked objects.
        frame.awaitDone()
        return pickedObjects
    }
}