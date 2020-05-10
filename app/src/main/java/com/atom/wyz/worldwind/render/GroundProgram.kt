package com.atom.wyz.worldwind.render

import android.content.res.Resources
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class GroundProgram(resources: Resources) : AtmosphereProgram() {
    companion object{
        val KEY: Any = GroundProgram::class
    }
    init {
        try {
            val vs: String = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_groundprogram_vert)
            val fs: String = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_groundprogram_frag)
            this.programSources = arrayOf(vs, fs)
            this.attribBindings = arrayOf("vertexPoint", "vertexTexCoord")
        } catch (logged: Exception) {
            Logger.logMessage(Logger.ERROR, "GroundProgram", "constructor", "errorReadingProgramSource", logged)
        }
    }
}