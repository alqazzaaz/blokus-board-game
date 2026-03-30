package service
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import entity.*
import kotlin.test.assertFails

/**
 * A simple test class to demonstrate a basic unit test for disconnect
 */
class NetworkServiceStartNewHostedGameTest {

    private lateinit var rootService: RootService
    private lateinit var networkService: NetworkService

    /**
     * Initialize service to set up the test environment. This function is executed before every test.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        networkService = rootService.networkService
    }

    /**
     * A simple test to check if the [NetworkService] is initialized.
     */
    @Test
    fun testIfSetUpWorked() {
        assertDoesNotThrow { networkService }
        assertEquals(ConnectionState.DISCONNECTED, networkService.connectionState)
    }

    /**
     * Testing, if startNewHostedGame can be called, without hostGame called before
     */
    @Test
    fun testWithoutHostGameCalledBeforeMyTurn() {
        val start = StartGameParams(
            listOf(
                Triple("P1", Color.BLUE, ParticipantType.NETWORK_PARTICIPANT),
                Triple("P2", Color.YELLOW, ParticipantType.PLAYER)
            ), true, true
        )

        // Shouldn't be possible
        assertFails { networkService.startNewHostedGame(start) }
    }

    /**
     * Testing, if startNewHostedGame can be called, with hostGame called before (my turn!)
     */
    @Test
    fun testWithHostGameCalledBeforeNotMyTurn() {

        networkService.hostGame(NetworkService.SECRET, "P1", "SID1")

        // wait for the program to do it's work
        val timeout = System.currentTimeMillis() + 5000
        while (networkService.connectionState != ConnectionState.WAITING_FOR_GUESTS) {
            Thread.sleep(100)
            if (System.currentTimeMillis() > timeout) error("Timeout!")
        }

        val start = StartGameParams(
            listOf(
                Triple("P1", Color.BLUE, ParticipantType.NETWORK_PARTICIPANT),
                Triple("P2", Color.YELLOW, ParticipantType.PLAYER)
            ), true, true
        )


        networkService.startNewHostedGame(start)

        assert(rootService.game != null)

        // Since I am P1 and blue, it should be my turn!
        assertEquals(ConnectionState.PLAYING_MY_TURN, networkService.connectionState)
    }

    /**
     * Testing, if startNewHostedGame can be called, with hostGame called before (not my turn!)
     */
    @Test
    fun testWithHostGameCalledBeforeMyTurn() {

        networkService.hostGame(NetworkService.SECRET, "P2", "SID2")

        // wait for the program to do it's work
        val timeout = System.currentTimeMillis() + 5000
        while (networkService.connectionState != ConnectionState.WAITING_FOR_GUESTS) {
            Thread.sleep(100)
            if (System.currentTimeMillis() > timeout) error("Timeout!")
        }

        val start = StartGameParams(
            listOf(
                Triple("P1", Color.BLUE, ParticipantType.NETWORK_PARTICIPANT),
                Triple("P2", Color.YELLOW, ParticipantType.PLAYER)
            ), true, true
        )


        networkService.startNewHostedGame(start)

        assert(rootService.game != null)

        // Since I am P1 and blue, it should be my turn!
        assertEquals(ConnectionState.WAIT_FOR_MY_TURN, networkService.connectionState)
    }
}