package com.atom.map.renderable.attribute

import com.atom.map.geom.Offset
import com.atom.map.geom.SimpleColor
import com.atom.map.renderable.ImageSource

class PlacemarkAttributes {

    companion object {
        private val attributes: PlacemarkAttributes =
            PlacemarkAttributes()
        fun defaults(attr: PlacemarkAttributes = attributes): PlacemarkAttributes {
            return PlacemarkAttributes(attr)
        }

        fun withImage(imageSource: ImageSource): PlacemarkAttributes {
            return defaults()
                .apply {
                this.imageSource = imageSource
            }
        }

        fun withImageAndLabel(imageSource: ImageSource): PlacemarkAttributes {
            return defaults()
                .apply {
                this.imageSource = imageSource
                this.drawLabel = true
            }
        }

        fun withImageAndLeaderLine(imageSource: ImageSource?): PlacemarkAttributes {
            return defaults()
                .apply {
                this.imageSource = imageSource
                this.drawLeader = true
            }
        }

        fun withImageAndLabelLeaderLine(imageSource: ImageSource): PlacemarkAttributes {
            return defaults()
                .apply {
                this.imageSource = imageSource
                this.drawLeader = true
                this.drawLabel = true
            }
        }
    }

    var imageSource: ImageSource? = null
    var imageColor: SimpleColor
    var imageOffset: Offset
    var imageScale: Double = 0.0

    var drawLabel: Boolean
    var labelAttributes: TextAttributes
    var drawLeader: Boolean
    var leaderAttributes: ShapeAttributes

    var minimumImageScale: Double = 0.0
    var depthTest: Boolean

    private constructor() {
        imageColor = SimpleColor(1f, 1f, 1f, 1f)
        imageScale = 1.0
        imageSource = null
        drawLeader = false
        drawLabel = false
        depthTest = true
        imageOffset = Offset(Offset.center())
        leaderAttributes =
            ShapeAttributes.defaults()
        labelAttributes = TextAttributes.defaults()
            .apply {
            this.textOffset = Offset.negate(imageOffset)
        }
    }

    fun imageOffset(offset: Offset) {
        imageOffset.set(offset)
        labelAttributes.textOffset = Offset.negate(offset)
    }

    private constructor(attributes: PlacemarkAttributes) {
        imageColor = SimpleColor(attributes.imageColor)
        imageOffset = Offset(attributes.imageOffset)
        imageScale = attributes.imageScale
        imageSource = attributes.imageSource
        minimumImageScale = attributes.minimumImageScale
        depthTest = attributes.depthTest
        drawLeader = attributes.drawLeader
        drawLabel = attributes.drawLabel
        labelAttributes =
            TextAttributes.defaults(
                attributes.labelAttributes
            )
        leaderAttributes =
            ShapeAttributes.defaults(
                attributes.leaderAttributes
            )

    }

    fun set(attributes: PlacemarkAttributes): PlacemarkAttributes {
        imageColor.set(attributes.imageColor)
        imageOffset.set(attributes.imageOffset)
        imageScale = attributes.imageScale
        imageSource = attributes.imageSource // TODO: resolve shallow or deep copy of imageSource
        depthTest = attributes.depthTest
        minimumImageScale = attributes.minimumImageScale
        drawLeader = attributes.drawLeader
        drawLabel = attributes.drawLabel
        labelAttributes =
            TextAttributes.defaults(
                attributes.labelAttributes
            )
        leaderAttributes =
            ShapeAttributes.defaults(
                attributes.leaderAttributes
            )
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
        return ((if (imageSource == null) that.imageSource == null else imageSource!! == that.imageSource)
                && imageColor == that.imageColor
                && imageOffset == that.imageOffset
                && imageScale == that.imageScale
                && minimumImageScale == that.minimumImageScale
                && drawLeader == that.drawLeader
                && drawLabel == that.drawLabel
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
        result = 31 * result + if (drawLabel) 1 else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + labelAttributes.hashCode()
        result = 31 * result + leaderAttributes.hashCode()
        return result
    }

}