package com.atom.wyz.worldwind.frame

import android.graphics.Rect
import com.atom.wyz.worldwind.draw.DrawableList
import com.atom.wyz.worldwind.draw.DrawableQueue
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.pool.Pool

class Frame {
    companion object{
        fun obtain(pool: Pool<Frame>): Frame {
            val instance: Frame? = pool.acquire() // get an instance from the pool
            return if (instance != null) instance.setPool(pool) else Frame().setPool(pool)
        }
    }

    val viewport = Rect()

    val modelview: Matrix4 = Matrix4()

    val projection: Matrix4 = Matrix4()

    val drawableQueue: DrawableQueue = DrawableQueue()

    val drawableTerrain: DrawableList = DrawableList()

    private var pool: Pool<Frame>? = null

    private fun setPool(pool: Pool<Frame>):Frame {
        this.pool = pool
        return this
    }

    fun recycle() {
        viewport.setEmpty()
        modelview.setToIdentity()
        projection.setToIdentity()
        drawableQueue.clearDrawables()
        drawableTerrain.clearDrawables()
        pool?.release(this)
        pool = null
    }
}