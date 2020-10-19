package com.atom.map.core.shader

import android.content.res.Resources
import com.atom.map.R
import com.atom.map.util.Logger
import com.atom.map.util.WWUtil

class SkyProgram(resources: Resources) : AtmosphereProgram() {
    companion object {
        val KEY: Any = SkyProgram::class
    }

    init {
        try {
            val vs: String = WWUtil.readResourceAsText(resources, R.raw.sky_program_vert)
            val fs: String = WWUtil.readResourceAsText(resources, R.raw.sky_program_frag)
            this.programSources = arrayOf(vs, fs)
            this.attribBindings = arrayOf("vertexPoint")
        } catch (logged: Exception) {
            Logger.logMessage(Logger.ERROR, "SkyProgram", "constructor", "errorReadingProgramSource", logged)
        }
    }

}