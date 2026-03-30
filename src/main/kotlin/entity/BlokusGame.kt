package entity

import java.io.Serializable

/**
 * data class which saves the most important information regarding the current game
 * @param isBasicScoring flag to save which scoring method will be used
 * @param isNetwork flag to save if the game is a network game
 * @param botSpeed represents the delay after each bot move (in seconds)
 * @param currentGameState the current game state
 */
data class BlokusGame(val isBasicScoring: Boolean,
                      val isNetwork: Boolean,
                      var botSpeed: Int,
                      var currentGameState: GameState) : Serializable {

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 1
    }
}