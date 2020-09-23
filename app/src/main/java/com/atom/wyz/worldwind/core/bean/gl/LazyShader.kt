package com.atom.wyz.worldwind.core.bean.gl

import android.content.res.Resources

open class LazyShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/base.frag")

    constructor(vert: String, frag: String) : super(null, vert, frag)

    constructor() : super(null, BASE_VERT, BASE_FRAG)

}