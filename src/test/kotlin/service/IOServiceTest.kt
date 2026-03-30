package service
import entity.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Test class for the [IOService]
 * checks the successful saving and loading of the game as well as
 * the interception of errors
 */

class IOServiceTest {

    private lateinit var rootService: RootService

    private val testFailPath = "test_savegame.blokus"

    /**
     * Prepares the test environment
     * creates a completely new [RootService] before each test
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /**
     * tests the success of saving and loading
     * it is checked whether it can be loaded without errors
     * and whether the player data is still correct
     */
    @Test
    fun testSaveAndLoadGame(){
        val params = StartGameParams(
            listOf(Triple("player1", Color.BLUE, ParticipantType.PLAYER),
                Triple("player2", Color.YELLOW, ParticipantType.PLAYER)
                ),
            isBigField = false,
            isBasicScoring = true
            )
        rootService.gameService.startGame(params)

        val game = rootService.game

        checkNotNull(game) {"no game is running yet "}

        rootService.ioService.saveGame(testFailPath)

        rootService.game = null

        rootService.ioService.loadGame(testFailPath)

        val loadGame = rootService.game

        checkNotNull(loadGame) {"game must be back after loading "}

        assertEquals(true, loadGame.isBasicScoring)
        assertEquals(false, loadGame.isNetwork)

        val playerList= loadGame.currentGameState.participantList

        assertEquals(2, playerList.size)

        assertEquals("player1", playerList[0].name )


    }
    /**
    * Tests the behavior when loading a file does not exist at all
    */
    @Test
    fun testLoadGameNoFile() {
        val falsePath = "file_doesnot_exist.blokus"

        assertFails {
            rootService.ioService.loadGame(falsePath)
        }
    }

    /**
     * Tests that network games may not be stored
     */
    @Test
    fun testSaveNetworkGame() {
        val params = StartGameParams(
            listOf(Triple("player1", Color.BLUE, ParticipantType.PLAYER),
                Triple("player2", Color.YELLOW, ParticipantType.NETWORK_PARTICIPANT)),
            isBigField = false,
            isBasicScoring = true
        )
        rootService.gameService.startGame(params)

        assertFails { rootService.ioService.saveGame(testFailPath) }
    }
}