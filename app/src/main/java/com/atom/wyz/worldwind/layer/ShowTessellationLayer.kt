package com.atom.wyz.worldwind.layer

import android.graphics.Color
import com.atom.wyz.worldwind.layer.render.RenderContext
import com.atom.wyz.worldwind.layer.draw.DrawableTessellation
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.core.shader.BasicProgram
import com.atom.wyz.worldwind.util.pool.Pool

class ShowTessellationLayer : AbstractLayer("Terrain Tessellation") {

    init {
        this.pickEnabled = false
    }
    protected var color: SimpleColor = SimpleColor(Color.WHITE)

    override fun doRender(rc: RenderContext) {
        val terrain = rc.terrain ?: return
        if (terrain.sector.isEmpty()) {
            return
        }
        var program: BasicProgram? = rc.getProgram(
            BasicProgram.KEY) as BasicProgram?
        if (program == null) {
            program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources)
            ) as BasicProgram
        }
        val pool: Pool<DrawableTessellation> = rc.getDrawablePool(
            DrawableTessellation::class.java)
        val drawable = DrawableTessellation.obtain(pool).set(program , color)
        rc.offerSurfaceDrawable(drawable, 1.0)
    }
}