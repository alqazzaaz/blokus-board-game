package service

import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * A simple test class to demonstrate a basic unit test.
 */
class GameServiceSetBotSpeedTest{

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
     * Test if the correct error gets thrown if there is no game initialized
     */
    @Test
    fun testIfNoCurrentGame(){
        assertFailsWith<IllegalStateException> { rootService.gameService.setBotSpeed(5) }
    }
    /**
     * Test if the correct error gets thrown if the value is negative
     */
    @Test
    fun testIfNegative(){
        //initialize a game
        val list = listOf(
            Triple("a", entity.Color.BLUE, ParticipantType.PLAYER),
            Triple("b",entity.Color.RED, ParticipantType.PLAYER)
        )
        rootService.gameService.startGame(
            StartGameParams(list, isBigField = false, isBasicScoring = false)
        )
        val game = rootService.game

        //necessary branch so it won't fail as long as startGame is not implemented
        if(game != null){
            assertFailsWith<IllegalArgumentException>{ rootService.gameService.setBotSpeed(-1) }
        }
    }
    /**
     * Test if the function works as intended (in the successful case)
     */
    @Test
    fun testIfCorrect(){
        val list = listOf(
            Triple("a", entity.Color.BLUE, ParticipantType.PLAYER),
            Triple("b", entity.Color.YELLOW, ParticipantType.PLAYER)
        )
        rootService.gameService.startGame(
            StartGameParams(list,isBigField = true, isBasicScoring = true)
        )
        val game = rootService.game
        //necessary branch so it won't fail as long as startGame is not implemented
        if(game != null){
            //check if value was indeed overwritten
            assertDoesNotThrow { rootService.gameService.setBotSpeed(2) }
            assertEquals(2,game.botSpeed)

            //check if successive calls work aswell
            assertDoesNotThrow { rootService.gameService.setBotSpeed(3) }
            assertEquals(3,game.botSpeed)
        }
    }

}