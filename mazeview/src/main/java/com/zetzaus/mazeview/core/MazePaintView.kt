package com.zetzaus.mazeview.core

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.zetzaus.mazeview.R
import com.zetzaus.mazeview.extension.*
import kotlin.math.min

class MazePaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Maze configuration information. */
    private val mazeConfig = MazeViewConfig()

    /** Width and height of a cell. */
    private var cellSize = 0

    /** Width and height of cell with an image. */
    private val bitmapSize
        get() = (cellSize * mazeConfig.entityScaleFactor).toInt()

    /** Left and right padding of the whole maze. */
    private var paddingHorizontal = 0

    /** Top and bottom padding of the whole maze. */
    private var paddingVertical = 0

    /** The amount of cells occupied to the robot indicator's diameter. */
    private val robotDiameterCellSize
        get() = mazeConfig.robotDiameterCellSize

    /** The radius of the robot indicator (which is a circle). */
    private val robotRadius
        get() = (cellSize * robotDiameterCellSize / 2) * mazeConfig.entityScaleFactor

    /** Used for the robot animation. It is the current radius of the animated circle. */
    private var currentRobotRadius = 0f

    /** Animator for animating the robot. */
    private var robotAnimator: ValueAnimator

    /** Animator for moving robot position from one cell to another. */
    private lateinit var moveAnimator: ValueAnimator

    /** Animator for rotating the robot's orientation indicator. */
    private lateinit var orientationAnimator: ValueAnimator

    /** Paint for drawing a cell. */
    private val fillPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /** Paint for drawing the cell borders. */
    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = mazeConfig.borderWidth
        isDither = true
        isAntiAlias = true
        color = ContextCompat.getColor(context, android.R.color.white)
    }

    /** Paint for drawing the robot radar-like animation. */
    private val robotAnimationPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        isDither = true
        strokeWidth = mazeConfig.ringWidth
    }

    /** The maze cells positions. */
    private lateinit var mazeCells: List<Rect>

    /** The robot center point index. */
    private var robotIndex = 0

    /** The current robot position. It is extracted to class property for animation purpose. */
    private lateinit var currentRobotPos: Pair<Int, Int>

    /** The robot's orientation. */
    var robotOrientation = Orientation.FRONT
        private set

    /** The current rotation of the orientation indicator (used for animation).*/
    private var currentIndicatorRotation = 0f

    /** The image id for orientation indicator. */
    private val orientationIndicatorImageId
        get() = mazeConfig.orientationIndicatorImageId

    /** Callback for click event. The parameters are the coordinate of the grid that is clicked (X, Y). */
    var touchUpListener: ((x: Int, y: Int) -> Unit)? = null
        set(value) {
            isClickable = value != null
            field = value
        }

    /** The encoded maze. Setting a new value to this variable will invalidate the maze. */
    var maze: String = ""
        set(value) {
            field = value
            invalidate()
        }

    /** Decodes a maze character into the tile. Set the decoder first before updating the maze. */
    var decoder: Map<Char, Tile> = mapOf()

    /** Used to retrieve the text boundaries to draw text center. */
    private val textBounds = Rect()

    init {
        mazeConfig.setConfig(context, attrs, R.styleable.MazePaintView)

        borderPaint.strokeWidth = mazeConfig.borderWidth
        borderPaint.color = mazeConfig.borderColor

        robotAnimationPaint.strokeWidth = mazeConfig.ringWidth

        robotAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            interpolator = DecelerateInterpolator()

            duration = mazeConfig.ringAnimationDuration.toLong()

            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART

            addUpdateListener {
                currentRobotRadius =
                    it.animatedFraction * robotRadius * mazeConfig.ringSizeMultiplier

                robotAnimationPaint.alpha = (255 * (1 - it.animatedFraction)).toInt()

                invalidate()
            }
        }
    }

    /**
     * Updates the robot indicator position in the maze.
     *
     * @param index The index of the String the robot is at.
     * @param animated `true` to animate the position change.
     */
    @JvmOverloads
    fun updateRobotPosition(index: Int, animated: Boolean = true) {
        robotIndex = index

        if (!::mazeCells.isInitialized) return

        if (!animated) {
            currentRobotPos = getRobotIndicatorCenterPoint(mazeCells, index)
            invalidate()
        } else {
            val fromX = currentRobotPos.first
            val fromY = currentRobotPos.second

            val (toX, toY) = getRobotIndicatorCenterPoint(mazeCells, index)

            if (::moveAnimator.isInitialized) {
                moveAnimator.cancel()
            }

            moveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = mazeConfig.moveAnimationDuration.toLong()
                addUpdateListener {
                    val nextX = fromX + it.animatedFraction * (toX - fromX)
                    val nextY = fromY + it.animatedFraction * (toY - fromY)

                    currentRobotPos = nextX.toInt() to nextY.toInt()

                    invalidate()
                }
            }

            moveAnimator.start()
        }
    }

    /**
     * Updates the orientation indicator image rotation.
     *
     * @param orientation The orientation of the robot.
     * @param animated `true` to animate the rotation.
     */
    @JvmOverloads
    fun updateRobotOrientation(orientation: Orientation, animated: Boolean = true) {
        val prevOrientation = robotOrientation
        robotOrientation = orientation
        var targetRotation = orientation.degree.toFloat()

        if (!animated) {
            currentIndicatorRotation = targetRotation
            invalidate()
        } else {
            if (prevOrientation == Orientation.LEFT && targetRotation == 0f) {
                targetRotation = 360f
            }

            if (currentIndicatorRotation == 0f && orientation == Orientation.LEFT) {
                currentIndicatorRotation = 360f
            }

            if (::orientationAnimator.isInitialized) orientationAnimator.cancel()

            orientationAnimator =
                ValueAnimator.ofFloat(currentIndicatorRotation, targetRotation).apply {
                    duration = mazeConfig.moveAnimationDuration.toLong()

                    addUpdateListener {
                        currentIndicatorRotation = it.animatedValue as Float
                        invalidate()
                    }

                    doOnEnd {
                        currentIndicatorRotation = orientation.degree.toFloat()
                    }

                    doOnCancel {
                        currentIndicatorRotation = orientation.degree.toFloat()
                    }
                }

            orientationAnimator.start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val rowCount = mazeConfig.rowCount
        val columnCount = mazeConfig.columnCount
        val isCoordinatesEnabled = mazeConfig.isCoordinateEnabled

        val maxColSize = w / (columnCount + if (isCoordinatesEnabled) 2 else 0)
        val maxRowSize = h / (rowCount + if (isCoordinatesEnabled) 2 else 0)
        cellSize = min(maxColSize, maxRowSize)
        Log.d("MazePaintView", "Square size is $cellSize")

        paddingVertical = ((h - cellSize * rowCount) / 2)
        Log.d("MazePaintView", "Padding vertical is $paddingVertical")

        paddingHorizontal = ((w - cellSize * columnCount) / 2)
        Log.d("MazePaintView", "Padding horizontal is $paddingHorizontal")

        val mutableRectangles = mutableListOf<Rect>()

        (1..rowCount).forEach { y ->
            val top = paddingVertical + cellSize * (y - 1)
            val bottom = top + cellSize

            (1..columnCount).forEach { x ->
                val left = paddingHorizontal + cellSize * (x - 1)
                val right = left + cellSize

                mutableRectangles.add(Rect(left, top, right, bottom))
            }
        }

        mazeCells = mutableRectangles

        if (::moveAnimator.isInitialized) moveAnimator.cancel()
        if (::orientationAnimator.isInitialized) orientationAnimator.cancel()

        currentRobotPos = getRobotIndicatorCenterPoint(mazeCells, robotIndex)

        robotAnimator.cancel()
        robotAnimator.start()
    }

    /**
     * Returns the center coordinate (x, y) of the robot indicator.
     *
     * @param cells The maze cells.
     * @param indexCenter The center index of the robot. For even-sized robot, the center index is
     * the lowest and leftmost cell.
     *
     * @return The center coordinate (x, y).
     */
    private fun getRobotIndicatorCenterPoint(cells: List<Rect>, indexCenter: Int): Pair<Int, Int> {
        return if (robotDiameterCellSize % 2 == 0) {
            val robotRadiusCellSize = robotDiameterCellSize / 2
            val centerCell = cells[indexCenter]
            val centerY = centerCell.bottom - (cellSize * robotRadiusCellSize)
            val centerX = centerCell.left + (cellSize * robotRadiusCellSize)

            centerX to centerY
        } else {
            cells[indexCenter].centerX() to cells[indexCenter].centerY()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (maze.isEmpty()) return

        maze.map {
            decoder[it]
                ?: throw IllegalArgumentException("The encoded maze contains character that is not stated in the decoder!")
        }.forEachIndexed { index, tile ->
            val currentRect = mazeCells[index]

            when (tile) {
                is Tile.SolidTile -> {
                    canvas.drawBorderedRect(
                        currentRect,
                        fillPaint.apply { color = tile.color },
                        borderPaint
                    )
                }

                is Tile.BitmapTile -> {
                    val rectPadStart = currentRect.left + (cellSize - bitmapSize) / 2
                    val rectPadTop = currentRect.top + (cellSize - bitmapSize) / 2

                    canvas.drawBorderedRect(
                        currentRect,
                        fillPaint.apply { color = tile.backgroundColor },
                        borderPaint
                    )

                    canvas.drawBitmap(
                        context.getBitmap(tile.imageId, bitmapSize),
                        rectPadStart.toFloat(),
                        rectPadTop.toFloat(),
                        null
                    )
                }
            }
        }

        // Draw robot last, so that robot is on top of all the tiles
        val currentRobotX = currentRobotPos.first.toFloat()
        val currentRobotY = currentRobotPos.second.toFloat()
        val robotColor = mazeConfig.robotColor

        canvas.drawCircle(
            currentRobotX,
            currentRobotY,
            robotRadius,
            fillPaint.apply { color = robotColor }
        )

        canvas.drawCircle(
            currentRobotX,
            currentRobotY,
            currentRobotRadius,
            robotAnimationPaint.apply {
                val currentAlpha = alpha
                color = robotColor
                alpha = currentAlpha
            }
        )

        // Draw robot orientation
        canvas.drawBitmapWithRotation(
            context.getBitmap(
                orientationIndicatorImageId,
                (robotRadius * 2 * mazeConfig.indicatorScaleFactor).toInt()
            ),
            currentRobotX,
            currentRobotY,
            currentIndicatorRotation,
            fillPaint
        )

        val textPaint = fillPaint.apply {
            textSize = mazeConfig.coordinateTextScaleFactor * cellSize
            color = mazeConfig.coordinateTextColor
            textAlign = Paint.Align.CENTER
        }

        // Draw x coordinates
        if (!mazeConfig.isCoordinateEnabled) return

        var currentX = 0
        mazeCells.takeLast(mazeConfig.columnCount).forEach { area ->
            canvas.drawTextCentered(
                currentX++.toString(),
                area.centerX().toFloat(),
                (area.centerY() + cellSize).toFloat(),
                textPaint,
                textBounds
            )
        }

        // Draw y coordinates
        var currentY = 0
        mazeCells.takeEvery(mazeConfig.columnCount)
            .reversed()
            .forEach { area ->
                canvas.drawTextCentered(
                    currentY++.toString(),
                    area.centerX().toFloat() - cellSize,
                    area.centerY().toFloat(),
                    textPaint,
                    textBounds
                )
            }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        if (event.action == MotionEvent.ACTION_UP) {
            // X is left to right
            val gridX = (touchX - paddingHorizontal) / cellSize

            // Y is bottom to up
            val gridY = (mazeCells.last().bottom - touchY) / cellSize

            // Ignore touch that is out of the grid
            if (gridX < 0 || gridX >= mazeConfig.columnCount || gridY < 0 || gridY >= mazeConfig.rowCount)
                return false

            touchUpListener?.invoke(gridX.toInt(), gridY.toInt())

            return true
        }

        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(PARENT_STATE_KEY, super.onSaveInstanceState())

            // Save maze
            putString(MAZE_KEY, maze)
            putInt(ROBOT_INDEX_KEY, robotIndex)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as Bundle?)?.run {
            maze = getString(MAZE_KEY)!!
            robotIndex = getInt(ROBOT_INDEX_KEY)

            super.onRestoreInstanceState(getParcelable(PARENT_STATE_KEY))
        }
    }

    companion object {
        const val PARENT_STATE_KEY = "PARENT_STATE_KEY"
        const val MAZE_KEY = "MAZE_KEY"
        const val ROBOT_INDEX_KEY = "ROBOT_INDEX_KEY"
    }
}