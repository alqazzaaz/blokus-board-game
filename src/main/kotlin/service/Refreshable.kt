package service

import entity.Participant

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {

    /**
     * Executes the corresponding actions after a game has been initialized.
     */
    fun refreshAfterInitializeGame() {}

    /**
     * Executes the corresponding actions after a piece has been rotated.
     */
    fun refreshAfterRotate() {}

    /**
     * Executes the corresponding actions after a piece has been flipped.
     */
    fun refreshAfterFlip() {}

    /**
     * Executes the corresponding actions after a piece has been placed.
     */
    fun refreshAfterPlace() {}

    /**
     * Executes the corresponding actions after undo was performed.
     */
    fun refreshAfterUndo() {}

    /**
     * Executes the corresponding actions after redo was performed.
     */
    fun refreshAfterRedo() {}

    /**
     * Executes the corresponding actions after a new participant is ready to play.
     */
    fun refreshAfterNextParticipant() {}

    /**
     * Executes the corresponding actions after the game ends and
     * the final score has been calculated.
     *
     * @param participantScores the participant scores as a list of pairs
     *          in which each participant is associated with its score
     */
    fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {}

    /**
     * Executes the corresponding actions after the current game has been saved.
     */
    fun refreshAfterSaveGame() {}

    /**
     * Executes the corresponding actions after a participant joined the remote game.
     *
     * @param name the name of the joined participant
     */
    fun refreshAfterParticipantJoined(name: String) {}

    /**
     * Executes the corresponding actions after a participant left the remote game.
     *
     * @param name the name of the left participant
     */
    fun refreshAfterParticipantLeft(name: String) {}
}