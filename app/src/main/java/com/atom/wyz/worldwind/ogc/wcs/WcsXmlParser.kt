package com.atom.wyz.worldwind.ogc.wcs

import android.util.Xml
import com.atom.wyz.worldwind.ogc.gml.GmlParser
import com.atom.wyz.worldwind.ogc.ows.OwsXmlParser
import com.atom.wyz.worldwind.util.xml.XmlModelParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class WcsXmlParser : XmlModelParser() {

    companion object {
        @Throws(IOException::class, XmlPullParserException::class)
        fun parse(inputStream: InputStream?): Any? {
            val pullParser = Xml.newPullParser()
            pullParser.setInput(inputStream, null /*inputEncoding*/)
            val modelParser: XmlModelParser =
                WcsXmlParser()
            modelParser.xpp = (pullParser)
            return modelParser.parse()
        }
    }

    protected var wcs20Namespace = "http://www.opengis.net/wcs/2.0"

    init {
        registerOwsModels()
        registerGmlModels()
        registerWcs20Models(wcs20Namespace)
    }

    protected fun registerOwsModels() {
        registerAllModels(OwsXmlParser())
    }

    protected fun registerGmlModels() {
        registerAllModels(GmlParser())
    }

    protected fun registerWcs20Models(namespace: String?) {
        registerXmlModel(namespace, "CoverageDescriptions", Wcs201CoverageDescriptions::class.java)
        registerXmlModel(namespace, "CoverageDescription", Wcs201CoverageDescription::class.java)
        registerTxtModel(namespace, "CoverageId")
    }
}