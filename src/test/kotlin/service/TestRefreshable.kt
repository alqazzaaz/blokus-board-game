package service

import entity.Participant

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called (since last [reset])
 */
class TestRefreshable: Refreshable {
    var refreshAfterInitializeGame: Boolean = false
        private set
    var refreshAfterRotate: Boolean = false
        private set
    var refreshAfterFlip: Boolean = false
        private set
    var refreshAfterPlace: Boolean = false
        private set
    var refreshAfterUndo: Boolean = false
        private set
    var refreshAfterRedo: Boolean = false
        private set
    var refreshAfterNextParticipant: Boolean = false
        private set
    var refreshAfterScore: Boolean = false
        private set
    var refreshAfterSaveGame: Boolean = false
        private set

    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterInitializeGame = false
        refreshAfterRotate = false
        refreshAfterFlip = false
        refreshAfterPlace = false
        refreshAfterUndo = false
        refreshAfterRedo = false
        refreshAfterNextParticipant = false
        refreshAfterScore = false
        refreshAfterSaveGame = false
    }
    override fun refreshAfterInitializeGame() {
        refreshAfterInitializeGame = true
    }

    override fun refreshAfterRotate() {
        refreshAfterRotate = true
    }

    override fun refreshAfterFlip() {
        refreshAfterFlip = true
    }

    override fun refreshAfterPlace() {
        refreshAfterPlace = true
    }

    override fun refreshAfterUndo() {
        refreshAfterUndo = true
    }

    override fun refreshAfterRedo() {
        refreshAfterRedo = true
    }

    override fun refreshAfterNextParticipant() {
        refreshAfterNextParticipant = true
    }

    override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
        refreshAfterScore = true
    }

    override fun refreshAfterSaveGame() {
        refreshAfterSaveGame = true
    }
}
