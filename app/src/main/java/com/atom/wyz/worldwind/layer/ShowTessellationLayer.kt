package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.DrawableTessellation
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.util.pool.Pool

class ShowTessellationLayer : AbstractLayer("Terrain Tessellation") {

    init {
        this.pickEnabled = false
    }
    protected var color: Color = Color.WHITE

    override fun doRender(rc: RenderContext) {
        val terrain = rc.terrain ?: return
        if (terrain.sector.isEmpty()) {
            return
        }
        var program: BasicProgram? = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (program == null) {
            val resources = rc.resources ?: return
            program = rc.putProgram(BasicProgram.KEY, BasicProgram(resources)) as BasicProgram
        }
        val pool: Pool<DrawableTessellation> = rc.getDrawablePool(DrawableTessellation::class.java)
        val drawable = DrawableTessellation.obtain(pool).set(program , color)
        rc.offerSurfaceDrawable(drawable, 1.0)
    }
}