package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext

class WmsFormat(namespaceUri: String?) : XmlModel(namespaceUri) {

    protected override fun doAddCharacters(ctx: XmlPullParserContext) {
        var s: String? = ctx.parser?.text
        s = if (s == null || s.isEmpty()) {
            return
        } else {
            s.replace("\n".toRegex(), "").trim { it <= ' ' }.toLowerCase()
        }
        val sb =
            this.getField(CHARACTERS_CONTENT) as StringBuilder?
        sb?.append(s) ?: this.setField(CHARACTERS_CONTENT, StringBuilder(s))
    }
}