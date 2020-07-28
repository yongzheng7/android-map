package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableSurfaceColor : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableSurfaceColor>): DrawableSurfaceColor =
            pool.acquire()?.setPool(pool) ?: DrawableSurfaceColor().setPool(pool)
    }

    var program: BasicProgram? = null

    var color: Color = Color()

    val mvpMatrix: Matrix4 = Matrix4()

    var pool: Pool<DrawableSurfaceColor>? = null

    private fun setPool(pool: Pool<DrawableSurfaceColor>): DrawableSurfaceColor {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return
        if (!program.useProgram(dc)) {
            return  // program failed to build
        }

        program.enableTexture(false)
        program.loadColor(color)

        var idx = 0
        val len = dc.getDrawableTerrainCount()
        while (idx < len) {
            // Get the drawable terrain associated with the draw context.
            val terrain = dc.getDrawableTerrain(idx) ?: continue
            // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
            // Use the terrain's vertex point attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                continue  // vertex buffer failed to bind
            }
            val terrainOrigin: Vec3 = terrain.vertexOrigin
            mvpMatrix.set(dc.modelviewProjection)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program.loadModelviewProjection(mvpMatrix)

            terrain.drawTriangles(dc)
            idx++
        }
    }

    override fun recycle() {
        program = null
        pool?.release(this)
        pool = null
    }
}