package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.SurfaceTexture
import com.atom.wyz.worldwind.render.SurfaceTextureProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableSurfaceTexture : Drawable, SurfaceTexture {
    companion object {
        public fun obtain(pool: Pool<DrawableSurfaceTexture>): DrawableSurfaceTexture {
            return pool.acquire()?.setPool(pool) ?: DrawableSurfaceTexture().setPool(pool)
        }
    }

    protected var program: SurfaceTextureProgram? = null

    protected var texture: GpuTexture? = null

    protected var texCoordMatrix = Matrix3()

    override var sector: Sector = Sector()
        get() = field
        set(value) {
            field.set(value)
        }

    override var texCoordTransform: Matrix3 = Matrix3()
        get() = field
        set(value) {
            field.set(value)
        }
    private var pool: Pool<DrawableSurfaceTexture>? = null
    private fun setPool(pool: Pool<DrawableSurfaceTexture>): DrawableSurfaceTexture {
        this.pool = pool
        return this
    }

    operator fun set(
        program: SurfaceTextureProgram?,
        sector: Sector?,
        texture: GpuTexture?,
        texCoordMatrix: Matrix3?
    ): DrawableSurfaceTexture {
        this.program = program
        this.texture = texture
        if (sector != null) {
            this.sector.set(sector)
        } else {
            this.sector.setEmpty()
        }
        if (texCoordMatrix != null) {
            this.texCoordMatrix.set(texCoordMatrix)
        } else {
            this.texCoordMatrix.setToIdentity()
        }
        return this
    }

    override fun bindTexture(dc: DrawContext): Boolean {
        return texture != null && texture!!.bindTexture(dc)
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return  // program failed to build
        }
        try {
            program.addSurfaceTexture(this)

            var next: Drawable?
            while (dc.peekDrawable().also { next = it } != null
                && this.canBatchWith(next!!)) {
                program.addSurfaceTexture(dc.pollDrawable() as SurfaceTexture?) // take it off the queue
            }
        } finally {
            // Draw all of the surface textures.
            program.draw(dc)
            // Clear the program's state.
            program.clear(dc)
        }
    }

    override fun recycle() {
        texture = null
        program = null
        pool?.release(this)
        pool = null
    }

    protected fun canBatchWith(that: Drawable): Boolean {
        return this.javaClass == that::javaClass && this.program === (that as DrawableSurfaceTexture).program
    }
}