package com.atom.wyz.worldwind.core.bean

import com.atom.wyz.worldwind.core.egl.EGLHelper


class TextureBean {
    var egl: EGLHelper? = null
    var sourceWidth: Int = 0
    var sourceHeight: Int = 0
    var textureId = 0
    var endFlag = false
    var timeStamp: Long = 0
    var textureTime: Long = 0
    var threadId: Long = 0
}