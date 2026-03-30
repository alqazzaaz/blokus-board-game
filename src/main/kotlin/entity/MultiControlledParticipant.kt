package entity

import java.io.Serializable

/**
 * class to represent the participant which is controlled by 3 different participants in the 3 Participant case
 * @param name the name of given participant
 * @param actionAvailable flag to track if there is a possible move
 * @param color color of given participant
 * @param lastPlacedPiece the last placed piece
 * @param controlledBy list of the participants who will control this participant next
 */
class MultiControlledParticipant(
    override val name: String,
    override var actionAvailable: Boolean,
    override val color: Color,
    override var lastPlacedPiece: Piece? = null,
    var controlledBy:MutableList<Participant>):
    Participant(name,actionAvailable,color,lastPlacedPiece=lastPlacedPiece), Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 5
    }
}