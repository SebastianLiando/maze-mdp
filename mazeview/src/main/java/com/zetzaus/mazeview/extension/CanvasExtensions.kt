package com.zetzaus.mazeview.extension

import android.graphics.*

/**
 * Draws a rectangle with a border around it according to [borderPaint].
 *
 * Some properties for border:
 *
 * `color` sets the border color.
 *
 * `style` needs to be [Paint.Style.STROKE].
 *
 * `strokeWidth` sets the border width.
 *
 * @param rect The rectangle coordinates.
 * @param rectPaint The paint for the solid rectangle.
 * @param borderPaint The paint for the border.
 */
internal fun Canvas.drawBorderedRect(rect: Rect, rectPaint: Paint, borderPaint: Paint) {
    drawRect(rect, rectPaint)
    drawRect(rect, borderPaint.apply { style = Paint.Style.STROKE })
}

internal fun Canvas.drawBitmapWithRotation(bitmap: Bitmap, centerX: Float, centerY: Float, degree: Float, paint: Paint) {
    val rotationMatrix = Matrix().apply {
        reset()
        postTranslate(-bitmap.width.toFloat() / 2f, -bitmap.height.toFloat() / 2f)
        postRotate(degree)
        postTranslate(centerX, centerY)
    }

    drawBitmap(bitmap, rotationMatrix, paint)
}