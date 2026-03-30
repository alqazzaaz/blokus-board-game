package service

import entity.*
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test class for [GameService.isLegalMove].
 * Covers the first move corner rule, out-of-bounds coordinates,
 * placing on an occupied cell, and error handling.
 */
class GameServiceIsLegalMoveTest {

    private fun leeresFeld() = Array(20) { Array<Color?>(20) { null } }

    /**
     * Tests that [GameService.isLegalMove] returns true when the first piece
     * is placed on a corner of the board.
     *
     * According to Blokus rules, the very first piece of each participant must
     * cover one of the four corners of the board.
     * This is indicated by [Participant.lastPlacedPiece] being null.
     *
     * Preconditions:
     * - A game is running with one participant "Alice".
     * - Alice has not placed any piece yet (lastPlacedPiece = null -> first move).
     * - The piece is a 1x1 piece placed at position (0, 0), the top-left corner.
     *
     * Expected result:
     *  [GameService.isLegalMove] return true.
     */
    @Test
    fun `isLegalMove erster Zug auf Ecke ist erlaubt`() {
        val rootService = RootService()

        val stein = Piece(figure = arrayOf(intArrayOf(1)), id = "O1")

        val alice = Player(name = "Alice", actionAvailable = true, color = Color.BLUE)
        alice.pieceList.clear()
        alice.pieceList.add(stein)

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice),
            nextGameState = null,
            previousGameState = null
        )
        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )
        assertTrue(rootService.gameService.isLegalMove(stein, Pair(0, 0)))
    }

    /**
     * Tests that [GameService.isLegalMove] returns false when the first piece
     * is not placed on the corner of the board.
     *
     * According to Blokus rules, the very first piece must cover one of the four
     * board corners. Placing it anywhere else is illegal.
     *
     * Preconditions:
     * -A game is running with one participant "Alice"
     * -Alice has not placed any piece yet (lastPlacedPiece = null -> first move).
     * -The piece is a 1x1 piece placed at position (5, 5) —> not a corner.
     *
     * Expected result:
     * -[GameService.isLegalMove] return false.
     */
    @Test
    fun `isLegalMove erster Zug nicht auf Ecke ist ungueltig`() {
        val rootService = RootService()

        val stein = Piece(figure = arrayOf(intArrayOf(1)), id = "O1")

        val alice = Player(name = "A", actionAvailable = true, color = Color.BLUE)
        alice.pieceList.clear()
        alice.pieceList.add(stein)

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        assertFalse(rootService.gameService.isLegalMove(stein, Pair(5, 5)))
    }

    /**
     * Tests that [GameService.isLegalMove] returns false when the given coordinates
     * are outside the bound of the board.
     *
     * Preconditions:
     * - A game is running with one participant (Alice).
     * - The piece is a 1x1 piece placed at position (21, 21) —> far outside the 20x20 board.
     *
     * Expected result:
     * -[GameService.isLegalMove] return false
     */
    @Test
    fun `isLegalMove gibt false bei Koordinaten ausserhalb des Feldes`() {
        val rootService = RootService()

        val stein = Piece(figure = arrayOf(intArrayOf(1)), id = "O1")

        val alice = Player(name = "A", actionAvailable = true, color = Color.BLUE)
        alice.pieceList.clear()
        alice.pieceList.add(stein)

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        assertFalse(rootService.gameService.isLegalMove(stein, Pair(21, 21)))
    }

    /**
     * Tests that [GameService.isLegalMove] returns false when the target cell
     * on the board is already occupied by another piece.
     *
     * Preconditions:
     * - A game is running with two participants (Alice and Bob).
     * - Bob (index 1) is the current participant and it's not his first move.
     * - Position (0, 0) on the board is already occupied by Alice's color (BLUE).
     * - Bob tries to place a 1x1 piece at position (0, 0).
     *
     * Expected result:
     * [GameService.isLegalMove] return false.
     */
    @Test
    fun `isLegalMove stein auf besetztem Feld ist ungueltig`() {
        val rootService = RootService()

        val stein = Piece(figure = arrayOf(intArrayOf(1)), id = "O1")

        val alice = Player(name = "A", actionAvailable = true, color = Color.BLUE)
        val bob   = Player(name = "B",   actionAvailable = true, color = Color.RED)

        bob.pieceList.clear()
        bob.pieceList.add(stein)
        bob.lastPlacedPiece = Piece(figure = arrayOf(intArrayOf(1)), id = "X1")

        val feld = leeresFeld()
        feld[0][0] = Color.BLUE

        val gameState = GameState(
            field = feld,
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
        assertFalse(rootService.gameService.isLegalMove(stein, Pair(0, 0)))
    }

    /**
     * Tests that [GameService.isLegalMove] throws an [IllegalStateException]
     * when no game is currently running.
     *
     * Expected result:
     * [IllegalStateException] is thrown.
     */
    @Test
    fun `isLegalMove wirft Fehler wenn kein Spiel laeuft`() {
        val rootService = RootService()
        val stein = Piece(figure = arrayOf(intArrayOf(1)), id = "O1")

        assertFailsWith<IllegalStateException> {
            rootService.gameService.isLegalMove(stein, Pair(0, 0))
        }
    }
}