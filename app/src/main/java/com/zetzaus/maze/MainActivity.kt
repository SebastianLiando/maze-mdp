package com.zetzaus.maze

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zetzaus.mazeview.core.MazePaintView
import com.zetzaus.mazeview.core.Tile
import com.zetzaus.mazeview.extension.getThemeColor

class MainActivity : AppCompatActivity() {
    private val colorSecondary
        get() = getThemeColor(R.attr.colorSecondary)

    private val colorPrimary
        get() = getThemeColor(R.attr.colorPrimary)

    private val startMaze = "6U6UU" + "UUUUU" + "UUUUU" + "UUUUU" + "UUUUP"
    private val afterRobotMoveMaze = "6U6UU" + "UUUUU" + "UUPUU" + "UUUUU" + "UUUUU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MazePaintView>(R.id.mazePaintView).apply {
            touchUpListener = { x, y ->
                Toast.makeText(this@MainActivity, "Clicked coordinate ($x, $y)", Toast.LENGTH_LONG)
                    .show()
            }

            decoder = mapOf(
                'U' to Tile.SolidTile(colorPrimary),
                'P' to Tile.RobotTile(colorSecondary, colorPrimary),
                '6' to Tile.BitmapTile(
                    R.drawable.ic_six,
                    ContextCompat.getColor(this@MainActivity, R.color.soft_black)
                )
            )

            updateMaze(startMaze)

            Handler(Looper.getMainLooper()).postDelayed({
                updateMaze(afterRobotMoveMaze, true)
            }, 1000)
        }
    }
}