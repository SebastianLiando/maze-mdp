package com.zetzaus.maze

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MazePaintView>(R.id.mazePaintView).apply {
            touchUpListener = { x, y ->
                Toast.makeText(this@MainActivity, "Clicked coordinate ($x, $y)", Toast.LENGTH_LONG)
                    .show()
            }

            updateMaze((1..300).joinToString { if (Random.nextBoolean()) "0" else "O" })
        }
    }
}