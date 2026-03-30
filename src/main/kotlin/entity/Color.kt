package entity

import java.io.Serializable

/**
 * enum class to represent the color of a player
 */
enum class Color : Serializable {
    BLUE,
    YELLOW,
    RED,
    GREEN;

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 3
    }
}