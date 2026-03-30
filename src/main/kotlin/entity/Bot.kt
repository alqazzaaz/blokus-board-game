package entity

import java.io.Serializable

/**
 * class to represent a local bot by inheriting from participant
 * @param name the name of given bot
 * @param actionAvailable flag to track if there is a possible move
 * @param color color of given bot
 * @param lastPlacedPiece the last placed piece
 * @param isEasy if the selected difficulty is easy
 */
class Bot (
    override val name: String,
    override var actionAvailable: Boolean,
    override val color: Color,
    override var lastPlacedPiece: Piece? = null,
    val isEasy :Boolean):
    Participant(name,actionAvailable,color,lastPlacedPiece=lastPlacedPiece), Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 2
    }
}