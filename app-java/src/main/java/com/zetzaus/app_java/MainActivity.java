package com.zetzaus.app_java;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.zetzaus.mazeview.core.MazePaintView;
import com.zetzaus.mazeview.core.Orientation;
import com.zetzaus.mazeview.core.Tile;
import com.zetzaus.mazeview.extension.ContextExtensionsKt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity {
    private static final int COLUMN = 10;
    private static final int ROW = 10;

    private int getPrimaryColor(Context context) {
        return ContextExtensionsKt.getThemeColor(context, R.attr.colorPrimary);
    }

    private String getStartingMaze() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            result.append("U");
        }

        result = result.replace(0, 1, "6")
                .replace(81, 82, "E");

        Log.d("GetStartingMaze", "Starting maze length: " + result.length());

        return result.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MazePaintView mazeView = findViewById(R.id.mazePaintView);

        // Set a listener when a cell in the maze is clicked
        mazeView.setTouchUpListener((x, y) -> {
            final String toastMessage = String.format(Locale.getDefault(), "Clicked coordinate (%d, %d)", x, y);

            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

            final int indexTouched = (ROW - y - 1) * COLUMN + x;

            if (mazeView.getMaze().charAt(indexTouched) != '6') {
                String currentMaze = mazeView.getMaze();

                String updatedMaze = new StringBuilder().append(currentMaze)
                        .replace(indexTouched, indexTouched + 1, "E")
                        .toString();

                mazeView.setMaze(updatedMaze);
                mazeView.updateRobotPosition(indexTouched);
            }

            return Unit.INSTANCE;
        });

        // Set the decoder to translate the maze string into the tile
        final Map<Character, Tile> decoder = new HashMap<>();
        decoder.put('U', new Tile.SolidTile(getPrimaryColor(this)));
        decoder.put('E', new Tile.SolidTile(Color.YELLOW));
        decoder.put('6', new Tile.BitmapTile(
                R.drawable.ic_six,
                ContextCompat.getColor(this, R.color.soft_black))
        );

        mazeView.setDecoder(decoder);

        // Set initial maze state
        mazeView.setMaze(getStartingMaze());
        mazeView.updateRobotPosition(getStartingMaze().indexOf('E'), false);

        // Set button listeners
        final Button leftButton = findViewById(R.id.buttonLeft);
        leftButton.setOnClickListener(
                v -> mazeView.updateRobotOrientation(
                        getLeftOrientation(mazeView.getRobotOrientation())
                )
        );

        final Button rightButton = findViewById(R.id.buttonRight);
        rightButton.setOnClickListener(
                v -> mazeView.updateRobotOrientation(
                        getRightOrientation(mazeView.getRobotOrientation())
                )
        );

    }

    @NonNull
    private Orientation getLeftOrientation(Orientation current) {
        switch (current) {
            case FRONT:
                return Orientation.LEFT;
            case BACK:
                return Orientation.RIGHT;
            case LEFT:
                return Orientation.BACK;
            default:
                return Orientation.FRONT;
        }
    }

    @NonNull
    private Orientation getRightOrientation(Orientation current) {
        switch (current) {
            case FRONT:
                return Orientation.RIGHT;
            case BACK:
                return Orientation.LEFT;
            case LEFT:
                return Orientation.FRONT;
            default:
                return Orientation.BACK;
        }
    }
}