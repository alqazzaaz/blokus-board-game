package service

import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

/**
 * Test class for [NetworkService.hostGame]
 */
class NetworkServiceHostGameTest {

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
     * Testing if hostGame correctly connects to the server and updates the connection state.
     */
    @Test
    fun testHostGameConnects() {
        networkService.hostGame(NetworkService.SECRET, "P1", "SID_HOST_1")

        assertNotNull(networkService.client)
        assertEquals(ConnectionState.WAITING_FOR_HOST_CONFIRMATION, networkService.connectionState)

        networkService.disconnect()
    }

    /**
     * Testing if hostGame fails with a blank name.
     */
    @Test
    fun testHostGameWithBlankName() {
        assertFails {
            networkService.hostGame(NetworkService.SECRET, "", "SID_HOST_2")
        }
    }

    /**
     * Testing if hostGame fails with a blank secret.
     */
    @Test
    fun testHostGameWithBlankSecret() {
        assertFails {
            networkService.hostGame("", "P1", "SID_HOST_3")
        }
    }

    /**
     * Testing if hostGame fails when already connected.
     */
    @Test
    fun testHostGameWhenAlreadyConnected()  {
        networkService.hostGame(NetworkService.SECRET, "P1", "SID_HOST_4")

        assertFails {
            networkService.hostGame(NetworkService.SECRET, "P2", "SID_HOST_5")
        }

        networkService.disconnect()
    }

    /**
     * Testing if hostGame correctly waits for guests after connection is confirmed.
     */
    @Test
    fun testHostGameWaitsForGuests() {
        networkService.hostGame(NetworkService.SECRET, "P1", "SID_HOST_6")

        val timeout = System.currentTimeMillis() + 5000
        while (networkService.connectionState != ConnectionState.WAITING_FOR_GUESTS) {
            Thread.sleep(100)
            if (System.currentTimeMillis() > timeout) error("Timeout")
        }

        assertEquals(ConnectionState.WAITING_FOR_GUESTS, networkService.connectionState)

        networkService.disconnect()
    }
}