package com.zetzaus.maze.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

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
fun Canvas.drawBorderedRect(rect: Rect, rectPaint: Paint, borderPaint: Paint) {
    drawRect(rect, rectPaint)
    drawRect(rect, borderPaint.apply { style = Paint.Style.STROKE })
}