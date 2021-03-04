package com.zetzaus.mazeview.extension

/**
 * Takes every [step] elements in the list.
 *
 * @param E The element type.
 * @param step The index range between elements to take.
 *
 * @return The list containing every [step] elements in the input list.
 */
fun <E> Collection<E>.takeEvery(step: Int): Collection<E> {
    val originalSize = size

    return mutableListOf<E>().apply {
        (0 until originalSize step step).forEach {
            add(this@takeEvery.elementAt(it))
        }
    }
}