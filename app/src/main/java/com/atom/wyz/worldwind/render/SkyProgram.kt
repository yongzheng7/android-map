package com.atom.wyz.worldwind.render

import android.content.res.Resources
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class SkyProgram(resources: Resources) : AtmosphereAndGroundProgram() {
    companion object {
        val KEY: Any = SkyProgram::class.java.name
    }

    init {
        try {
            val vs: String = WWUtil.readResourceAsText(resources, R.raw.sky_worldwind_vert)
            val fs: String = WWUtil.readResourceAsText(resources, R.raw.sky_ground_worldwind_frag)
            this.programSources = arrayOf(vs, fs)
            this.attribBindings = arrayOf("vertexPoint")
        } catch (logged: Exception) {
            Logger.logMessage(Logger.ERROR, "SkyProgram", "constructor", "errorReadingProgramSource", logged)
        }
    }

}