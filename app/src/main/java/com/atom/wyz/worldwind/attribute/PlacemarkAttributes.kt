package com.atom.wyz.worldwind.attribute

import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Offset
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.util.Logger

class PlacemarkAttributes {

    companion object{
        fun defaults(): PlacemarkAttributes {
            return PlacemarkAttributes()
        }

        fun withImage(imageSource: ImageSource?): PlacemarkAttributes {
            val placemarkAttributes =
                PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            return placemarkAttributes
        }

        fun withImageAndLabel(imageSource: ImageSource?): PlacemarkAttributes {
            val placemarkAttributes =
                PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            placemarkAttributes.labelAttributes =
                TextAttributes()
            return placemarkAttributes
        }

        fun withImageAndLeaderLine(imageSource: ImageSource?): PlacemarkAttributes {
            val placemarkAttributes =
                PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            placemarkAttributes.leaderAttributes =
                ShapeAttributes()
            placemarkAttributes.drawLeader = true  ;
            return placemarkAttributes
        }

        fun withImageAndLabelLeaderLine(imageSource : ImageSource): PlacemarkAttributes {
            val placemarkAttributes =
                PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            placemarkAttributes.labelAttributes =
                TextAttributes()
            placemarkAttributes.leaderAttributes =
                ShapeAttributes()
            placemarkAttributes.drawLeader = true
            return placemarkAttributes
        }
    }

    var imageColor: Color? = null
    var imageOffset: Offset? = null
    var imageScale = 0.0
    var imageSource: ImageSource? = null

    var labelAttributes: TextAttributes? = null

    var drawLeader = false
    var leaderAttributes: ShapeAttributes? = null

    var minimumImageScale = 0.0

    var depthTest = false

    constructor() {
        imageColor = Color(1f, 1f, 1f, 1f)
        imageOffset = Offset(Offset.center())
        imageScale = 1.0
        imageSource = null
        drawLeader = false
        depthTest = true
        labelAttributes = TextAttributes()
        leaderAttributes = ShapeAttributes()
    }

    constructor(attributes: PlacemarkAttributes) {
        imageColor = Color(attributes.imageColor!!)
        imageOffset = Offset(attributes.imageOffset!!)
        imageScale = attributes.imageScale
        imageSource = attributes.imageSource
        minimumImageScale = attributes.minimumImageScale
        depthTest = attributes.depthTest
        attributes.labelAttributes ?.let { labelAttributes =
            TextAttributes(it)
        } ?:let { labelAttributes = null }

        drawLeader = attributes.drawLeader
        attributes.leaderAttributes ?.let { leaderAttributes =
            ShapeAttributes(it)
        } ?:let { leaderAttributes = null }

    }

    fun set(attributes: PlacemarkAttributes?): PlacemarkAttributes {
        if (attributes == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "set", "missingAttributes"))
        }
        attributes.imageColor?.let { imageColor!!.set(it) }
        attributes.imageOffset?.let { imageOffset!!.set(it) }
        imageScale = attributes.imageScale
        imageSource = attributes.imageSource // TODO: resolve shallow or deep copy of imageSource
        depthTest = attributes.depthTest
        minimumImageScale = attributes.minimumImageScale
        drawLeader = attributes.drawLeader
        if (attributes.labelAttributes != null) {
            if (labelAttributes == null) {
                labelAttributes =
                    TextAttributes(attributes.labelAttributes!!)
            } else {
                labelAttributes!!.set(attributes.labelAttributes!!)
            }
        } else {
            labelAttributes = null
        }
        if (attributes.leaderAttributes != null) {
            if (leaderAttributes == null) {
                leaderAttributes =
                    ShapeAttributes(attributes.leaderAttributes!!)
            } else {
                leaderAttributes!!.set(attributes.leaderAttributes!!)
            }
        } else {
            leaderAttributes = null
        }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that = other as PlacemarkAttributes
        return ((if (imageSource == null) that.imageSource == null else imageSource!!.equals(that.imageSource))
                && imageColor!!.equals(that.imageColor)
                && imageOffset!!.equals(that.imageOffset)
                && imageScale == that.imageScale
                && minimumImageScale == that.minimumImageScale
                && drawLeader == that.drawLeader
                && depthTest == that.depthTest
                && (if (labelAttributes == null) that.labelAttributes == null else labelAttributes!!.equals(that.labelAttributes))
                && if (leaderAttributes == null) that.leaderAttributes == null else leaderAttributes!!.equals(that.leaderAttributes))
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        result = if (imageSource != null) imageSource.hashCode() else 0
        result = 31 * result + imageColor.hashCode()
        result = 31 * result + imageOffset.hashCode()
        temp = java.lang.Double.doubleToLongBits(imageScale)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(minimumImageScale)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + if (drawLeader) 1 else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + if (labelAttributes != null) labelAttributes.hashCode() else 0
        result = 31 * result + if (leaderAttributes != null) leaderAttributes.hashCode() else 0
        return result
    }

}