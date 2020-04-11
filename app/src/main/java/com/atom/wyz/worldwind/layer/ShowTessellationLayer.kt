package com.atom.wyz.worldwind.layer

import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.DrawContext

class ShowTessellationLayer : AbstractLayer("Terrain Tessellation") {

    protected var offsetMvpMatrix: Matrix4 = Matrix4()

    protected var mvpMatrix: Matrix4 = Matrix4()
    @Volatile
    var aa = 0 ;
    override fun doRender(dc: DrawContext) {

        val terrain: Terrain = dc.terrain ?: return
        if(terrain.getTileCount() == 0) return ;

        var program = dc.getProgram(BasicProgram.KEY) as BasicProgram?

        if(program == null){
            program = dc.putProgram(BasicProgram.KEY , dc.resources ?.let { BasicProgram(it) } ?: return) as BasicProgram
        }

        // Use World Wind's basic GLSL program.
        if(!program.useProgram(dc)) return

        // Configure the program to draw opaque white fragments.
        program.enableTexture(false)
        program.loadColor(1f, 1f, 1f, 1f)

        // Suppress writes to the OpenGL depth buffer.
        // 禁止写入OpenGL深度缓冲区。
        GLES20.glDepthMask(false)

        this.offsetMvpMatrix.set(dc.projection).offsetProjectionDepth(-1.0e-3).multiplyByMatrix(dc.modelview)

        // Get the draw context's tessellated terrain and modelview projection matrix.


        for (idx in 0 until (terrain.getTileCount() ?: 0)) { // Use the draw context's modelview projection matrix, offset by the tile's origin.

            val origin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue

            this.mvpMatrix.set(this.offsetMvpMatrix)
                    .multiplyByTranslation(origin.x, origin.y, origin.z)

            program.loadModelviewProjection(this.mvpMatrix)
            // Use the tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0)
            // Draw the tile vertices as lines.
            terrain.drawTileLines(dc, idx)

        }

        // Restore default World Wind OpenGL state.
        GLES20.glDepthMask(true)
    }
}