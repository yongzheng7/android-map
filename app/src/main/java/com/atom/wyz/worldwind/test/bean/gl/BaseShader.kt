package com.atom.wyz.worldwind.test.bean.gl

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.test.Renderer
import com.atom.wyz.worldwind.test.bean.FrameBuffer
import com.atom.wyz.worldwind.test.utils.MatrixUtils.Companion.getOriginalMatrix
import com.atom.wyz.worldwind.test.utils.MatrixUtils.Companion.getOriginalTextureCo
import com.atom.wyz.worldwind.test.utils.MatrixUtils.Companion.getOriginalVertexCo
import com.atom.wyz.worldwind.test.utils.ShaderUtils.Companion.createGLProgram
import com.atom.wyz.worldwind.test.utils.ShaderUtils.Companion.createGLProgramByAssetsFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

abstract class BaseShader : Renderer {

    companion object {
        val BASE_VERT = """attribute vec4 aVertexCo;
                            attribute vec2 aTextureCo;

                            uniform mat4 uVertexMatrix;
                            uniform mat4 uTextureMatrix;

                            varying vec2 vTextureCo;

                            void main(){
                                gl_Position = uVertexMatrix*aVertexCo;
                                vTextureCo = (uTextureMatrix*vec4(aTextureCo,0,1)).xy;
                            }"""

        val BASE_FRAG = """precision mediump float;
            
                            varying vec2 vTextureCo;
                            uniform sampler2D uTexture;
                            
                            void main() {
                                gl_FragColor = texture2D( uTexture, vTextureCo);
                            }"""
    }

    protected var mWidth = 0
    protected var mHeight = 0

    protected var mRes: Resources? = null
    private var mVertex: String? = null
    private var mFragment: String? = null


    protected var mVertexBuffer: FloatBuffer? = null
    protected var mTextureBuffer: FloatBuffer? = null

    protected var mVertexMatrix = getOriginalMatrix()
    protected var mTextureMatrix = getOriginalMatrix()


    protected var mGLProgram = 0
    protected var mGLVertexCo = 0
    protected var mGLTextureCo = 0
    protected var mGLVertexMatrix = 0
    protected var mGLTextureMatrix = 0
    protected var mGLTexture = 0

    private var mGLWidth = 0
    private var mGLHeight = 0
    private var isUseExpandConfig = false

    private var mFrameTemp: FrameBuffer
    private val mTasks = LinkedList<Runnable>()

    protected constructor(resource: Resources?, vertex: String?, fragment: String?) {
        mRes = resource
        mVertex = vertex
        mFragment = fragment
        mFrameTemp = FrameBuffer()
        initBuffer()
    }

    protected open fun initBuffer() {
        ByteBuffer.allocateDirect(32)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .also {
                    it.put(getOriginalVertexCo())
                    it.position(0)
                    mVertexBuffer = it
                }
        ByteBuffer.allocateDirect(32)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().also {
                    it.put(getOriginalTextureCo())
                    it.position(0)
                    mTextureBuffer = it
                }
    }

    open fun setVertexCo(vertexCo: FloatArray) {
        mVertexBuffer?.clear()
        mVertexBuffer?.put(vertexCo)
        mVertexBuffer?.position(0)
    }

    open fun setTextureCo(textureCo: FloatArray) {
        mTextureBuffer?.clear()
        mTextureBuffer?.put(textureCo)
        mTextureBuffer?.position(0)
    }

    open fun setVertexBuffer(vertexBuffer: FloatBuffer) {
        mVertexBuffer = vertexBuffer
    }

    open fun setTextureBuffer(textureBuffer: FloatBuffer) {
        mTextureBuffer = textureBuffer
    }

    open fun setVertexMatrix(matrix: FloatArray) {
        mVertexMatrix = matrix
    }

    open fun setTextureMatrix(matrix: FloatArray) {
        mTextureMatrix = matrix
    }

    open fun getVertexMatrix(): FloatArray {
        return mVertexMatrix
    }

    open fun getTextureMatrix(): FloatArray {
        return mTextureMatrix
    }

    open fun needUseExpandConfig(need: Boolean) {
        isUseExpandConfig = need
    }

    open fun runOnGLThread(runnable: Runnable) {
        mTasks.addLast(runnable)
    }

    override fun create() {
        if (mVertex != null && mFragment != null) {
            onCreate()
        }
    }

    protected open fun onCreate() {
        mGLProgram = if (mRes != null) {
            createGLProgramByAssetsFile(mRes!!, mVertex!!, mFragment!!)
        } else {
            createGLProgram(mVertex, mFragment)
        }
        mGLVertexCo = GLES20.glGetAttribLocation(mGLProgram, "aVertexCo")
        mGLTextureCo = GLES20.glGetAttribLocation(mGLProgram, "aTextureCo")

        mGLVertexMatrix = GLES20.glGetUniformLocation(mGLProgram, "uVertexMatrix")
        mGLTextureMatrix = GLES20.glGetUniformLocation(mGLProgram, "uTextureMatrix")
        mGLTexture = GLES20.glGetUniformLocation(mGLProgram, "uTexture")
        if (isUseExpandConfig) {
            onCreateExpandConfig()
        }
    }

    protected open fun onCreateExpandConfig() {
        mGLWidth = GLES20.glGetUniformLocation(mGLProgram, "uWidth")
        mGLHeight = GLES20.glGetUniformLocation(mGLProgram, "uHeight")
    }

    override fun sizeChanged(width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
        onSizeChanged(width, height)
        mFrameTemp.destroyFrameBuffer()
    }

    protected open fun onSizeChanged(width: Int, height: Int) {

    }


    override fun draw(texture: Int) {
        onClear()
        onUseProgram()
        onSetExpandData()
        onBindTexture(texture)
        onDraw()
    }


    /**
     * 绘制内容到纹理上
     * @param texture 输入纹理ID 将该纹理绘制到自定义帧缓存上
     * @return 输出纹理ID 获取帧缓存上绑定的颜色附件id = 纹理id
     */
    override fun drawToTexture(texture: Int): Int {
        mFrameTemp.bindFrameBuffer(mWidth, mHeight)
        draw(texture)
        mFrameTemp.unBindFrameBuffer()
        return mFrameTemp.cacheTextureId
    }

    protected open fun onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    protected open fun onUseProgram() {
        GLES20.glUseProgram(mGLProgram)
        onTaskExec()
    }

    protected open fun onTaskExec() {
        while (!mTasks.isEmpty()) {
            mTasks.removeFirst().run()
        }
    }

    protected open fun onSetExpandData() {
        GLES20.glUniformMatrix4fv(mGLVertexMatrix, 1, false, mVertexMatrix, 0)
        GLES20.glUniformMatrix4fv(mGLTextureMatrix, 1, false, mTextureMatrix, 0)
        if (isUseExpandConfig) {
           onDrawExpandConfig()
        }
    }

    protected open fun onDrawExpandConfig(){
        GLES20.glUniform1f(mGLWidth, mWidth.toFloat())
        GLES20.glUniform1f(mGLHeight, mHeight.toFloat())
    }

    protected open fun onBindTexture(textureId: Int?) {
        textureId?.also {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, it)
            GLES20.glUniform1i(mGLTexture, 0)
        }
    }

    protected open fun onDraw() {
        GLES20.glEnableVertexAttribArray(mGLVertexCo)
        GLES20.glVertexAttribPointer(mGLVertexCo, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mGLTextureCo)
        GLES20.glVertexAttribPointer(mGLTextureCo, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mGLVertexCo)
        GLES20.glDisableVertexAttribArray(mGLTextureCo)
    }

    override fun destroy() {
        GLES20.glDeleteProgram(mGLProgram)
        mFrameTemp.destroyFrameBuffer()
    }

}