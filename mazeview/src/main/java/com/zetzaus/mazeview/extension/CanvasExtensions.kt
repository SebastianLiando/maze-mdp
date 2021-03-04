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

/**
 * Draws a rotated bitmap.
 *
 * @param bitmap The image.
 * @param centerX The center x position.
 * @param centerY The center y position
 * @param degree The rotation in degree.
 * @param paint The paint.
 */
internal fun Canvas.drawBitmapWithRotation(
    bitmap: Bitmap,
    centerX: Float,
    centerY: Float,
    degree: Float,
    paint: Paint
) {
    val rotationMatrix = Matrix().apply {
        reset()
        postTranslate(-bitmap.width.toFloat() / 2f, -bitmap.height.toFloat() / 2f)
        postRotate(degree)
        postTranslate(centerX, centerY)
    }

    drawBitmap(bitmap, rotationMatrix, paint)
}

/**
 * Draws text that are centered vertically and horizontally.
 *
 * @param text The text to draw.
 * @param cx The center x coordinate.
 * @param cy The center y coordinate.
 * @param paint The paint to draw the text.
 * @param bound The rect to be used to store text boundaries.
 */
internal fun Canvas.drawTextCentered(
    text: String,
    cx: Float,
    cy: Float,
    paint: Paint,
    bound: Rect
) {
    val centeredPaint = paint.apply { textAlign = Paint.Align.CENTER }
    centeredPaint.getTextBounds(text, 0, text.length, bound)

    drawText(text, cx, cy - bound.exactCenterY(), centeredPaint)
}