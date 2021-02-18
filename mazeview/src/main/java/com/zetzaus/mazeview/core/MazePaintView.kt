package com.zetzaus.mazeview.core

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.zetzaus.mazeview.R
import com.zetzaus.mazeview.extension.drawBorderedRect
import kotlin.math.min

class MazePaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Width and height of a cell. */
    private var cellSize = 0

    /** Width and height of cell with an image. */
    private val bitmapSize
        get() = (cellSize * scaleFactor).toInt()

    /** Left and right padding of the whole maze. */
    private var paddingHorizontal = 0

    /** Top and bottom padding of the whole maze. */
    private var paddingVertical = 0

    /** The amount of cells occupied to the robot indicator's diameter. */
    private var robotDiameterCellSize = DEFAULT_DIAMETER_SIZE

    /** The radius of the robot indicator (which is a circle). */
    private val robotRadius
        get() = (cellSize * robotDiameterCellSize / 2) * scaleFactor

    /** Used for the robot animation. It is the current radius of the animated circle. */
    private var currentRobotRadius = 0f

    /** Animator for animating the robot. */
    private lateinit var robotAnimator: ValueAnimator

    /** Animator for moving robot position from one cell to another. */
    private lateinit var moveAnimator: ValueAnimator

    /** Paint for drawing a cell. */
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    /** Paint for drawing the cell borders. */
    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = DEFAULT_BORDER_WIDTH
        color = ContextCompat.getColor(context, android.R.color.white)
    }

    /** Paint for drawing the robot radar-like animation. */
    private val robotAnimationPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = DEFAULT_RING_WIDTH
    }

    /** The maze cells positions. */
    private lateinit var mazeCells: List<Rect>

    private var robotIndex = 0

    /** The current robot position. It is extracted to class property for animation purpose. */
    private lateinit var currentRobotPos: Pair<Int, Int>

    /** Callback for click event. The parameters are the coordinate of the grid that is clicked (X, Y). */
    var touchUpListener: ((x: Int, y: Int) -> Unit)? = null
        set(value) {
            isClickable = value != null
            field = value
        }

    private var rowCount = 1
    private var columnCount = 1
    private var scaleFactor = DEFAULT_SCALE_FACTOR
    private var moveAnimationDuration = DEFAULT_MOVE_ANIMATION_DURATION
    private var robotColor = Color.BLACK

    /** The encoded maze. Setting a new value to this variable will invalidate the maze. */
    var maze: String = ""
        set(value) {
            field = value
            invalidate()
        }

    /** Decodes a maze character into the tile. Set the decoder first before updating the maze. */
    var decoder: Map<Char, Tile> = mapOf()

    init {
        context.withStyledAttributes(attrs, R.styleable.MazePaintView) {
            rowCount = getInteger(R.styleable.MazePaintView_rowCount, 1)
            columnCount = getInteger(R.styleable.MazePaintView_columnCount, 1)
            scaleFactor = getFloat(R.styleable.MazePaintView_entityScale, DEFAULT_SCALE_FACTOR)
            maze = getString(R.styleable.MazePaintView_encodedMaze) ?: ""

            robotColor = getColor(R.styleable.MazePaintView_robotColor, Color.BLACK)

            robotDiameterCellSize =
                getInteger(R.styleable.MazePaintView_robotDiameterCellSize, DEFAULT_DIAMETER_SIZE)

            borderPaint.strokeWidth =
                getFloat(R.styleable.MazePaintView_cellBorderWidth, DEFAULT_BORDER_WIDTH)

            borderPaint.color =
                getColor(
                    R.styleable.MazePaintView_cellBorderColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )

            robotAnimationPaint.strokeWidth =
                getFloat(R.styleable.MazePaintView_ringWidth, DEFAULT_RING_WIDTH)

            moveAnimationDuration = getInteger(
                R.styleable.MazePaintView_moveAnimationDurationMs,
                DEFAULT_MOVE_ANIMATION_DURATION
            )

            robotAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                interpolator = DecelerateInterpolator()

                duration = this@withStyledAttributes.getInteger(
                    R.styleable.MazePaintView_ringAnimationDurationMs,
                    DEFAULT_RING_ANIMATION_DURATION
                ).toLong()

                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART

                val sizeMultiplier = this@withStyledAttributes.getFloat(
                    R.styleable.MazePaintView_ringSizeMultiplier,
                    DEFAULT_RING_SIZE_MULTIPLIER
                )

                addUpdateListener {
                    currentRobotRadius = it.animatedFraction * robotRadius * sizeMultiplier
                    robotAnimationPaint.alpha = (255 * (1 - it.animatedFraction)).toInt()

                    invalidate()
                }
            }
        }
    }

    /**
     * Updates the robot indicator position in the maze.
     *
     * @param index The index of the String the robot is at.
     * @param animated `true` to animate the position change.
     */
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
                duration = moveAnimationDuration.toLong()
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val maxColSize = w / columnCount
        val maxRowSize = h / rowCount
        cellSize = min(maxColSize, maxRowSize)
        Log.d("MazePaintView", "Square size is $cellSize")

        paddingVertical = (h - cellSize * rowCount) / 2
        Log.d("MazePaintView", "Padding vertical is $paddingVertical")

        paddingHorizontal = (w - cellSize * columnCount) / 2
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
                        tile.getBitmap(context, bitmapSize),
                        rectPadStart.toFloat(),
                        rectPadTop.toFloat(),
                        null
                    )
                }
            }
        }

        // Draw robot last, so that robot is on top of all the tiles
        canvas.drawCircle(
            currentRobotPos.first.toFloat(),
            currentRobotPos.second.toFloat(),
            robotRadius,
            fillPaint.apply { color = robotColor }
        )

        canvas.drawCircle(
            currentRobotPos.first.toFloat(),
            currentRobotPos.second.toFloat(),
            currentRobotRadius,
            robotAnimationPaint.apply {
                val currentAlpha = alpha
                color = robotColor
                alpha = currentAlpha
            }
        )
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
            if (gridX < 0 || gridX >= columnCount || gridY < 0 || gridY >= rowCount) return false

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
        const val DEFAULT_SCALE_FACTOR = 0.7f
        const val DEFAULT_BORDER_WIDTH = 4f
        const val DEFAULT_RING_WIDTH = 10f

        const val DEFAULT_RING_ANIMATION_DURATION = 1500
        const val DEFAULT_RING_SIZE_MULTIPLIER = 3f

        const val DEFAULT_MOVE_ANIMATION_DURATION = 500

        const val DEFAULT_DIAMETER_SIZE = 1

        const val PARENT_STATE_KEY = "PARENT_STATE_KEY"
        const val MAZE_KEY = "MAZE_KEY"
        const val ROBOT_INDEX_KEY = "ROBOT_INDEX_KEY"
    }
}