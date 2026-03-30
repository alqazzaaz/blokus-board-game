package service

import entity.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A simple test class to demonstrate a basic unit test.
 */
class PlayerActionServiceFlipTest {

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
     * Tests will call this method to compare the expected figure of Piece (ID) with the figure, which resulted
     * after the method "rotate".
     */
    private fun assertFigure(originalID: String, expFigAfterRotate: Array<IntArray>) {

        val player = Player("Test",true,Color.BLUE)
        val piece = player.pieceList.find { it.id == originalID }
        checkNotNull(piece)

        rootService.playerActionService.flip(piece)

        for (i in expFigAfterRotate.indices) {
            for (j in expFigAfterRotate[i].indices) {
                assertEquals(expFigAfterRotate[i][j], piece.figure[i][j],
                    "Piece-ID: $originalID: rotation didn't work properly!\n" +
                            "Mismatch happened at position [$i][$j]!")
            }
        }
    }

    /**
     * Test if refresh is called
     */
    @Test
    fun testRefreshAfterRotateCalled() {
        val refreshableTest = TestRefreshable()
        rootService.playerActionService.addRefreshable(refreshableTest)

        rootService.playerActionService.flip(Piece(arrayOf(intArrayOf(1)),"O1"))
        assertTrue(refreshableTest.refreshAfterFlip)
    }

    /**
     * Test for Piece-ID: I5
     */
    @Test
    fun testI5() {
        assertFigure("I5", arrayOf(
            intArrayOf(1,1,1,1,1)))
    }

    /**
     * Test for Piece-ID: N5
     */
    @Test
    fun testN5() {
        assertFigure("N5", arrayOf(
            intArrayOf(1,1,1,0),
            intArrayOf(0,0,1,1)))
    }

    /**
     * Test for Piece-ID: V5
     */
    @Test
    fun testV5() {
        assertFigure("V5", arrayOf(
            intArrayOf(0,0,1),
            intArrayOf(0,0,1),
            intArrayOf(1,1,1)))
    }

    /**
     * Test for Piece-ID: T5
     */
    @Test
    fun testT5() {
        assertFigure("T5", arrayOf(
            intArrayOf(0,1,0),
            intArrayOf(0,1,0),
            intArrayOf(1,1,1)))
    }

    /**
     * Test for Piece-ID: U5
     */
    @Test
    fun testU5() {
        assertFigure("U5", arrayOf(
            intArrayOf(1,1,1),
            intArrayOf(1,0,1)))
    }

    /**
     * Test for Piece-ID: L5
     */
    @Test
    fun testL5() {
        assertFigure("L5", arrayOf(
            intArrayOf(1,1,1,1),
            intArrayOf(0,0,0,1)))
    }

    /**
     * Test for Piece-ID: Y5
     */
    @Test
    fun testY5() {
        assertFigure("Y5", arrayOf(
            intArrayOf(1,1,1,1),
            intArrayOf(0,0,1,0)))
    }

    /**
     * Test for Piece-ID: Z5
     */
    @Test
    fun testZ5() {
        assertFigure("Z5", arrayOf(
            intArrayOf(0,0,1),
            intArrayOf(1,1,1),
            intArrayOf(1,0,0)))
    }

    /**
     * Test for Piece-ID: W5
     */
    @Test
    fun testW5() {
        assertFigure("W5", arrayOf(
            intArrayOf(0,0,1),
            intArrayOf(0,1,1),
            intArrayOf(1,1,0)))
    }

    /**
     * Test for Piece-ID: P5
     */
    @Test
    fun testP5() {
        assertFigure("P5", arrayOf(
            intArrayOf(1,1),
            intArrayOf(1,1),
            intArrayOf(0,1)))
    }

    /**
     * Test for Piece-ID: X5
     */
    @Test
    fun testX5() {
        assertFigure("X5", arrayOf(
            intArrayOf(0,1,0),
            intArrayOf(1,1,1),
            intArrayOf(0,1,0)))
    }

    /**
     * Test for Piece-ID: F5
     */
    @Test
    fun testF5() {
        assertFigure("F5", arrayOf(
            intArrayOf(0,1,0),
            intArrayOf(1,1,1),
            intArrayOf(0,0,1)))
    }

    /**
     * Test for Piece-ID: Z4
     */
    @Test
    fun testZ4() {
        assertFigure("Z4", arrayOf(
            intArrayOf(0,1),
            intArrayOf(1,1),
            intArrayOf(1,0)))
    }

    /**
     * Test for Piece-ID: I4
     */
    @Test
    fun testI4() {
        assertFigure("I4", arrayOf(
            intArrayOf(1,1,1,1)))
    }

    /**
     * Test for Piece-ID: L4
     */
    @Test
    fun testL4() {
        assertFigure("L4", arrayOf(
            intArrayOf(0,0,1),
            intArrayOf(1,1,1)))
    }

    /**
     * Test for Piece-ID: O4
     */
    @Test
    fun testO4() {
        assertFigure("O4", arrayOf(
            intArrayOf(1,1),
            intArrayOf(1,1)))
    }

    /**
     * Test for Piece-ID: T4
     */
    @Test
    fun testT4() {
        assertFigure("T4", arrayOf(
            intArrayOf(0,1,0),
            intArrayOf(1,1,1)))
    }

    /**
     * Test for Piece-ID: I3
     */
    @Test
    fun testI3() {
        assertFigure("I3", arrayOf(
            intArrayOf(1,1,1)))
    }

    /**
     * Test for Piece-ID: V3
     */
    @Test
    fun testV3() {
        assertFigure("V3", arrayOf(
            intArrayOf(1,1),
            intArrayOf(1,0)))
    }

    /**
     * Test for Piece-ID: I2
     */
    @Test
    fun testI2() {
        assertFigure("I2", arrayOf(
            intArrayOf(1,1)))
    }

    /**
     * Test for Piece-ID: O1
     */
    @Test
    fun testO1() {
        assertFigure("O1", arrayOf(
            intArrayOf(1)))
    }
}