package com.atom.wyz.worldwind.util.xml

import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NameStringModel : XmlModel {

    constructor(namespaceUri: String?) : super(namespaceUri)

    @Throws(XmlPullParserException::class, IOException::class)
    override fun read(ctx: XmlPullParserContext): Any? {
        var s: String = ctx.parser ?.name ?: return null
        if (s.isNotEmpty()) {
            s = s.replace("\n".toRegex(), "").trim { it <= ' ' }
        }
        val sb = this.getField(CHARACTERS_CONTENT) as StringBuilder?
        sb?.append(s) ?: this.setField(CHARACTERS_CONTENT, StringBuilder(s))
        return super.read(ctx)
    }
}