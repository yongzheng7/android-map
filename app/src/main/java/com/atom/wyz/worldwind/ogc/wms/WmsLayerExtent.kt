package com.atom.wyz.worldwind.ogc.wms

import android.util.Log
import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsLayerExtent(namespaceURI: String?) : XmlModel(namespaceURI) {

    companion object{
        const val DEFAULT_ATTRIBUTE_NAMESPACE = ""

        val NAME =
            QName(DEFAULT_ATTRIBUTE_NAMESPACE, "name")

        val DEFAULT =
            QName(DEFAULT_ATTRIBUTE_NAMESPACE, "default")

        val MULTIPLE_VALUES =
            QName(DEFAULT_ATTRIBUTE_NAMESPACE, "multipleValues")

        val NEAREST_VALUES =
            QName(DEFAULT_ATTRIBUTE_NAMESPACE, "nearestValues")

        val CURRENT =
            QName(DEFAULT_ATTRIBUTE_NAMESPACE, "current")
    }


    fun getExtent(): String? {
        return this.getField(CHARACTERS_CONTENT).toString()
    }

    fun  getName() : String? {
        return this.getField(NAME).toString()
    }

    fun  setName(name:String ) {
        this.setField(NAME, name)
    }

    fun getDefaultValue() :String{
        return  this.getField(DEFAULT).toString()
    }

    fun  setDefaultValue(defaultValue:String ) {
        this.setField(DEFAULT, defaultValue);
    }

    fun  isNearestValue():Boolean {
        val value = this.getField(NEAREST_VALUES) ?.toString() ?: return false
        try {
            val numericalValue = value.toInt()
            return numericalValue == 1
        } catch (e:NumberFormatException ) {
            Log.d("gov.nasa.worldwind", e.toString());
        }
        return false;
    }

    fun setNearestValue (nearestValue:Boolean ) {
        this.setField(NEAREST_VALUES, "1");
    }
}