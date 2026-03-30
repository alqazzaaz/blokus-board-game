package service

import entity.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertEquals

/**
 * A simple test class to demonstrate a basic unit test.
 */
class PlayerActionServiceRedoTest {

    /**
     * This service is initialized in the [setUp] function hence it is a late-initialized property.
     */
    private lateinit var rootService: RootService

    /**
     * Initialize service to set up the test environment. This function is executed before every test.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /**
     * A simple test to check if the [RootService] is initialized.
     */
    @Test
    fun testIfSetUpWorked() {
        assertDoesNotThrow("The root service should be initialized.") { rootService }
    }

    /**
     * Testing, if redo can't be called with no game running
     */
    @Test
    fun testIfRedoPossibleWithoutGame() {
        // simulate, that there is no game
        rootService.game = null

        // Can't redo, if there is no game
        assertFails {
            rootService.playerActionService.redo()
        }
    }

    /**
     * Testing, if redo can't be called with a network-game running
     */
    @Test
    fun testIfRedoPossibleInNetworkGame() {

        // start a network-game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.NETWORK_PARTICIPANT)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // Ensures, that startGame identifies the game as a network game
        assert(game.isNetwork)

        // initialize a field, which is null everywhere
        val field: Array<Array<Color?>> = Array(14) { Array(14) { null } }

        // simulate, that there is a nextGameState
        game.currentGameState.nextGameState = GameState(
            field,
            0,
            null,
            null,
            game.currentGameState.participantList
        )

        assertFails {
            rootService.playerActionService.redo()
        }
    }

    /**
     * Testing, if redo can't be called with no next game state available
     */
    @Test
    fun testIfRedoPossibleWithNullNextState() {
        // start a game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // Ensures, that the next game state is null
        assert(game.currentGameState.nextGameState == null)

        // Should be not possible with a next game state of null
        assertFails {
            rootService.playerActionService.redo()
        }
    }

    /**
     * Testing, if redo can't be called with a game running only by bots
     */
    @Test
    fun testIfRedoPossibleWithOnlyBots() {
        // start an only bot game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.EASY_BOT),
            Triple("2", Color.GREEN, ParticipantType.STRONG_BOT),),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // initialize a field, which is null everywhere
        val field: Array<Array<Color?>> = Array(14) { Array(14) { null } }

        // simulate, that there is a nextGameState
        game.currentGameState.nextGameState = GameState(
            field,
            0,
            null,
            null,
            game.currentGameState.participantList
        )

        // Should fail, because there are only bots
        assertFails {
            rootService.playerActionService.redo()
        }
    }

    /**
     * Testing, if redo is skipping bots
     */
    @Test
    fun testIfRedoIsSkippingBots() {
        // start a game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.GREEN, ParticipantType.STRONG_BOT),),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // initialize a field, which is null everywhere
        val field: Array<Array<Color?>> = Array(14) { Array(14) { null } }

        // simulate, that there is a nextGameState (Bots turn!)
        game.currentGameState.nextGameState = GameState(
            field,
            1,
            null,
            null,
            game.currentGameState.participantList
        )

        val nextGameState = game.currentGameState.nextGameState
        checkNotNull(nextGameState)

        // simulate, that there is a nextGameState to the nextGameState (Player's turn!)
        nextGameState.nextGameState = GameState(
            field,
            0,
            null,
            null,
            game.currentGameState.participantList
        )

        // Should skip the bot
        rootService.playerActionService.redo()

        // currentParticipantIndex should be 0 (from the player!)
        assertEquals(0, game.currentGameState.currentParticipantIndex)

    }

    /**
     * Testing, if redo works correctly in a regular case: Saving the field and the currentParticipantIndex
     */
    @Test
    fun testRedoInRegularCase() {
        // start a game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // initialize a field, which is null everywhere
        val field: Array<Array<Color?>> = Array(14) { Array(14) { null } }

        // Will set up a state, which should be remembered, when redo() is called
        field[1][2] = Color.BLUE
        field[3][4] = Color.RED
        field[5][6] = Color.GREEN
        field[7][8] = Color.YELLOW

        // simulate, that there is a nextGameState
        game.currentGameState.nextGameState = GameState(
            field,
            1,
            null,
            null,
            game.currentGameState.participantList
        )

        rootService.playerActionService.redo()

        // Should be correct
        assertEquals(Color.BLUE, field[1][2])
        assertEquals(Color.RED, field[3][4])
        assertEquals(Color.GREEN, field[5][6])
        assertEquals(Color.YELLOW, field[7][8])
        assertEquals(1, game.currentGameState.currentParticipantIndex)
    }

    /**
     * Testing, if redo is skipping participants with no actions left
     */
    @Test
    fun testIfRedoIsSkippingParticipantsWithNoActions() {
        // start a game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.GREEN, ParticipantType.PLAYER)),
            false, true))

        val game = rootService.game
        checkNotNull(game)

        // initialize a field, which is null everywhere
        val field: Array<Array<Color?>> = Array(14) { Array(14) { null } }

        // simulate, that there is a nextGameState (Player with no actions available turn!)
        game.currentGameState.nextGameState = GameState(
            field,
            1,
            null,
            null,
            game.currentGameState.participantList
        )

        val nextGameState = game.currentGameState.nextGameState
        checkNotNull(nextGameState)

        // simulate, that the player has no actions available
        nextGameState.participantList[1].actionAvailable = false

        // simulate, that there is a nextGameState to the nextGameState (Player's turn with actions)
        nextGameState.nextGameState = GameState(
            field,
            0,
            null,
            null,
            game.currentGameState.participantList
        )

        // Should skip the player with no actions available
        rootService.playerActionService.redo()

        // currentParticipantIndex should be 0 (with actions left)
        assertEquals(0, game.currentGameState.currentParticipantIndex)
    }

    /**
     * Testing, if redo can't be called, when there are only bots available (and a multi controller participant)
     */
    @Test
    fun testIfRedoPossibleWithThreeBotsAndMultiControlledParticipant() {
        // start a game properly (extra multi controlled participant will be added)
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.EASY_BOT),
            Triple("2", Color.GREEN, ParticipantType.STRONG_BOT),
            Triple("3", Color.YELLOW, ParticipantType.EASY_BOT)),
            true, true))

        val game = rootService.game
        checkNotNull(game)

        // Should not be possible, because the shared player is controlled only by bots
        assertFails {
            rootService.playerActionService.redo()
        }
    }
}