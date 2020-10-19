package com.atom.map.ogc.ows

import android.util.Xml
import com.atom.map.util.WWUtil
import com.atom.map.util.xml.XmlModelParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URLConnection

class OwsXmlParser : XmlModelParser() {

    companion object{
        @Throws(IOException::class, XmlPullParserException::class)
        fun parse(inputStream: InputStream?): Any? {
            val pullParser = Xml.newPullParser()
            pullParser.setInput(inputStream, null /*inputEncoding*/)
            val modelParser: XmlModelParser =
                OwsXmlParser()
            modelParser.xpp = (pullParser)
            return modelParser.parse()
        }

        fun parseErrorStream(connection: URLConnection?): OwsExceptionReport? {
            var errorStream: InputStream? = null
            return try {
                if (connection !is HttpURLConnection) {
                    return null // need an HTTP connection to parse the error stream
                }
                errorStream = connection.errorStream
                if (errorStream == null) {
                    return null // connection did not respond with an error
                }
                val responseXml =
                    parse(
                        errorStream
                    )
                if (responseXml is OwsExceptionReport) responseXml else null
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
                null // silently ignore checked exceptions
            } finally {
                WWUtil.closeSilently(errorStream)
            }
        }
    }
    protected var ows20Namespace = "http://www.opengis.net/ows/2.0"

    protected var xmlNamespace = "http://www.w3.org/2001/XMLSchema"

    init {
        registerOws20Models(ows20Namespace)
        registerXmlModels(xmlNamespace)
    }

    protected fun registerOws20Models(namespace: String) {
        registerXmlModel(namespace, "Exception", OwsException::class.java)
        registerTxtModel(namespace, "exceptionCode")
        registerXmlModel(namespace, "ExceptionReport", OwsExceptionReport::class.java)
        registerTxtModel(namespace, "ExceptionText")
        registerTxtModel(namespace, "locator")
        registerTxtModel(namespace, "version")
    }

    protected fun registerXmlModels(namespace: String) {
        registerTxtModel(namespace, "lang")
    }

}