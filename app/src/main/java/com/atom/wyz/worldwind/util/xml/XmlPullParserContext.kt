package com.atom.wyz.worldwind.util.xml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.lang.reflect.Constructor
import javax.xml.namespace.QName

open class XmlPullParserContext(namespaceUri: String?) {
    companion object {
        const val DEFAULT_NAMESPACE = "http://www.opengis.net/wms"
    }

    lateinit var parser: XmlPullParser

    var parserModels: HashMap<QName, XmlModel?> = hashMapOf()

    var namespaceUri :String ?= namespaceUri
    set(value) {
        field = value
        parserModels.clear()
        initializeParsers()
    }
    init {
        this.initializeParsers()
    }
    protected open fun initializeParsers() {}
    // 设置xml文件的字节输入流
    @Throws(XmlPullParserException::class)
    fun setParserInput(`is`: InputStream?) {
        this.parser = Xml.newPullParser()
        this.parser.setInput(`is`, null)
    }
    fun createParsableModel(eventName: QName): XmlModel? {
        val model = this.parserModels[eventName] ?: return XmlModel(eventName.namespaceURI)
        try {
            val ctor: Constructor<out XmlModel> = model::class.java.getConstructor(String::class.java)
            //ctor.setAccessible(true);
            return ctor.newInstance(eventName.namespaceURI) as XmlModel
        } catch (e: Exception) {
            // TODO log error
        }
        return null
    }

    fun registerParsableModel(elementName: QName, parsableModel: XmlModel) {
        parserModels.put(elementName, parsableModel)
    }

    fun getUnrecognizedElementModel(): XmlModel {
        return XmlModel(this.namespaceUri)
    }
}