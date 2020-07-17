package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.util.xml.XmlModel
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.xml.namespace.QName

class NameStringModelTest {
    companion object{
        const val TEST_ELEMENT_NAME = "MyImportantTagName"
    }
    @Test
    @Throws(Exception::class)
    fun testGetValue() {
        val namespace = ""
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
                        <EnclosingElement>
                            <$TEST_ELEMENT_NAME>
                                <SelfClosingTagWithAttrValuesNotAssociatedWithTest zoom="100"/>
                            </$TEST_ELEMENT_NAME>
                        </EnclosingElement>"""
        val `is`: InputStream = ByteArrayInputStream(xml.toByteArray())
        val ctx = XmlPullParserContext(namespace)
        ctx.setParserInput(`is`)
        ctx.registerParsableModel(
            QName(
                namespace,
                TEST_ELEMENT_NAME
            ), NameStringModel(namespace)
        )
        val xmlModel = XmlModel(namespace)
        xmlModel.read(ctx)
        val nameStringModel: NameStringModel = xmlModel.getField(
            QName(
                namespace,
                TEST_ELEMENT_NAME
            )
        ) as NameStringModel
        assertEquals(
            "Integer Value",
            TEST_ELEMENT_NAME,
            nameStringModel.getCharactersContent()
        )
    }
}