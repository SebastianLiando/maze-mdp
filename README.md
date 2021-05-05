# Maze View
This Android library adds the custom view `MazePaintView`. It is a 2D grid with an entity as shown below.

## Installation
Add the following as a gradle dependencies.
```implementation 'com.github.SebastianLiando:maze-mdp:1.3.2'```

If your project is Java based, ensure that Kotlin is configured for the project. 
First, add the Kotlin as dependency on the **project-level** gradle file.
```classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"```

Next, add these 2 dependencies to the **app-level** gradle file.
```
implementation "androidx.core:core-ktx:1.3.2"
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
```

Also add the Kotlin plugins
```
plugins {
    ...

    id 'kotlin-android'
    id 'kotlin-android-extensions'
}
```

## Getting Started
Please the wiki section of this project for more information. 

### Inflating the View
Add the `MazePaintView` to your XML layout. 

The most important attributes will be the `columnCount`, `rowCount`, and `robotDiameterCellSize`.
For more information of the attributes, please see the wiki section of this project.

```
<com.zetzaus.mazeview.core.MazePaintView
        android:id="@+id/mazePaintView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:cellBorderColor="?attr/colorSurface"
        app:columnCount="10"
        app:coordinateTextColor="?attr/colorOnSurface"
        app:coordinateTextScaleFactor="0.3"
        app:coordinatesEnabled="true"
        app:indicatorScale="0.5"
        app:layout_constraintBottom_toTopOf="@id/buttonLeft"
        app:orientationIndicatorDrawable="@drawable/ic_indicator"
        app:robotColor="?attr/colorSecondary"
        app:robotDiameterCellSize="3"
        app:rowCount="10" />
```

### Set Initial State
The custom view requires the maze string, the maze string decoder, and the center position of the entity. 

#### Maze String
The maze string is just a string. Each character of the string corresponds to a cell of the maze 
starting from the top left to the bottom right. The character to use is up to the developer, as the 
maze string decoder will map the character to the maze cell.

To set or update the maze string, set the `maze` property.

Kotlin code:
```
mazeView.maze = "000001111100000"
```

Java code:
```
mazeView.setMaze("000001111100000");
```

#### Decoder
The decoder is a `Map` that matches a `char` to a `Tile` subclasses. There are 2 `Tile` subclasses:
1. `SolidTile` - creates a colored cell
2. `BitmapTile` - creates a colored cell with an image on top of it

The decoder only needs to be set once. To set the decoder, set the `decoder` property.

Kotlin code:
```
mazeView.decoder = mapOf(
                       'U' to Tile.SolidTile(colorPrimary),
                       'E' to Tile.SolidTile(Color.YELLOW),
                       '6' to Tile.BitmapTile(
                           R.drawable.ic_six,
                           ContextCompat.getColor(this@MainActivity, R.color.soft_black)
                       )
                   )
```

Java code:
```
final Map<Character, Tile> decoder = new HashMap<>();
decoder.put('U', new Tile.SolidTile(getPrimaryColor(this)));
decoder.put('E', new Tile.SolidTile(Color.YELLOW));
decoder.put('6', new Tile.BitmapTile(
        R.drawable.ic_six,
        ContextCompat.getColor(this, R.color.soft_black))
);

mazeView.setDecoder(decoder);
```

#### Entity Position
The entity position is specified by the index of the center position of the entity in the maze string. For example, 
in a 3x3 maze, the top left will be index 0, the top middle will be index 1, etc. 

To set or update the entity center position, call the `updateRobotPosition` method. The second argument determines
whether the position update should be animated or not.

Kotlin code:
```
mazeView.updateRobotPosition(0, false);
```

Java code:
```
mazeView.updateRobotPosition(0, false);
```

#### Listening to Click
The custom view allows to listen to clicks to the cells, and will pass the coordinates of the clicked cell. Note that
the coordinate (0, 0) is at the bottom left side.

To add the listener, set the `touchUpListener` property.

Kotlin code:
```
touchUpListener = { x, y ->
    Toast.makeText(this@MainActivity, "Clicked coordinate ($x, $y)", Toast.LENGTH_LONG)
        .show()
}
```

Java code:
```
mazeView.setTouchUpListener((x, y) -> {
    final String toastMessage = String.format(Locale.getDefault(), "Clicked coordinate (%d, %d)", x, y);

    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

    return Unit.INSTANCE;
});
```