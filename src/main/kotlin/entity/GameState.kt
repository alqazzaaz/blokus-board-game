package entity

import java.io.Serializable

/**
 * data class to represent a Game State
 * @param field  the playing field, represented as a matrix
 * @param currentParticipantIndex Index of the current participant
 * @param nextGameState reference to the next saved game state (null if no such state exists)
 * @param previousGameState reference to the previous saved game state (null if no such state exists)
 * @param participantList List of the participants of the game
 */
class GameState(
    val field: Array<Array<Color?>>,
    var currentParticipantIndex: Int,
    var nextGameState: GameState? = null,
    var previousGameState: GameState? = null,
    val participantList: List<Participant>) : Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 4
    }
}