package service

import entity.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails

/**
 * Test class for receiving played pieces from opponents in the [NetworkService]
 */
class NetworkServiceReceivePlayedPieceFromOpponentsTest {
    private lateinit var rootService: RootService

    /**
     * Prepares the test environment
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()
    }
    /**
     * Tests receiving a piece fails when it is the player own turn
     */
    @Test
    fun testReceivePieceNotWaiting() {
        rootService.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)

        val testMessage = edu.udo.cs.sopra.ntf.ActionMessage(
            isMirrored = false,
            coords = Pair(0, 0),
            rotation = edu.udo.cs.sopra.ntf.Rotation.NONE,
            blockType = edu.udo.cs.sopra.ntf.BlockType.O1
        )

        assertFails {
            rootService.networkService.receivePlayedPieceFromOpponents(testMessage)
        }
    }
    /**
     * Tests receiving a piece fails when there is no active game
     */
    @Test
    fun testReceivePlayedPieceWithoutGame() {
        rootService.networkService.updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)

        check(rootService.game == null)
        val testMessage = edu.udo.cs.sopra.ntf.ActionMessage(
            isMirrored = false,
            coords = Pair(0, 0),
            rotation = edu.udo.cs.sopra.ntf.Rotation.NINETY,
            blockType = edu.udo.cs.sopra.ntf.BlockType.O1
        )

        assertFails {
            rootService.networkService.receivePlayedPieceFromOpponents(testMessage)
        }
    }
}