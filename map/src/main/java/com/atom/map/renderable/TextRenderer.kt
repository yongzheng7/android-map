package com.atom.map.renderable

import android.graphics.*
import com.atom.map.core.shader.GpuTexture
import com.atom.map.geom.SimpleColor
import com.atom.map.util.StringUtils
import com.atom.map.util.otherwise
import com.atom.map.util.yes


class TextRenderer {

    var textColor: SimpleColor = SimpleColor(Color.WHITE)

    var outlineColor: SimpleColor = SimpleColor(Color.BLACK)

    var textSize = 0f
        set(value) {
            field = value
            paint.textSize = value
        }

    var typeface: Typeface? = null
        set(value) {
            field = value
            paint.typeface = value
        }

    var enableOutline = false

    var outlineWidth = 0f
        set(value) {
            field = value
            paint.strokeWidth = value
        }

    var paint: Paint

    var canvas: Canvas

    var scratchBounds = Rect()

    constructor() {
        paint = Paint()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.LEFT
        canvas = Canvas()
        textSize = paint.textSize
        typeface = paint.typeface
        enableOutline = false
        outlineWidth = paint.strokeWidth
    }

    fun renderText(text: String): GpuTexture {
        val temp = text.isEmpty().yes {
            StringUtils.getRandomString(6)
        }.otherwise { text }
        val bitmap: Bitmap = this.drawText(temp)
        return GpuTexture(bitmap)
    }

    protected fun drawText(text: String): Bitmap {
        paint.getTextBounds(text, 0, text.length, scratchBounds)
        var x = -scratchBounds.left + 1
        var y = -scratchBounds.top + 1
        var width = scratchBounds.width() + 2
        var height = scratchBounds.height() + 2
        if (enableOutline) {
            val strokeWidth_2 = Math.ceil(paint.strokeWidth * 0.5f.toDouble()).toInt()
            x += strokeWidth_2
            y += strokeWidth_2
            width += strokeWidth_2 * 2
            height += strokeWidth_2 * 2
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        canvas.setBitmap(bitmap)
        if (enableOutline) {
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.color = this.outlineColor.toColorInt()
            canvas.drawText(text, 0, text.length, x.toFloat(), y.toFloat(), paint)
        }
        paint.style = Paint.Style.FILL
        paint.color = this.textColor.toColorInt()
        canvas.drawText(text, 0, text.length, x.toFloat(), y.toFloat(), paint)
        canvas.setBitmap(null)
        return bitmap
    }
}