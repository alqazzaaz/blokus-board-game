package entity

import java.io.Serializable

/**
 * class to represent a local player by inheriting from participant
 * @param name the name of given player
 * @param actionAvailable flag to track if there is a possible move
 * @param color color of given player
 * @param lastPlacedPiece the last placed piece
 */
class Player(
    override val name: String,
    override var actionAvailable: Boolean,
    override val color: Color,
    override var lastPlacedPiece: Piece? = null):
    Participant(name,actionAvailable,color,lastPlacedPiece=lastPlacedPiece), Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 9
    }
}