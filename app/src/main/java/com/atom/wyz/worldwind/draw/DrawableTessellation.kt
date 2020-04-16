package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableTessellation : Drawable {

    companion object {

        fun obtain(pool: Pool<DrawableTessellation>): DrawableTessellation {
            return pool.acquire()?.setPool(pool) ?: DrawableTessellation().setPool(pool)
        }
    }

    protected var pool: Pool<DrawableTessellation>? = null

    protected var offsetMvpMatrix: Matrix4 = Matrix4()

    protected var mvpMatrix: Matrix4 = Matrix4()

    protected var program: BasicProgram? = null

    protected var color = Color()

    private fun setPool(pool: Pool<DrawableTessellation>): DrawableTessellation {
        this.pool = pool
        return this
    }

    operator fun set(program: BasicProgram, color: Color?): DrawableTessellation {
        this.program = program
        if (color != null) {
            this.color.set(color)
        } else {
            this.color.set(1f, 1f, 1f, 1f)
        }
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return  // program failed to build
        }
        program.enableTexture(false)
        program.loadColor(color)
        //禁止写入深度缓存
        GLES20.glDepthMask(false)

        offsetMvpMatrix.set(dc.projection)
        // z数值变小到 99.9%
        offsetMvpMatrix.offsetProjectionDepth(-1.0e-3) // offset this layer's depth values toward the eye
        // 乘上变换矩阵
        offsetMvpMatrix.multiplyByMatrix(dc.modelview)

        val terrain = dc.terrain ?: return
        var idx = 0
        val len: Int = terrain.getTileCount()
        while (idx < len) {

            val terrainOrigin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue
            mvpMatrix.set(offsetMvpMatrix)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program.loadModelviewProjection(mvpMatrix)
            terrain.useVertexPointAttrib(dc, idx, 0)
            terrain.drawTileLines(dc, idx)
            idx++
        }
        GLES20.glDepthMask(true)
    }

    override fun recycle() {
        program = null
        pool?.release(this) // return this instance to the pool
        pool = null
    }
}