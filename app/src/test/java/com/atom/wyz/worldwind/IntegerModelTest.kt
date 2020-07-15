package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.util.xml.IntegerModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class IntegerModelTest {

    companion object{
        const val TEST_ELEMENT_NAME = "MyIntegerValue"
    }

    @Test
    @Throws(Exception::class)
    fun testGetValue() {
        val namespace = ""
        val elementValue = 24601
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
                    <$TEST_ELEMENT_NAME>
                        $elementValue
                    </$TEST_ELEMENT_NAME>"""
        val `is`: InputStream = ByteArrayInputStream(xml.toByteArray())
        val ctx = XmlPullParserContext(namespace)
        ctx.setParserInput(`is`)
        val integerModel = IntegerModel(namespace)
        integerModel.read(ctx)
        assertEquals("Integer Value", elementValue, integerModel.getValue())
    }
}