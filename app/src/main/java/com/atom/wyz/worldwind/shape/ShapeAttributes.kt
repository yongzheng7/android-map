package com.atom.wyz.worldwind.shape

import com.atom.wyz.worldwind.geom.Color

class ShapeAttributes {

    var drawInterior = false

    var drawOutline = false

    var enableLighting = false
    /**
     * 指示相关形状的内部颜色和不透明度。
     */
    var interiorColor: Color? = null
    /**
     * 指示关联的形状的轮廓颜色和不透明度。
     */
    var outlineColor: Color? = null
    /**
     * 指示关联的形状的轮廓颜色和不透明度。
     */
    var outlineWidth = 0f
    /**
     * 指示相关形状的轮廓点画因子。
     * 指定轮廓点画模式中的每个位在使用下一位之前被重复的次数。
     * 例如，如果轮廓点画因子为3，则在使用下一位之前，将每个位重复3次。
     * 指定的因子必须为0或大于0的整数。点画因子0表示没有点画。
     */
    var outlineStippleFactor = 0
    /**
     * 指示相关形状的轮廓点画图案。
     * 指定一个数字，该数字的低16位定义一个图案，该图案的轮廓中的哪些像素被渲染，哪些像素被抑制。
     * 每一位对应于形状轮廓中的一个像素，并且图案每隔n * 16个像素重复一次，
     * 其中n是[点画因子] {@ link ShapeAttributes＃outlineStippleFactor}。
     * 例如，如果轮廓点画因子为3，则点画样式中的每个位在使用下一位之前要重复3次。
     * <p />要禁用轮廓点画，请指定点画因子0或指定全部1位的点画模式，即0xFFFF。
     */
    var outlineStipplePattern: Short = 0
    /**
     * 指示关联的形状的图像源。 可以为null，在这种情况下，没有图像应用于形状。
     */
    var imageSource: Any? = null
    /**
     * 指示是否应针对场景中的其他对象对形状进行深度测试。
     * 如果为true，则在某些观看情况下，地形和其他对象可能会遮挡形状。 如果为false，则形状不会被地形和其他对象遮挡。
     */
    var depthTest = false
    /**
     * 指示此形状是否应绘制从其指定位置延伸到地面的垂直线。
     */
    var drawVerticals = false
    /**
     * 指示是否将照明应用于形状。
     */
    var applyLighting = false

    constructor() {
        drawInterior = true
        drawOutline = true
        enableLighting = false
        interiorColor = Color(Color.WHITE)
        outlineColor = Color( Color.RED)
        outlineWidth = 1.0f
        outlineStippleFactor = 0
        outlineStipplePattern = 0xF0F0.toShort()
        imageSource = null
        depthTest = true
        drawVerticals = false
        applyLighting = false
    }

    constructor(copy: ShapeAttributes) {
        drawInterior = copy.drawInterior
        drawOutline = copy.drawOutline
        enableLighting = copy.enableLighting
        interiorColor = copy.interiorColor?.let { Color(it) }
        outlineColor = copy.outlineColor?.let { Color(it) }
        outlineWidth = copy.outlineWidth
        outlineStippleFactor = copy.outlineStippleFactor
        outlineStipplePattern = copy.outlineStipplePattern
        imageSource = copy.imageSource
        depthTest = copy.depthTest
        drawVerticals = copy.drawVerticals
        applyLighting = copy.applyLighting
    }

    fun set(attributes: ShapeAttributes): ShapeAttributes {
        imageSource = attributes.imageSource
        attributes.interiorColor?.let { interiorColor!!.set(it) }
        attributes.outlineColor?.let { outlineColor!!.set(it) }
        drawInterior = attributes.drawInterior
        drawOutline = attributes.drawOutline
        enableLighting = attributes.enableLighting
        outlineWidth = attributes.outlineWidth
        outlineStippleFactor = attributes.outlineStippleFactor
        outlineStipplePattern = attributes.outlineStipplePattern
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
        applyLighting = attributes.applyLighting
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that: ShapeAttributes = o as ShapeAttributes
        if (drawInterior != that.drawInterior) return false
        if (drawOutline != that.drawOutline) return false
        if (enableLighting != that.enableLighting) return false
        if (java.lang.Double.compare(that.outlineWidth.toDouble(), outlineWidth.toDouble()) != 0) return false
        if (outlineStippleFactor != that.outlineStippleFactor) return false
        if (outlineStipplePattern != that.outlineStipplePattern) return false
        if (depthTest != that.depthTest) return false
        if (drawVerticals != that.drawVerticals) return false
        if (applyLighting != that.applyLighting) return false
        if (if (interiorColor != null) interiorColor != that.interiorColor else that.interiorColor != null) return false
        return if (if (outlineColor != null) outlineColor != that.outlineColor else that.outlineColor != null) false else !if (imageSource != null) imageSource != that.imageSource else that.imageSource != null
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = if (drawInterior) 1 else 0
        result = 31 * result + if (drawOutline) 1 else 0
        result = 31 * result + if (enableLighting) 1 else 0
        result = 31 * result + if (interiorColor != null) interiorColor.hashCode() else 0
        result = 31 * result + if (outlineColor != null) outlineColor.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(outlineWidth.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + outlineStippleFactor
        result = 31 * result + outlineStipplePattern.toInt()
        result = 31 * result + if (imageSource != null) imageSource.hashCode() else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + if (drawVerticals) 1 else 0
        result = 31 * result + if (applyLighting) 1 else 0
        return result
    }
}