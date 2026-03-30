package service
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.Test

/**
 * Test class for checking the connection state updates in the [NetworkService]
 */
class NetworkServiceUpdateConnectionStateTest{
    private lateinit var rootService: RootService
    /**
     * Prepares the test environment
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }
    /**
     * Tests that the connection state is updated correctly
     */
    @Test
    fun testUpdateConnectionState(){
        assertEquals(ConnectionState.DISCONNECTED,rootService.networkService.connectionState)
        rootService.networkService.updateConnectionState(ConnectionState.CONNECTED)
        assertEquals(ConnectionState.CONNECTED,rootService.networkService.connectionState)
    }
}