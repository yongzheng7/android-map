package com.atom.wyz.worldwind

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class DoubleModelTest {

    companion object{
        const val TEST_ELEMENT_NAME = "MyDoubleValue"

        const val DELTA = 1e-9
    }

    @Test
    @Throws(Exception::class)
    fun testGetValue() {
        val namespace = ""
        val elementValue = 3.14159018
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
                    <$TEST_ELEMENT_NAME>
                        $elementValue
                    </$TEST_ELEMENT_NAME>"""
        val `is`: InputStream = ByteArrayInputStream(xml.toByteArray())
        val ctx = XmlPullParserContext(namespace)
        ctx.setParserInput(`is`)
        val doubleModel = DoubleModel(namespace)
        doubleModel.read(ctx)
        assertEquals("Double Value", elementValue, doubleModel.getValue()!!, DELTA)
    }
}