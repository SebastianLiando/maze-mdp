package com.zetzaus.mazeview.extension

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

@Throws(IllegalArgumentException::class)
fun Context.getDrawableOrThrow(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
    ?: throw IllegalArgumentException("Id $id does not exist!")

/**
 * Returns the color that is relevant to the current theme.
 *
 * @param id The color id. Theme relevant color ids can be found in [R.attr.*].
 * @return The color.
 */
fun Context.getThemeColor(@AttrRes id: Int): Int {
    val typed = TypedValue()

    val attributes = obtainStyledAttributes(typed.data, IntArray(1) { id })
    val color = attributes.getColor(0, 0)

    attributes.recycle()

    return color
}

/**
 * Returns the image.
 *
 * @param id The drawable resource id.
 * @param size The size of the image.
 *
 * @return The image.
 */
fun Context.getBitmap(@DrawableRes id: Int, size: Int): Bitmap {
    return getDrawableOrThrow(id).toScaledBitmap(size, size)
}