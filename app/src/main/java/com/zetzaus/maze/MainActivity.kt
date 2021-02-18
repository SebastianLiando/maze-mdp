package com.zetzaus.maze

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zetzaus.mazeview.core.MazePaintView
import com.zetzaus.mazeview.core.Tile
import com.zetzaus.mazeview.extension.getThemeColor

class MainActivity : AppCompatActivity() {
    private val colorPrimary
        get() = getThemeColor(R.attr.colorPrimary)

    private val startMaze = "U".repeat(100)
        .replaceRange(0..0, "6")
        .replaceRange(81..81, "E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MazePaintView>(R.id.mazePaintView).apply {
            touchUpListener = { x, y ->
                Toast.makeText(this@MainActivity, "Clicked coordinate ($x, $y)", Toast.LENGTH_LONG)
                    .show()

                val indexTouched = (ROW - y - 1) * COLUMN + x

                if (maze[indexTouched] != '6') {
                    maze = maze.replaceRange(indexTouched..indexTouched, "E")
                    updateRobotPosition(indexTouched)
                }
            }

            decoder = mapOf(
                'U' to Tile.SolidTile(colorPrimary),
                'E' to Tile.SolidTile(Color.YELLOW),
                '6' to Tile.BitmapTile(
                    R.drawable.ic_six,
                    ContextCompat.getColor(this@MainActivity, R.color.soft_black)
                )
            )

            maze = startMaze
            updateRobotPosition(startMaze.indexOf('E'), false)
        }
    }

    companion object {
        const val COLUMN = 10
        const val ROW = 10
    }
}