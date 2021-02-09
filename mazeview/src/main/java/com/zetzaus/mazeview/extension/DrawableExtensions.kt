package com.zetzaus.mazeview.extension

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

/**
 *  Returns a [Bitmap] from an image that is retrieved from the resource file. The image
 *  will be scaled using billinear filtering.
 *
 * @param width The target width.
 * @param height The target height.
 * @return
 */
fun Drawable.toScaledBitmap(width: Int, height: Int): Bitmap {
    val bitmap = toBitmap(config = Bitmap.Config.ARGB_8888)
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}
