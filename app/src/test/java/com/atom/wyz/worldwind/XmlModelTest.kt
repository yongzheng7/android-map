package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.util.xml.XmlModel
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import javax.xml.namespace.QName

class XmlModelTest {
    companion object{
        const val NAMESPACE = ""

        const val DOUBLE_VALUE = 3.14159243684

        const val DELTA = 1e-9

        const val XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<LevelOne levelone=\"false\">\n" +
                "\t<LevelTwoA leveltwo=\"2\">\n" +
                "\t\t<LevelThreeA levelthree=\"" + DOUBLE_VALUE + "\"/>\n" +
                "\t</LevelTwoA>\n" +
                "\t<LevelTwoB leveltwo=\"" + DOUBLE_VALUE + "\">\n" +
                "\t\t<LevelThreeB levelthree=\"testthreeb\">\n" +
                "\t\t\tLevel Three B\n" +
                "\t\t</LevelThreeB>\n" +
                "\t</LevelTwoB>\n" +
                "\t<LevelTwoC>\n" +
                "\t\t<LevelThreeC>\n" +
                "\t\t\t<Value>A_AAA</Value>\n" +
                "\t\t\t<Value>B_AAA</Value>\n" +
                "\t\t\t<Value>C_AAA</Value>\n" +
                "\t\t\t<LevelFourD>\n" +
                "\t\t\t\t<Value>A_BBB</Value>\n" +
                "\t\t\t\t<Value>B_BBB</Value>\n" +
                "\t\t\t\t<LevelFive levelfive=\"true\"/>\n" +
                "\t\t\t</LevelFourD>\n" +
                "\t\t</LevelThreeC>\n" +
                "\t</LevelTwoC>\n" +
                "</LevelOne>"

        /**
         * This class mimics the functionality required when mutiple elements with an identical field are used in a
         * document.
         */
        protected class MultipleEntryElement(namespaceUri: String?) : XmlModel(namespaceUri) {
           override fun setField(keyName: QName, value: Any?) {
                if (keyName == NAME) {
                    var values =
                        this.getField(NAME) as MutableSet<String>?
                    if (values == null) {
                        values = HashSet()
                        super.setField(NAME, values)
                    }
                    values.add((value as XmlModel).getCharactersContent().toString())
                } else {
                    super.setField(keyName, value)
                }
            }

            companion object {
                val NAME =
                    QName(NAMESPACE, "Value")
            }
        }
    }

    lateinit var context: XmlPullParserContext

    lateinit var root: XmlModel

    @Before
    @Throws(Exception::class)
    fun setup() {
        context = XmlPullParserContext(NAMESPACE)
        context.registerParsableModel(
            QName(
                NAMESPACE,
                "LevelThreeC"
            ), MultipleEntryElement(NAMESPACE)
        )
        context.registerParsableModel(
            QName(
                NAMESPACE,
                "LevelFourD"
            ), MultipleEntryElement(NAMESPACE)
        )
        val `is`: InputStream = ByteArrayInputStream(XML.toByteArray())
        context.setParserInput(`is`)
        this.root = XmlModel(NAMESPACE)
        this.root.read(context)
    }

    @Test
    fun testGetInheritedField() {

        // Get the leaf node for which to query for inherited values
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoC"
            )
        ) as XmlModel
        // Expected Values
        val levelThreeExpectedValues: MutableSet<String> =
            HashSet()
        levelThreeExpectedValues.addAll(
            Arrays.asList(
                "A_AAA",
                "B_AAA",
                "C_AAA"
            )
        )
        val levelFourAndFiveExpectedValues: MutableSet<String> =
            HashSet()
        levelFourAndFiveExpectedValues.addAll(
            Arrays.asList(
                "A_BBB",
                "B_BBB"
            )
        )

        // LevelThreeC includes its own values for the Value element
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeC"
            )
        ) as XmlModel
        val levelThreeInheritedValues =
            model.getInheritedField(MultipleEntryElement.NAME) as Set<String>?

        // LevelFourD contains Value elements and should not have inherited ones
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelFourD"
            )
        ) as XmlModel
        val levelFourInheritedValues =
            model.getInheritedField(MultipleEntryElement.NAME) as Set<String>?

        // LevelFive contains no Value elements but should inherited the Values in only the preceding level
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelFive"
            )
        ) as XmlModel
        val levelFiveInheritedValues =
            model.getInheritedField(MultipleEntryElement.NAME) as Set<String>?
        Assert.assertEquals(
            "Only present value no inheritance",
            levelThreeExpectedValues,
            levelThreeInheritedValues
        )
        Assert.assertEquals(
            "Inherited with present values",
            levelFourAndFiveExpectedValues,
            levelFourInheritedValues
        )
        Assert.assertEquals(
            "Inherited with no own values",
            levelFourAndFiveExpectedValues,
            levelFiveInheritedValues
        )
    }

    @Test
    fun testGetAdditiveInheritedField() {

        // Get the leaf node for which to query for inherited values
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoC"
            )
        ) as XmlModel
        // Expected Values
        val levelThreeExpectedValues: MutableSet<String> =
            HashSet()
        levelThreeExpectedValues.addAll(
            Arrays.asList(
                "A_AAA",
                "B_AAA",
                "C_AAA"
            )
        )
        val levelFourAndFiveExpectedValues: MutableSet<String> =
            HashSet()
        levelFourAndFiveExpectedValues.addAll(
            Arrays.asList(
                "A_BBB",
                "B_BBB",
                "A_AAA",
                "B_AAA",
                "C_AAA"
            )
        )

        // LevelThreeC includes its own values for the Value element
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeC"
            )
        ) as XmlModel
        val levelThreeInheritedValues: MutableSet<String> = HashSet()
        model.getAdditiveInheritedField(MultipleEntryElement.NAME, levelThreeInheritedValues)

        // LevelFourD contains Value elements and should have inherited ones
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelFourD"
            )
        ) as XmlModel
        val levelFourInheritedValues: MutableSet<String> =
            HashSet()
        model.getAdditiveInheritedField(MultipleEntryElement.NAME, levelFourInheritedValues)

        // LevelFive contains no Value elements but should inherited the Values in the preceding levels
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelFive"
            )
        ) as XmlModel
        val levelFiveInheritedValues: MutableSet<String> =
            HashSet()
        model.getAdditiveInheritedField(MultipleEntryElement.NAME, levelFiveInheritedValues)
        Assert.assertEquals(
            "Only present value no inheritance",
            levelThreeExpectedValues,
            levelThreeInheritedValues
        )
        Assert.assertEquals(
            "Inherited with present values",
            levelFourAndFiveExpectedValues,
            levelFourInheritedValues
        )
        Assert.assertEquals(
            "Inherited with no own values",
            levelFourAndFiveExpectedValues,
            levelFiveInheritedValues
        )
    }

    @Test
    fun testGetCharactersContent() {

        // Get the leaf node
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoB"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeB"
            )
        ) as XmlModel
        val expectedValue = "Level Three B"
        val actualValue: String = model.getCharactersContent()!!
        Assert.assertEquals("Character Values", expectedValue, actualValue)
    }

    @Test
    fun testGetChildCharacterContent() {

        // Get the leaf node
        val model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoB"
            )
        ) as XmlModel
        val expectedValue = "Level Three B"
        val actualValue: String = model.getChildCharacterValue(
            QName(
                NAMESPACE,
                "LevelThreeB"
            )
        )!!
        Assert.assertEquals("Character Values", expectedValue, actualValue)
    }

    @Test
    fun testGetDoubleAttributeValue_NoInheritance() {

        // Get the leaf node
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoA"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeA"
            )
        ) as XmlModel
        val actualValue: Double =
            model.getDoubleAttributeValue(QName("", "levelthree"), false)!!
        Assert.assertEquals(
            "Not Inherited Double Attribute Value",
            DOUBLE_VALUE,
            actualValue,
            DELTA
        )
    }

    @Test
    fun testGetDoubleAttributeValue_Inheritance() {

        // Get the leaf node
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoB"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeB"
            )
        ) as XmlModel
        val actualValue: Double =
            model.getDoubleAttributeValue(QName("", "leveltwo"), true)!!
        Assert.assertEquals(
            "Not Inherited Double Attribute Value",
            DOUBLE_VALUE,
            actualValue,
            DELTA
        )
    }

    @Test
    fun testGetIntegerAttributeValue_NoInheritance() {

        // Get the leaf node
        val model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoA"
            )
        ) as XmlModel
        val actualValue: Int =
            model.getIntegerAttributeValue(QName("", "leveltwo"), false)!!
        Assert.assertEquals("Not Inherited Double Attribute Value", 2, actualValue)
    }

    @Test
    fun testGetIntegerAttributeValue_Inheritance() {

        // Get the leaf node
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoA"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeA"
            )
        ) as XmlModel
        val actualValue: Int =
            model.getIntegerAttributeValue(QName("", "leveltwo"), true)!!
        Assert.assertEquals("Not Inherited Double Attribute Value", 2, actualValue)
    }

    @Test
    fun testGetBooleanAttributeValue_NoInheritance() {

        // Get the leaf node
        var model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoC"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelThreeC"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelFourD"
            )
        ) as XmlModel
        model = model.getField(
            QName(
                NAMESPACE,
                "LevelFive"
            )
        ) as XmlModel
        val actualValue: Boolean =
            model.getBooleanAttributeValue(QName("", "levelfive"), false)!!
        Assert.assertEquals(
            "Not Inherited Boolean Attribute Value",
            true,
            actualValue
        )
    }

    @Test
    fun testGetBooleanAttributeValue_Inheritance() {

        // Get the leaf node
        val model: XmlModel = this.root.getField(
            QName(
                NAMESPACE,
                "LevelTwoA"
            )
        ) as XmlModel
        val actualValue: Boolean =
            model.getBooleanAttributeValue(QName("", "levelone"), true)!!
        Assert.assertEquals(
            "Not Inherited Double Attribute Value",
            false,
            actualValue
        )
    }
}