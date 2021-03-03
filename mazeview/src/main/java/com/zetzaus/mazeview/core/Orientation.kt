package com.zetzaus.mazeview.core

/**
 * Represents where the entity robot is facing. The direction is related to the user's point of view.
 *
 */
enum class Orientation {
    FRONT, BACK, LEFT, RIGHT;

    val degree: Int
        get() = when (this) {
            FRONT -> 0
            RIGHT -> 90
            BACK -> 180
            LEFT -> 270
        }
}