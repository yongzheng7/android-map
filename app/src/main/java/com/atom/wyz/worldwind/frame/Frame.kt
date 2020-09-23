package com.atom.wyz.worldwind.frame

import com.atom.wyz.worldwind.layer.draw.DrawableQueue
import com.atom.wyz.worldwind.geom.Line
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec2
import com.atom.wyz.worldwind.geom.Viewport
import com.atom.wyz.worldwind.layer.render.pick.PickedObjectList
import com.atom.wyz.worldwind.util.pool.Pool
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class Frame {
    companion object{
        fun obtain(pool: Pool<Frame>): Frame {
            val instance: Frame? = pool.acquire() // get an instance from the pool
            return if (instance != null) instance.init(pool) else Frame().init(pool)
        }
    }

    val viewport = Viewport()

    val modelview: Matrix4 = Matrix4()

    val projection: Matrix4 = Matrix4()

    val infiniteProjection = Matrix4()

    val drawableQueue: DrawableQueue =
        DrawableQueue()

    val drawableTerrain: DrawableQueue =
        DrawableQueue()

    var pickedObjects: PickedObjectList? = null

    var pickPoint: Vec2? = null

    var pickRay: Line? = null

    var pickMode = false

    var pickViewport: Viewport? = null

    private var isDone = false

    private var isAwaitingDone = false

    private var doneLock: Lock = ReentrantLock()

    private var doneCondition = doneLock.newCondition()

    private var pool: Pool<Frame>? = null

    private fun init(pool: Pool<Frame>):Frame {
        this.pool = pool
        isDone = false
        isAwaitingDone = false
        return this
    }

    fun recycle() {
        viewport.setEmpty()
        modelview.setToIdentity()
        projection.setToIdentity()
        drawableQueue.clearDrawables()
        drawableTerrain.clearDrawables()

        pickViewport = null
        pickedObjects = null
        pickRay = null
        pickPoint = null
        pickMode = false

        pool?.release(this)
        pool = null
    }

    fun awaitDone() {
        doneLock.lock()
        try {
            while (!isDone) {
                isAwaitingDone = true
                doneCondition.await()
            }
        } catch (ignored: InterruptedException) {
        } finally {
            doneLock.unlock()
        }
    }

    fun signalDone() {
        doneLock.lock()
        try {
            isDone = true
            if (isAwaitingDone) {
                doneCondition.signal()
            }
        } finally {
            doneLock.unlock()
        }
    }
}