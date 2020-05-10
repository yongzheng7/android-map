package com.atom.wyz.worldwind.render

import android.content.res.Resources
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class SkyProgram(resources: Resources) : AtmosphereProgram() {
    companion object {
        val KEY: Any = SkyProgram::class
    }

    init {
        try {
            val vs: String = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_skyprogram_vert)
            val fs: String = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_skyprogram_frag)
            this.programSources = arrayOf(vs, fs)
            this.attribBindings = arrayOf("vertexPoint")
        } catch (logged: Exception) {
            Logger.logMessage(Logger.ERROR, "SkyProgram", "constructor", "errorReadingProgramSource", logged)
        }
    }

}