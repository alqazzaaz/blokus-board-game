package service
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * A simple test class to demonstrate a basic unit test for disconnect
 */
class NetworkServiceDisconnectTest {

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
     * Testing Disconnect without an existing connection: Should not throw an error!
     */
    @Test
    fun testDisconnectWithoutConnection() {
        assertNull(networkService.client)

        networkService.disconnect()

        assertNull(networkService.client)
        assertEquals(ConnectionState.DISCONNECTED, networkService.connectionState)
    }

    /**
     * Testing Disconnect with an existing connection: Should change back to DISCONNECTED
     */
    @Test
    fun testDisconnectWithConnection() {
        networkService.hostGame(NetworkService.SECRET, "P1", "SID")

        // Check, if hostGame worked
        assert(networkService.client != null)
        assertEquals(ConnectionState.WAITING_FOR_HOST_CONFIRMATION, networkService.connectionState)

        networkService.disconnect()

        assertNull(networkService.client)
        assertEquals(ConnectionState.DISCONNECTED, networkService.connectionState)
    }
}