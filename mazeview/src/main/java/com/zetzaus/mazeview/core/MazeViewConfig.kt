package com.zetzaus.mazeview.core

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.zetzaus.mazeview.R

private const val DEFAULT_COL_COUNT = 1
private const val DEFAULT_ROW_COUNT = 1

private const val DEFAULT_SCALE_FACTOR = 0.7f
private const val DEFAULT_TEXT_SCALE_FACTOR = 0.5f

private const val DEFAULT_ROBOT_COLOR = Color.BLACK
private const val DEFAULT_TEXT_COLOR = Color.CYAN

private const val DEFAULT_DIAMETER_SIZE = 1

private const val DEFAULT_BORDER_WIDTH = 4f
private const val DEFAULT_BORDER_COLOR = Color.WHITE

private const val DEFAULT_RING_WIDTH = 10f
private const val DEFAULT_RING_SIZE_MULTIPLIER = 3f

private val DEFAULT_ORIENTATION_INDICATOR = R.drawable.ic_default_orientation_pointer

private const val DEFAULT_MOVE_ANIMATION_DURATION = 500
private const val DEFAULT_RING_ANIMATION_DURATION = 1500

private const val DEFAULT_COORDINATE_ENABLED = false

/**
 * Contains maze configuration information.
 *
 * @property rowCount The number of rows in the maze.
 * @property columnCount The number of columns in the maze.
 * @property entityScaleFactor The proportion between the maze cell and the entity inside it.
 * Entity refers to the robot and the image.
 * @property indicatorScaleFactor The proportion between the robot circle and the orientation image inside it.
 * @property coordinateTextScaleFactor The proportion between the maze cell and the text inside it.
 * @property robotColor The robot circle color.
 * @property coordinateTextColor The coordinate text color.
 * @property robotDiameterCellSize The amount of cells occupied to the robot indicator's diameter.
 * @property borderWidth The width of the border.
 * @property borderColor The color of the border.
 * @property ringWidth The thickness of the ring.
 * @property ringSizeMultiplier Determines the maximum size the ring can increase.
 * @property orientationIndicatorImageId The drawable resource id for the orientation indicator.
 * @property moveAnimationDuration The duration of robot movement animation.
 * @property ringAnimationDuration The duration of robot ring animation.
 * @property isCoordinateEnabled Whether to display the cell coordinates.
 */
data class MazeViewConfig(
    var rowCount: Int = DEFAULT_ROW_COUNT,
    var columnCount: Int = DEFAULT_COL_COUNT,

    var entityScaleFactor: Float = DEFAULT_SCALE_FACTOR,
    var indicatorScaleFactor: Float = DEFAULT_SCALE_FACTOR,
    var coordinateTextScaleFactor: Float = DEFAULT_TEXT_SCALE_FACTOR,

    var robotColor: Int = DEFAULT_ROBOT_COLOR,
    var coordinateTextColor: Int = DEFAULT_TEXT_COLOR,

    var robotDiameterCellSize: Int = DEFAULT_DIAMETER_SIZE,

    var borderWidth: Float = DEFAULT_BORDER_WIDTH,
    var borderColor: Int = DEFAULT_BORDER_COLOR,

    var ringWidth: Float = DEFAULT_RING_WIDTH,
    var ringSizeMultiplier: Float = DEFAULT_RING_SIZE_MULTIPLIER,

    var orientationIndicatorImageId: Int = DEFAULT_ORIENTATION_INDICATOR,

    var moveAnimationDuration: Int = DEFAULT_MOVE_ANIMATION_DURATION,
    var ringAnimationDuration: Int = DEFAULT_RING_ANIMATION_DURATION,

    var isCoordinateEnabled: Boolean = DEFAULT_COORDINATE_ENABLED,
) {
    fun setConfig(context: Context, set: AttributeSet?, attrs: IntArray) {
        context.withStyledAttributes(set, attrs) {
            rowCount = getInteger(R.styleable.MazePaintView_rowCount, DEFAULT_ROW_COUNT)
            columnCount = getInteger(R.styleable.MazePaintView_columnCount, DEFAULT_COL_COUNT)

            entityScaleFactor =
                getFloat(R.styleable.MazePaintView_entityScale, DEFAULT_SCALE_FACTOR)
            indicatorScaleFactor =
                getFloat(R.styleable.MazePaintView_indicatorScale, DEFAULT_SCALE_FACTOR)
            coordinateTextScaleFactor =
                getFloat(
                    R.styleable.MazePaintView_coordinateTextScaleFactor,
                    DEFAULT_TEXT_SCALE_FACTOR
                )

            robotColor = getColor(R.styleable.MazePaintView_robotColor, DEFAULT_ROBOT_COLOR)
            coordinateTextColor =
                getColor(R.styleable.MazePaintView_coordinateTextColor, DEFAULT_TEXT_COLOR)

            robotDiameterCellSize =
                getInteger(R.styleable.MazePaintView_robotDiameterCellSize, DEFAULT_DIAMETER_SIZE)

            borderWidth = getFloat(R.styleable.MazePaintView_cellBorderWidth, DEFAULT_BORDER_WIDTH)
            borderColor = getColor(R.styleable.MazePaintView_cellBorderColor, DEFAULT_BORDER_COLOR)

            ringWidth = getFloat(R.styleable.MazePaintView_ringWidth, DEFAULT_RING_WIDTH)
            ringSizeMultiplier = getFloat(
                R.styleable.MazePaintView_ringSizeMultiplier,
                DEFAULT_RING_SIZE_MULTIPLIER
            )

            orientationIndicatorImageId = getResourceId(
                R.styleable.MazePaintView_orientationIndicatorDrawable,
                DEFAULT_ORIENTATION_INDICATOR
            )

            moveAnimationDuration = getInteger(
                R.styleable.MazePaintView_moveAnimationDurationMs,
                DEFAULT_MOVE_ANIMATION_DURATION
            )

            ringAnimationDuration = getInteger(
                R.styleable.MazePaintView_ringAnimationDurationMs,
                DEFAULT_RING_ANIMATION_DURATION
            )

            isCoordinateEnabled =
                getBoolean(R.styleable.MazePaintView_coordinatesEnabled, DEFAULT_COORDINATE_ENABLED)
        }
    }
}
