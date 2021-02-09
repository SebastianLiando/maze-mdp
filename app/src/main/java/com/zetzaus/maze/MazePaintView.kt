package com.zetzaus.maze

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.zetzaus.maze.extension.drawBorderedRect
import com.zetzaus.maze.extension.getDrawableOrThrow
import com.zetzaus.maze.extension.getThemeColor
import com.zetzaus.maze.extension.toScaledBitmap
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

    private var paddingHorizontal = 0
    private var paddingVertical = 0

    private val robotRadius
        get() = (cellSize / 2) * scaleFactor

    private var currentRobotRadius = 0f
    private val robotAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        interpolator = DecelerateInterpolator()
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART

        addUpdateListener {
            currentRobotRadius = it.animatedFraction * robotRadius * 3f
            robotAnimationPaint.alpha = (255 * (1 - it.animatedFraction)).toInt()

            invalidate()
        }
    }

    private val tilePaint = Paint().apply {
        style = Paint.Style.FILL
        color = colorPrimary
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = colorSurface
    }

    private val obstaclePaint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.soft_black)
        alpha = 190
    }

    private val robotPaint = Paint().apply {
        style = Paint.Style.FILL
        color = colorSecondary
    }

    private val robotAnimationPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = colorSecondary
        strokeWidth = 12f
    }

    private lateinit var sixBitmap: Bitmap

    private lateinit var rectangles: List<Rect>

    /** Callback for click event. The parameters are the coordinate of the grid that is clicked (X, Y). */
    var touchUpListener: (x: Int, y: Int) -> Unit = { _, _ -> }

    private val colorSecondary
        get() = context.getThemeColor(R.attr.colorSecondary)

    private val colorPrimary
        get() = context.getThemeColor(R.attr.colorPrimary)

    private val colorSurface
        get() = context.getThemeColor(R.attr.colorSurface)

    private var rowCount = 1
    private var columnCount = 1
    private var scaleFactor = DEFAULT_SCALE_FACTOR

    /** The encoded maze. */
    private var maze: String = ""

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.MazePaintView) {
            rowCount = getInteger(R.styleable.MazePaintView_rowCount, 1)
            columnCount = getInteger(R.styleable.MazePaintView_columnCount, 1)
            scaleFactor = getFloat(R.styleable.MazePaintView_entityScale, DEFAULT_SCALE_FACTOR)
            maze = getString(R.styleable.MazePaintView_encodedMaze) ?: ""
        }
    }

    /**
     * Updates the current encoded maze [String]. Calling this function will redraw the maze by
     * triggering [onDraw].
     *
     * @param updatedMaze The updated encoded maze [String].
     */
    fun updateMaze(updatedMaze: String) {
        maze = updatedMaze
        invalidate()
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

        rectangles = mutableRectangles

        sixBitmap =
            context.getDrawableOrThrow(R.drawable.ic_six).toScaledBitmap(bitmapSize, bitmapSize)

        robotAnimator.cancel()
        robotAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rectangles.forEachIndexed { idx, it ->
            val paintToUse = if (maze[idx] == 'O') obstaclePaint else tilePaint
            canvas.drawBorderedRect(it, paintToUse, borderPaint)
        }

        val rect = rectangles[10]
        val rectPadStart = rect.left + (cellSize - bitmapSize) / 2
        val rectPadTop = rect.top + (cellSize - bitmapSize) / 2
        canvas.drawBitmap(sixBitmap, rectPadStart.toFloat(), rectPadTop.toFloat(), null)

        val robotRect = rectangles[25]

        canvas.drawCircle(
            robotRect.centerX().toFloat(),
            robotRect.centerY().toFloat(),
            robotRadius,
            robotPaint
        )

        canvas.drawCircle(
            robotRect.centerX().toFloat(),
            robotRect.centerY().toFloat(),
            currentRobotRadius,
            robotAnimationPaint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        if (event.action == MotionEvent.ACTION_UP) {
            val gridX = (touchX - paddingHorizontal) / cellSize
            val gridY = (touchY - paddingVertical) / cellSize

            // Ignore touch that is out of the grid
            if (gridX < 0 || gridX >= columnCount || gridY < 0 || gridY >= rowCount) return false

            touchUpListener(gridX.toInt(), gridY.toInt())
            return true
        }

        return true
    }

    companion object {
        const val DEFAULT_SCALE_FACTOR = 0.7f
    }
}