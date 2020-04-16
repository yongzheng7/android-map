package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableTessellation
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.util.pool.Pool

class ShowTessellationLayer : AbstractLayer("Terrain Tessellation") {

    protected var color: Color = Color.WHITE

    override fun doRender(dc: DrawContext) {
        val terrain = dc.terrain ?: return
        if (terrain.getTileCount() == 0) {
            return
        }
        var program: BasicProgram? = dc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (program == null) {
            val resources = dc.resources ?: return
            program = dc.putProgram(BasicProgram.KEY, BasicProgram(resources)) as BasicProgram
        }
        val pool: Pool<DrawableTessellation> = dc.getDrawablePool(DrawableTessellation::class.java)
        val drawable = DrawableTessellation.obtain(pool).set(program , color)
        dc.offerSurfaceDrawable(drawable, 1.0)
    }
}