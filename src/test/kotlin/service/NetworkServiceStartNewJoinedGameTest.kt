package service
import edu.udo.cs.sopra.ntf.InitMessage
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import edu.udo.cs.sopra.ntf.Color
import edu.udo.cs.sopra.ntf.GameMode
import kotlin.test.assertFails

/**
 * A simple test class to demonstrate a basic unit test for disconnect
 */
class NetworkServiceStartNewJoinedGameTest {

    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService
    private lateinit var networkServiceHost: NetworkService
    private lateinit var networkServiceGuest: NetworkService

    /**
     * Initialize service to set up the test environment. This function is executed before every test.
     */
    @BeforeTest
    fun setUp() {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()
        networkServiceHost = rootServiceHost.networkService
        networkServiceGuest = rootServiceGuest.networkService
    }

    /**
     * A simple test to check if the [NetworkService] is initialized.
     */
    @Test
    fun testIfSetUpWorked() {
        assertDoesNotThrow { networkServiceGuest }
        assertEquals(ConnectionState.DISCONNECTED, networkServiceGuest.connectionState)
    }

    /**
     * Testing, if startNewJoinedGame can be called, without joinGame called before
     */
    @Test
    fun testWithoutJoinGameCalledBefore() {

        val players = listOf(
            Pair("P1", Color.BLUE),
            Pair("P2", Color.YELLOW))

        // Shouldn't be possible
        assertFails { networkServiceGuest.startNewJoinedGame(InitMessage(players,
            GameMode.TWO_PLAYER, true), "P2")}
    }

    /**
     * Testing, if startNewJoinedGame can be called, with joinGame called before (not my turn!)
     */
    @Test
    fun testWithJoinGameCalledBeforeNotMyTurn() {

        val gameOne = networkServiceHost
        val gameTwo = networkServiceGuest

        gameOne.hostGame(NetworkService.SECRET, "P1", "SID3")

        gameTwo.joinGame(NetworkService.SECRET, "P2",
            ParticipantType.PLAYER,"SID3")

        // wait for the program to do it's work
        val timeout = System.currentTimeMillis() + 5000
        while (gameTwo.connectionState != ConnectionState.WAITING_FOR_INIT) {
            Thread.sleep(100)
            if (System.currentTimeMillis() > timeout) error("Timeout!")
        }

        val start = StartGameParams(
            listOf(
                Triple("P1", entity.Color.BLUE, ParticipantType.PLAYER),
                Triple("P2", entity.Color.YELLOW, ParticipantType.PLAYER)
            ), true, true
        )

        val players = listOf(
            Pair("P1", Color.BLUE),
            Pair("P2", Color.YELLOW))

        gameOne.startNewHostedGame(start)
        gameTwo.startNewJoinedGame(InitMessage(players, GameMode.TWO_PLAYER,
            true), "P2")

        assert(rootServiceGuest.game != null)

        // Since I am P2 and not blue, it should be not my turn!
        assertEquals(ConnectionState.WAIT_FOR_MY_TURN, gameTwo.connectionState)

        gameOne.disconnect()
        gameTwo.disconnect()
    }

    /**
     * Testing, if startNewJoinedGame can be called, with joinGame called before (my turn!)
     */
    @Test
    fun testWithJoinGameCalledBeforeMyTurn() {

        val gameOne = networkServiceHost
        val gameTwo = networkServiceGuest

        gameOne.hostGame(NetworkService.SECRET, "P1", "SID4")

        gameTwo.joinGame(NetworkService.SECRET, "P2",
            ParticipantType.PLAYER,"SID4")

        // wait for the program to do it's work
        val timeout = System.currentTimeMillis() + 5000
        while (gameTwo.connectionState != ConnectionState.WAITING_FOR_INIT) {
            Thread.sleep(100)
            if (System.currentTimeMillis() > timeout) error("Timeout!")
        }

        val start = StartGameParams(
            listOf(
                Triple("P2", entity.Color.BLUE, ParticipantType.PLAYER),
                Triple("P1", entity.Color.YELLOW, ParticipantType.PLAYER)
            ), true, true
        )

        val players = listOf(
            Pair("P2",Color.BLUE),
            Pair("P1",Color.YELLOW))

        gameOne.startNewHostedGame(start)
        gameTwo.startNewJoinedGame(InitMessage(players, GameMode.TWO_PLAYER,
            true), "P2")

        assert(rootServiceGuest.game != null)

        // Since I am P2 and blue, it should be my turn!
        assertEquals(ConnectionState.PLAYING_MY_TURN, gameTwo.connectionState)

        gameOne.disconnect()
        gameTwo.disconnect()
    }
}