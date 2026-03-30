package service

import entity.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Test class for [GameService.nextParticipant].
 * Covers normal turn advancement, wraparound to the first participant,
 * automatic game end when no moves are available, and error handling
 */
class GameServiceNextParticipantTest {

    private fun leeresFeld() = Array(20) { Array<Color?>(20) { null } }

    /**
     * Tests that [GameService.nextParticipant] correctly advances the turn
     * from the current participant to the next one in the list.
     *
     * Each participant is given 19 pieces so that [possibleMove] returns true
     * immediately without scanning the entire board.
     *
     * Preconditions:
     * - A game is running with two participants.
     * - Alice (index 0) is the current participant.
     * - Both participants have 19 pieces and can still play.
     *
     * Expected result:
     * - After calling nextParticipant(), the current index is 1 (Bob's turn).
     */
    @Test
    fun `nextParticipant wechselt zum naechsten Spieler`() {
        val rootService = RootService()

        val alice = Player(name = "Alice", actionAvailable = true, color = Color.BLUE)
        val bob   = Player(name = "Bob",   actionAvailable = true, color = Color.RED)

        alice.pieceList.clear()
        alice.pieceList.addAll(MutableList(19) { Piece(figure = arrayOf(intArrayOf(1)), id = "O1") })
        bob.pieceList.clear()
        bob.pieceList.addAll(MutableList(19) { Piece(figure = arrayOf(intArrayOf(1)), id = "O1") })

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice, bob),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )
        rootService.gameService.nextParticipant()
        val game = checkNotNull(rootService.game)
        assertEquals(1, game.currentGameState.currentParticipantIndex)
    }

    /**
     * Test that [GameService.nextParticipant] correctly wraps around to index 0
     * after the last participant has finished their turn.
     *
     * Preconditions:
     * - A game is running with two participants.
     * - Bob (index 1, the last participant) is the current participant.
     * - Both participants have 19 pieces and can still play.
     *
     * Expected result:
     * - After calling nextParticipant(), the current index wraps around to 0 (Alice turn).
     */
    @Test
    fun `nextParticipant springt nach letztem Spieler wieder zu Index 0`() {
        val rootService = RootService()

        val alice = Player(name = "Alice", actionAvailable = true, color = Color.BLUE)
        val bob   = Player(name = "Bob",   actionAvailable = true, color = Color.RED)

        alice.pieceList.clear()
        alice.pieceList.addAll(MutableList(19) { Piece(figure = arrayOf(intArrayOf(1)), id = "O1") })
        bob.pieceList.clear()
        bob.pieceList.addAll(MutableList(19) { Piece(figure = arrayOf(intArrayOf(1)), id = "O1") })

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 1,
            participantList = mutableListOf(alice, bob),
            nextGameState = null,
            previousGameState = null
        )
        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        rootService.gameService.nextParticipant()
        val game = checkNotNull(rootService.game)
        assertEquals(0, game.currentGameState.currentParticipantIndex)
    }

    /**
     * Test that [GameService.nextParticipant] triggers [GameService.score]
     * when no participant is able to make a move.
     *
     * If all participants have no remaining pieces, [possibleMove] returns false
     * for every participant and the game ends by calling score().
     * This is verified by checking that [Refreshable.refreshAfterScore] was called.
     *
     * Preconditions:
     * - A game is running with two participants.
     * - Both Alice and Bob have no remaining pieces and actionAvailable = false.
     *
     * Expected result:
     * - [GameService.score] is called, which triggers [Refreshable.refreshAfterScore].
     */
    @Test
    fun `nextParticipant ruft score auf wenn niemand mehr spielen kann`() {
        val rootService = RootService()

        val alice = Player(name = "Alice", actionAvailable = false, color = Color.BLUE)
        val bob   = Player(name = "Bob",   actionAvailable = false, color = Color.RED)
        alice.pieceList.clear()
        bob.pieceList.clear()

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice, bob),
            nextGameState = null,
            previousGameState = null
        )
        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        var scoreWurdeAufgerufen = false
        rootService.gameService.addRefreshable(object : Refreshable {
            override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
                scoreWurdeAufgerufen = true
            }
        })

        rootService.gameService.nextParticipant()
        assertTrue(scoreWurdeAufgerufen, "score() ist nicht aufgerufen, obwohl niemand mehr spielen kann")
    }

    /**
     * Tests that [GameService.nextParticipant] throws an [IllegalStateException]
     * when no game is currently running
     *
     * Preconditions:
     * -No game has been started.
     *
     * Expected result:
     * -[IllegalStateException] is thrown.
     */
    @Test
    fun `nextParticipant wirft Fehler wenn kein Spiel laeuft`() {
        val rootService = RootService()

        assertFailsWith<IllegalStateException> {
            rootService.gameService.nextParticipant()
        }
    }
}