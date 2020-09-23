package com.atom.wyz.worldwind.layer.render.attribute

import com.atom.wyz.worldwind.geom.Offset
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.layer.render.ImageSource

class PlacemarkAttributes {

    companion object {
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
            placemarkAttributes.drawLeader = true;
            return placemarkAttributes
        }

        fun withImageAndLabelLeaderLine(imageSource: ImageSource): PlacemarkAttributes {
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

    var imageSource: ImageSource? = null
    var imageColor: SimpleColor
    var imageOffset: Offset
    var imageScale = 0.0
    var labelAttributes: TextAttributes
    var drawLeader = false
    var leaderAttributes: ShapeAttributes
    var minimumImageScale = 0.0
    var depthTest = false

    constructor() {
        imageColor = SimpleColor(1f, 1f, 1f, 1f)
        imageOffset = Offset(Offset.center())
        imageScale = 1.0
        imageSource = null
        drawLeader = false
        depthTest = true
        labelAttributes = TextAttributes()
        leaderAttributes = ShapeAttributes()
    }

    constructor(attributes: PlacemarkAttributes) {
        imageColor = SimpleColor(attributes.imageColor)
        imageOffset = Offset(attributes.imageOffset)
        imageScale = attributes.imageScale
        imageSource = attributes.imageSource
        minimumImageScale = attributes.minimumImageScale
        depthTest = attributes.depthTest
        drawLeader = attributes.drawLeader

        labelAttributes = TextAttributes(attributes.labelAttributes)
        leaderAttributes = ShapeAttributes(attributes.leaderAttributes)

    }

    fun set(attributes: PlacemarkAttributes): PlacemarkAttributes {
        imageColor.set(attributes.imageColor)
        imageOffset.set(attributes.imageOffset)
        imageScale = attributes.imageScale
        imageSource = attributes.imageSource // TODO: resolve shallow or deep copy of imageSource
        depthTest = attributes.depthTest
        minimumImageScale = attributes.minimumImageScale
        drawLeader = attributes.drawLeader
        labelAttributes = TextAttributes(attributes.labelAttributes)
        leaderAttributes = ShapeAttributes(attributes.leaderAttributes)
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
                && imageColor == that.imageColor
                && imageOffset == that.imageOffset
                && imageScale == that.imageScale
                && minimumImageScale == that.minimumImageScale
                && drawLeader == that.drawLeader
                && depthTest == that.depthTest
                && (labelAttributes == that.labelAttributes)
                && leaderAttributes == that.leaderAttributes)
    }

    override fun hashCode(): Int {
        var result: Int = if (imageSource != null) imageSource.hashCode() else 0
        result = 31 * result + imageColor.hashCode()
        result = 31 * result + imageOffset.hashCode()
        var temp: Long = imageScale.toBits()
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = minimumImageScale.toBits()
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + if (drawLeader) 1 else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + labelAttributes.hashCode()
        result = 31 * result + leaderAttributes.hashCode()
        return result
    }

}