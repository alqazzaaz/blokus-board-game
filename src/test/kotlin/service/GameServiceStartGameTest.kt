package service

import entity.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * A simple test class to demonstrate a basic unit test.
 */
class GameServiceStartGameTest {

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
     * Testing, if startGame starts a game correctly (and doesn't, if a game is already running)
     */
    @Test
    fun testGameRunning() {
        check(rootService.game == null)

        //start a game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            true, true))

        // should be not null anymore
        checkNotNull(rootService.game)

        // Can't start a game if a game is already running.
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.BLUE, ParticipantType.PLAYER),
                Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
                true, true))
        }
    }

    /**
     * Testing, if startGame can't be executed with a wrong amount of participants
     */
    @Test
    fun testAmountParticipants() {
        // Can't start a game with <2 players
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.RED, ParticipantType.PLAYER)),
                true, true))
        }
        // Can't start a game with >=4 players (also because there is a duplicate color!)
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.RED, ParticipantType.PLAYER),
                Triple("2", Color.BLUE, ParticipantType.PLAYER),
                Triple("3", Color.BLUE, ParticipantType.PLAYER),
                Triple("4", Color.GREEN, ParticipantType.PLAYER),
                Triple("5", Color.YELLOW, ParticipantType.PLAYER)),
                true, true))
        }
    }

    /**
     * Testing, if startGame checks invalid color choices for the case of 2 players in a big field.
     */
    @Test
    fun testTwoPlayerBigFieldColors() {
        // Invalid Color Choice: has to be {(Blue,Red),(Yellow,Green)}
        // -> Different players cant have (blue and red) or (yellow and green)
        // if it's a big field.
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.BLUE, ParticipantType.PLAYER),
                Triple("2", Color.RED, ParticipantType.PLAYER)),
                true, true))
        }
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.YELLOW, ParticipantType.PLAYER),
                Triple("2", Color.GREEN, ParticipantType.PLAYER)),
                true, true))
        }
    }

    /**
     * Testing, if startGame duplicates names correctly for the 2 player case with bigField
     * and also sets up the colors correctly for this case.
     */
    @Test
    fun testTwoPlayerBigField() {
        // order: blue -> yellow -> red -> green
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.GREEN, ParticipantType.PLAYER)),
            true, true))

        val game = rootService.game
        checkNotNull(game)
        val playerAmount = game.currentGameState.participantList.size

        // Check if the players are correctly added.
        assertEquals(4, playerAmount)

        // Check if the playernames got duplicated correctly in that case.
        assertEquals("1", game.currentGameState.participantList[0].name)
        assertEquals("2", game.currentGameState.participantList[1].name)
        assertEquals("1", game.currentGameState.participantList[2].name)
        assertEquals("2", game.currentGameState.participantList[3].name)
        // Check if the colors are set up in a correct order in that case.
        assertEquals(Color.BLUE ,game.currentGameState.participantList[0].color)
        assertEquals(Color.YELLOW, game.currentGameState.participantList[1].color)
        assertEquals(Color.RED, game.currentGameState.participantList[2].color)
        assertEquals(Color.GREEN, game.currentGameState.participantList[3].color)
    }

    /**
     * Testing, if the order is correct for the 2-4 player cases (2 player bigField already tested!)
     */
    @Test
    fun testAllPossibleOrders() {
        //--------------------------------------------------------------------------------------------
        // Initialize Game: 2 Player case smallField
        // order: blue -> yellow -> red -> green
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.YELLOW, ParticipantType.PLAYER),
            Triple("2", Color.BLUE, ParticipantType.PLAYER)),
            false, true))

        var game = rootService.game
        checkNotNull(game)

        // Check if the 2-player order is correct.
        assertEquals("2", game.currentGameState.participantList[0].name)
        assertEquals("1", game.currentGameState.participantList[1].name)
        //--------------------------------------------------------------------------------------------
        rootService.game = null
        // Initialize Game: 3 Player case
        // order: blue -> yellow -> red -> green
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.YELLOW, ParticipantType.PLAYER),
            Triple("2", Color.BLUE, ParticipantType.PLAYER),
            Triple("3", Color.RED, ParticipantType.PLAYER)),
            true, true))

        game = rootService.game
        checkNotNull(game)

        // Check if the 3-player order is correct.
        assertEquals("2", game.currentGameState.participantList[0].name)
        assertEquals("1", game.currentGameState.participantList[1].name)
        assertEquals("3", game.currentGameState.participantList[2].name)
        //--------------------------------------------------------------------------------------------
        rootService.game = null
        // Initialize Game: 4 Player case
        // order: not color based anymore!
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.RED, ParticipantType.PLAYER),
            Triple("2", Color.BLUE, ParticipantType.PLAYER),
            Triple("3", Color.GREEN, ParticipantType.PLAYER),
            Triple("4", Color.YELLOW, ParticipantType.PLAYER)),
            true, true))

        game = rootService.game
        checkNotNull(game)

        // Check if the 4-player order is correct (Colors doesn't matter in this case!)
        assertEquals("1", game.currentGameState.participantList[0].name)
        assertEquals("2", game.currentGameState.participantList[1].name)
        assertEquals("3", game.currentGameState.participantList[2].name)
        assertEquals("4", game.currentGameState.participantList[3].name)
    }

    /**
     * Testing, if startGame adds a multi controlled participant for the 3 player case.
     */
    @Test
    fun testMultiControlledParticipant() {
        //start a game properly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.RED, ParticipantType.PLAYER),
            Triple("3", Color.YELLOW, ParticipantType.PLAYER)),
            true, true))

        val game = rootService.game
        checkNotNull(game)

        val playerAmount = game.currentGameState.participantList.size

        // There needs to be 4 players (the added player has to be a MultiControlledParticipant)
        assertEquals(4, playerAmount)

        // There needs to be an automatically added MultiControlledParticipant
        assert(
            game.currentGameState.participantList.any { it is MultiControlledParticipant }
        )
    }

    /**
     * Testing, if startGame can't be executed with duplicate player names
     */
    @Test
    fun testDuplicatePlayerNames() {
        // Can't start a game with a duplicate name
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.RED, ParticipantType.PLAYER),
                Triple("1", Color.YELLOW, ParticipantType.PLAYER)),
                true, true))
        }
    }

    /**
     * Testing, if startGame can't be executed with an invalid color selection
     */
    @Test
    fun testDuplicateColors() {
        // Can't start a game with duplicate colors
        assertFails {
            rootService.gameService.startGame(StartGameParams(listOf(
                Triple("1", Color.BLUE, ParticipantType.PLAYER),
                Triple("2", Color.BLUE, ParticipantType.PLAYER)),
                true, true))
        }
    }

    /**
     * Testing, if startGame initializes the participants correctly
     */
    @Test
    fun testParticipantInitialization() {
        //start a game with 2 participants
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.EASY_BOT)),
            true, true))

        val game = rootService.game
        checkNotNull(game)

        // Check names
        assertEquals("1",game.currentGameState.participantList[0].name)
        assertEquals("2",game.currentGameState.participantList[1].name)

        // Check colors
        assertEquals(Color.BLUE,game.currentGameState.participantList[0].color)
        assertEquals(Color.YELLOW,game.currentGameState.participantList[1].color)

        // Check action available
        assert(game.currentGameState.participantList[0].actionAvailable)
        assert(game.currentGameState.participantList[1].actionAvailable)

    }

    /**
     * Testing, if startGame sets up the correct size for the field
     */
    @Test
    fun testFieldSize() {
        //start a game with a big field of 20x20
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            true, true))

        var game = rootService.game
        checkNotNull(game)

        // size must be 20
        assertEquals(20, game.currentGameState.field.size)

        // reset game
        rootService.game = null

        //start a game with a big field of 14x14
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, true))

        game = rootService.game
        checkNotNull(game)

        // size must be 14
        assertEquals(14, game.currentGameState.field.size)

    }

    /**
     * Testing, if startGame sets up the correct Scoring Selection
     */
    @Test
    fun testScoringMethod() {
        //start a game with advanced Scoring
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            true, false))

        var game = rootService.game
        checkNotNull(game)

        // IsBasicScoring must be false
        assertFalse(game.isBasicScoring)

        rootService.game = null

        //start a game with basic Scoring
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            true, true))

        game = rootService.game
        checkNotNull(game)

        // isBasicScoring must be true
        assert(game.isBasicScoring)

    }

    /**
     * Testing, if startGame initializes the board correctly with null
     */
    @Test
    fun testBoardInitiallyNull() {
        //start a game regularly (with a 14x14 field)
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, false))

        val game = rootService.game
        checkNotNull(game)

        // check if no color is set (null)
        for (i in 0 until game.currentGameState.field.size) {
            for (j in 0 until game.currentGameState.field[i].size) {
                assertNull(game.currentGameState.field[i][j])
            }
        }
    }

    /**
     * Testing, if startGame initializes previous and next game states correctly with null
     */
    @Test
    fun testLinkedGameStatesInitiallyNull() {
        //start a game regularly
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, false))

        val game = rootService.game
        checkNotNull(game)

        // Check if these are null
        assertNull(game.currentGameState.previousGameState)
        assertNull(game.currentGameState.nextGameState)

    }

    /**
     * Testing, if startGame identifies a network game correctly based on the participants
     */
    @Test
    fun testIsNetworkGame() {
        //start a game regularly with no network participant
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
            false, false))

        var game = rootService.game
        checkNotNull(game)

        // no network game, if no network participant
        assertFalse(game.isNetwork)

        // reset game
        rootService.game = null

        //start a game regularly with a network participant
        rootService.gameService.startGame(StartGameParams(listOf(
            Triple("1", Color.BLUE, ParticipantType.PLAYER),
            Triple("2", Color.YELLOW, ParticipantType.NETWORK_PARTICIPANT)),
            false, false))

        game = rootService.game
        checkNotNull(game)

        assert(game.isNetwork)
    }

    /**
     * Testing, if startGame calls the refresh after being executed
     */
    @Test
    fun testRefreshCalled() {
        val refreshableTest = TestRefreshable()
        rootService.gameService.addRefreshable(refreshableTest)

        // Must be initially false
        assertFalse(refreshableTest.refreshAfterInitializeGame)

        //start a game regularly
        rootService.gameService.startGame(
            StartGameParams(
                listOf(
                    Triple("1", Color.BLUE, ParticipantType.PLAYER),
                    Triple("2", Color.YELLOW, ParticipantType.PLAYER)),
                false, false))

        // Must be called now
        assert(refreshableTest.refreshAfterInitializeGame)
    }
}