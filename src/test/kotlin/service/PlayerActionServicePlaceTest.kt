package service

import entity.Color
import entity.MultiControlledParticipant
import entity.Piece
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * test for given function
 */
class PlayerActionServicePlaceTest {
    private lateinit var rootService: RootService

    /**
     * setup method
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /**
     * simple method to check for setup
     */
    @Test
    fun testIfSetUpWorked() {
        assertDoesNotThrow("The root service should be initialized.") { rootService }
    }

    /**
     * Testing if place fails when no game is running
     */
    @Test
    fun testPlaceWithNoGame() {
        rootService.game = null
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        assertFails {
            rootService.playerActionService.place(piece, Pair(0, 0))
        }
    }

    /**
     * Testing if place correctly places a piece on the board at top-left corner
     */
    @Test
    fun testPlacePieceOnTopLeftCorner() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertEquals(Color.BLUE, game.currentGameState.field[0][0])
    }

    /**
     * Testing if place correctly places a piece on the board at top-right corner
     */
    @Test
    fun testPlacePieceOnTopRightCorner() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        val fieldSize = game.currentGameState.field.size
        rootService.playerActionService.place(piece, Pair(0, fieldSize - 1))

        assertEquals(Color.BLUE, game.currentGameState.field[0][fieldSize - 1])
    }

    /**
     * Testing if place correctly places a piece on the board at bottom-left corner
     */
    @Test
    fun testPlacePieceOnBottomLeftCorner() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        val fieldSize = game.currentGameState.field.size
        rootService.playerActionService.place(piece, Pair(fieldSize - 1, 0))

        assertEquals(Color.BLUE, game.currentGameState.field[fieldSize - 1][0])
    }

    /**
     * Testing if place correctly places a piece on the board at bottom-right corner
     */
    @Test
    fun testPlacePieceOnBottomRightCorner() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        val fieldSize = game.currentGameState.field.size
        rootService.playerActionService.place(piece, Pair(fieldSize - 1, fieldSize - 1))

        assertEquals(Color.BLUE, game.currentGameState.field[fieldSize - 1][fieldSize - 1])
    }

    /**
     * Testing if place fails when the piece goes out of bounds
     */
    @Test
    fun testPlacePieceOutOfBounds() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = participant.pieceList.first()

        val fieldSize = game.currentGameState.field.size
        assertFails {
            rootService.playerActionService.place(piece, Pair(fieldSize, fieldSize))
        }
    }

    /**
     * Testing if place removes the piece from the participant's pieceList
     */
    @Test
    fun testPlaceRemovesPieceFromList() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)
        val sizeBeforePlace = participant.pieceList.size

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertEquals(sizeBeforePlace - 1, participant.pieceList.size)
    }

    /**
     * Testing if place correctly sets lastPlacedPiece
     */
    @Test
    fun testPlaceSetsLastPlacedPiece() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        assertNull(participant.lastPlacedPiece)

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertNotNull(participant.lastPlacedPiece)
        assertEquals("O1", participant.lastPlacedPiece?.id)
    }

    /**
     * Testing if place correctly adds a new GameState to the linked list
     */
    @Test
    fun testPlaceCreatesNewGameState() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        assertNull(game.currentGameState.previousGameState)

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertNotNull(game.currentGameState.previousGameState)
    }

    /**
     * Testing if the linked list is correctly built after two moves
     */
    @Test
    fun testLinkedListAfterTwoMoves() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // First move - Blue
        val participant1 = game.currentGameState.participantList[0]
        val piece1 = Piece(arrayOf(intArrayOf(1)), "O1")
        participant1.pieceList.add(0, piece1)
        rootService.playerActionService.place(piece1, Pair(0, 0))

        // Second move - Yellow
        val participant2 = game.currentGameState.participantList[1]
        val piece2 = Piece(arrayOf(intArrayOf(1)), "O1")
        participant2.pieceList.add(0, piece2)

        val fieldSize = game.currentGameState.field.size
        rootService.playerActionService.place(piece2, Pair(0, fieldSize - 1))

        // Linked list should have 2 previous states
        assertNotNull(game.currentGameState.previousGameState)
        assertNotNull(game.currentGameState.previousGameState?.previousGameState)
    }

    /**
     * Testing if nextGameState is null after a new move (old future is discarded)
     */
    @Test
    fun testPlaceDiscardsOldFuture() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertNull(game.currentGameState.nextGameState)
    }

    /**
     * Testing if the participant switches after place
     */
    @Test
    fun testPlaceSwitchesParticipant() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        assertEquals(0, game.currentGameState.currentParticipantIndex)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertEquals(1, game.currentGameState.currentParticipantIndex)
    }

    /**
     * Testing if place fails when the move is illegal (not on corner for first move)
     */
    @Test
    fun testPlaceWithIllegalMove() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = participant.pieceList.first()

        assertFails {
            rootService.playerActionService.place(piece, Pair(7, 7))
        }
    }

    /**
     * Testing if controlledBy list is rotated after place with MultiControlledParticipant
     */
    @Test
    fun testPlaceRotatesControlledByForMultiControlledParticipant() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER),
            Triple("3", Color.RED, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // Find the MultiControlledParticipant
        val mcp = game.currentGameState.participantList
            .filterIsInstance<MultiControlledParticipant>()
            .firstOrNull()
        checkNotNull(mcp)

        val firstController = mcp.controlledBy[0]

        // Set MCP as current participant
        game.currentGameState.currentParticipantIndex =
            game.currentGameState.participantList.indexOf(mcp)

        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        mcp.pieceList.add(0, piece)

        val fieldSize = game.currentGameState.field.size
        rootService.playerActionService.place(piece, Pair(fieldSize - 1, fieldSize - 1))

        // First controller should now be at the end of the list
        assertEquals(firstController, mcp.controlledBy[2])
    }

    /**
     * Testing if [Refreshable.refreshAfterPlace] is called after a piece has been placed on the board.
     */
    @Test
    fun testRefreshAfterPlaceCalled() {
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val refreshableTest = TestRefreshable()
        rootService.playerActionService.addRefreshable(refreshableTest)

        val game = rootService.game
        checkNotNull(game)

        val participant = game.currentGameState.participantList[0]
        val piece = Piece(arrayOf(intArrayOf(1)), "O1")
        participant.pieceList.add(0, piece)

        rootService.playerActionService.place(piece, Pair(0, 0))

        assertTrue(refreshableTest.refreshAfterPlace)
    }
}