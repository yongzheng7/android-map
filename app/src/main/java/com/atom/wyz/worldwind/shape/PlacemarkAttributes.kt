package com.atom.wyz.worldwind.shape

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
            val placemarkAttributes = PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            return placemarkAttributes
        }

        fun withImageAndLabel(imageSource: ImageSource?): PlacemarkAttributes {
            val placemarkAttributes = PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            placemarkAttributes.labelAttributes = TextAttributes()
            return placemarkAttributes
        }

        fun withImageAndLeaderLine(imageSource: ImageSource?): PlacemarkAttributes {
            val placemarkAttributes = PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            placemarkAttributes.leaderAttributes = ShapeAttributes()
            placemarkAttributes.drawLeader = true  ;
            return placemarkAttributes
        }

        fun withImageAndLabelLeaderLine(imageSource : ImageSource ): PlacemarkAttributes {
            val placemarkAttributes = PlacemarkAttributes()
            placemarkAttributes.imageSource = imageSource
            placemarkAttributes.labelAttributes = TextAttributes()
            placemarkAttributes.leaderAttributes = ShapeAttributes()
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
        imageOffset = Offset(Offset.CENTER)
        imageScale = 1.0
        imageSource = null
        labelAttributes = TextAttributes()
        leaderAttributes = ShapeAttributes()
        drawLeader = false
        depthTest = true
    }

    constructor(copy: PlacemarkAttributes) {
        imageColor = Color(copy.imageColor!!)
        imageOffset = Offset(copy.imageOffset!!)
        imageScale = copy.imageScale
        imageSource = copy.imageSource
        minimumImageScale = copy.minimumImageScale
        depthTest = copy.depthTest
        copy.labelAttributes ?.let { labelAttributes = TextAttributes(it) } ?:let { labelAttributes = null }

        drawLeader = copy.drawLeader
        copy.leaderAttributes ?.let { leaderAttributes = ShapeAttributes(it) } ?:let { leaderAttributes = null }

    }

    fun set(attributes: PlacemarkAttributes?): PlacemarkAttributes{
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
        if (attributes.labelAttributes != null) {
            if (labelAttributes == null) {
                labelAttributes = TextAttributes(attributes.labelAttributes!!)
            } else {
                labelAttributes!!.set(attributes.labelAttributes!!)
            }
        } else {
            labelAttributes = null
        }

        drawLeader = attributes.drawLeader
        if (attributes.leaderAttributes != null) {
            if (leaderAttributes == null) {
                leaderAttributes = ShapeAttributes(attributes.leaderAttributes!!)
            } else {
                leaderAttributes!!.set(attributes.leaderAttributes!!)
            }
        } else {
            leaderAttributes = null
        }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that: PlacemarkAttributes = other as PlacemarkAttributes
        if (java.lang.Double.compare(that.imageScale, imageScale) != 0) return false
        if (depthTest != that.depthTest) return false
        if (drawLeader != that.drawLeader) return false
        if (if (imageColor != null) imageColor != that.imageColor else that.imageColor != null) return false
        if (if (imageOffset != null) imageOffset != that.imageOffset else that.imageOffset != null) return false
        if (if (imageSource != null) imageSource != that.imageSource else that.imageSource != null) return false
        return if (if (labelAttributes != null) labelAttributes != that.labelAttributes else that.labelAttributes != null) false else !if (leaderAttributes != null) leaderAttributes != that.leaderAttributes else that.leaderAttributes != null
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = if (imageColor != null) imageColor.hashCode() else 0
        result = 31 * result + if (imageOffset != null) imageOffset.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(imageScale)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + if (imageSource != null) imageSource.hashCode() else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + if (labelAttributes != null) labelAttributes.hashCode() else 0
        result = 31 * result + if (drawLeader) 1 else 0
        result = 31 * result + if (leaderAttributes != null) leaderAttributes.hashCode() else 0
        return result
    }

}