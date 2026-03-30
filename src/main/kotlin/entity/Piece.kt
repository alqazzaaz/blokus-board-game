package entity

import java.io.Serializable

/**
 * class to represent a piece using a 5x5 matrix
 * @param figure the 5x5 matrix with 0's and 1's representing the position of the piece
 */
class Piece(var figure : Array<IntArray>,val id : String) : Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 8
    }
}