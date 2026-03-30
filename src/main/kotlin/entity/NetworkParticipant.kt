package entity

import java.io.Serializable

/**
 * class to represent a network participant (so bot or human) by inheriting from participant
 * @param name the name of given participant
 * @param actionAvailable flag to track if there is a possible move
 * @param color color of given participant
 * @param lastPlacedPiece the last placed piece
 */
class NetworkParticipant(
    override val name: String,
    override var actionAvailable: Boolean,
    override val color: Color,
    override var lastPlacedPiece: Piece? = null):
    Participant(name,actionAvailable,color,lastPlacedPiece=lastPlacedPiece), Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 6
    }
}