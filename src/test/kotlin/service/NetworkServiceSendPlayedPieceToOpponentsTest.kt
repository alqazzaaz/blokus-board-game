package service

import entity.*
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertFails
/**
 * test class for sending played pieces from opponents in the [NetworkService]
 */
class NetworkServiceSendPlayedPieceToOpponentsTest {
    private lateinit var rootService: RootService
    /**
     * prepares the test environment
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()
    }
    /**
     * Tests sending a piece fails when it is not the players turn
     */
    @Test
    fun testSendPieceNotMyTurn(){
        rootService.networkService.updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)

        val testStone = Piece(arrayOf(intArrayOf(1)),"O1")

        assertFails {
            rootService.networkService.sendPlayedPieceToOpponents(testStone, Pair(0,0))
        }
    }
    /**
     * Tests that sending a piece fails when there is no active game
     */
    @Test
    fun testSendPieceWithoutGame(){
        rootService.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        check(rootService.game==null )
        val testStone = Piece(arrayOf(intArrayOf(1)),"O1")
        assertFails {
            rootService.networkService.sendPlayedPieceToOpponents(testStone, Pair(0,0))
        }
    }
    /**
     * Tests the send method when all conditions are met
     */
    @Test
    fun testSendPieceSuccess(){

        val params = StartGameParams(listOf(Triple("player1", Color.BLUE, ParticipantType.PLAYER),
            Triple("player2", Color.YELLOW, ParticipantType.PLAYER)),
            isBigField = false,
            isBasicScoring = true
        )
        rootService.gameService.startGame(params)
        rootService.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)

        val game = rootService.game
        checkNotNull(game)
        val currentPlayer= game.currentGameState.participantList[0]
        val stone=currentPlayer.pieceList.first{ it.id=="O1"}

        rootService.networkService.sendPlayedPieceToOpponents(stone, Pair(0,0))

        assertEquals(ConnectionState.PLAYING_MY_TURN, rootService.networkService.connectionState)


    }

}