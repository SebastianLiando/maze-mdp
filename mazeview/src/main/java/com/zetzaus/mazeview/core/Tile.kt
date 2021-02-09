package com.zetzaus.mazeview.core

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.zetzaus.mazeview.extension.getDrawableOrThrow
import com.zetzaus.mazeview.extension.toScaledBitmap

/**
 * Is a cell in the maze.
 *
 */
sealed class Tile {
    /**
     * Represents a fully colored tile.
     * This can be used to represent unexplored, explored, and obstacle tiles.
     *
     * @property color The tile color.
     */
    data class SolidTile(val color: Int) : Tile()

    /** Represents the current robot position. */
    /**
     * Represents the current robot position.
     *
     * @property robotColor The robot indicator color.
     * @property backgroundColor The tile color.
     */
    data class RobotTile(val robotColor: Int, val backgroundColor: Int) : Tile()

    /**
     * Represents the tile with an image on top.
     *
     * @property imageId The image id.
     * @property backgroundColor The tile color.
     */
    data class BitmapTile(@DrawableRes val imageId: Int, val backgroundColor: Int) : Tile() {

        /**
         * Returns the image.
         *
         * @param context The context.
         * @param size The size of the image.
         * @return The image.
         */
        fun getBitmap(context: Context, size: Int): Bitmap {
            return context.getDrawableOrThrow(imageId).toScaledBitmap(size, size)
        }
    }
}
