package com.atom.wyz.worldwind.render

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.util.Logger

open class GpuProgram() : RenderResource {
    companion object {
        protected const val VERTEX_SHADER = 0
        protected const val FRAGMENT_SHADER = 1
    }

    protected var programId = 0
    protected var programSources: Array<String>? = null
        get() {
            return field
        }
        set(value) {
            field = value
            programLength = 0
            mustBuildProgram = true
            for (str in value ?: return) {
                programLength += str.length
            }
        }
    protected var attribBindings: Array<String>? = null
        get() {
            return field
        }
        set(value) {
            field = value
            mustBuildProgram = true
        }

    var programLength = 0
    protected var shaderId = IntArray(2)
    protected var mustBuildProgram = true

    //使用program
    open fun useProgram(dc: DrawContext): Boolean {
        if (mustBuildProgram) {
            mustBuildProgram = false
            // 初始化
            if (programId != 0) {
                this.deleteProgram(dc)
            }
            // 加载着色器代码并绑定上
            if (programSources != null) {
                this.buildProgram(dc, programSources!!, attribBindings)
            }
            // 判断加载上了
            if (programId != 0) {
                val currProgram: Int = dc.currentProgram()
                try {
                    // 分两步  1 加载GLES20.glUseProgram(id)  2 通过programid 加载变量
                    dc.useProgram(programId)
                    initProgram(dc)
                } finally {
                    dc.useProgram(currProgram)
                }
            }
        }
        if (programId != 0) {
            dc.useProgram(programId)
        }
        return programId != 0
    }

    protected open fun buildProgram(dc: DrawContext, programSource: Array<String>, attribBindings: Array<String>?) {
        val status = IntArray(1)
        val vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vs, programSource[VERTEX_SHADER])
        GLES20.glCompileShader(vs)
        GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val msg = GLES20.glGetShaderInfoLog(vs)
            GLES20.glDeleteShader(vs)
            Logger.logMessage(Logger.ERROR, "GpuProgram  ${this.javaClass.simpleName}", "buildProgram",
                    "Error compiling GL vertex shader \n$msg")
            return
        }
        val fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fs, programSource[FRAGMENT_SHADER])
        GLES20.glCompileShader(fs)
        GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val msg = GLES20.glGetShaderInfoLog(fs)
            GLES20.glDeleteShader(vs)
            GLES20.glDeleteShader(fs)
            Logger.logMessage(Logger.ERROR, "GpuProgram  ${this.javaClass.simpleName}", "buildProgram",
                    "Error compiling GL fragment shader \n$msg")
            return
        }
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vs)
        GLES20.glAttachShader(program, fs)
        if (attribBindings != null) {
            for (i in attribBindings.indices) {
                GLES20.glBindAttribLocation(program, i, attribBindings[i])
            }
        }
        GLES20.glLinkProgram(program)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val msg = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            GLES20.glDeleteShader(vs)
            GLES20.glDeleteShader(fs)
            Logger.logMessage(Logger.ERROR, "GpuProgram  ${this.javaClass.simpleName}", "buildProgram",
                    "Error linking GL program \n$msg")
            return
        }
        programId = program
        shaderId[0] = vs
        shaderId[1] = fs
    }

    protected open fun initProgram(dc: DrawContext) {}

    protected open fun deleteProgram(dc: DrawContext) {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            GLES20.glDeleteShader(shaderId[VERTEX_SHADER])
            GLES20.glDeleteShader(shaderId[FRAGMENT_SHADER])
            programId = 0
            shaderId[VERTEX_SHADER] = 0
            shaderId[FRAGMENT_SHADER] = 0
        }
    }

    override fun release(dc: DrawContext) {
        this.deleteProgram(dc)
    }
}