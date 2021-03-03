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

    /**
     * Represents the tile with an image on top.
     *
     * @property imageId The image id.
     * @property backgroundColor The tile color.
     */
    data class BitmapTile(@DrawableRes val imageId: Int, val backgroundColor: Int) : Tile()
}
