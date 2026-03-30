package service

import entity.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test class for [GameService.score].
 * Covers Basic Scoring, Advanced Scoring (including +15 and +5 bonuses),
 * the 2-player 4-color combined score case.
 */
class GameServiceScoreTest {

    private fun leeresFeld() = Array(20) { Array<Color?>(20) { null } }

    /**
     * Tests that [GameService.score] correctly calculates Basic Scoring.
     *
     * In Basic Scoring, the remaining squares of unplaced pieces are counted.
     * A lower score is better, the participant with fewer remaining squares wins.
     *
     * Preconditions:
     * - A game is running with Basic Scoring enabled.
     * - Alice has no remaining pieces (score = 0).
     * - Bob has one 1x1 piece remaining (score = 1).
     *
     * Expected result:
     * - Alice is ranked first with 0 points.
     * - Bob is ranked second with 1 point.
     */
    @Test
    fun `score Basic Scoring - weniger Reststeine gewinnt`() {
        val rootService = RootService()

        val alice = Player(name = "Alice", actionAvailable = false, color = Color.BLUE)
        alice.pieceList.clear()

        val bob = Player(name = "Bob", actionAvailable = false, color = Color.RED)
        bob.pieceList.clear()
        bob.pieceList.add(Piece(figure = arrayOf(intArrayOf(1)), id = "O1"))

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice, bob),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        var ergebnis: List<Pair<Int, Participant>>? = null
        rootService.gameService.addRefreshable(object : Refreshable {
            override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
                ergebnis = participantScores
            }
        })

        rootService.gameService.score()

        val scores = checkNotNull(ergebnis)
        assertEquals("Alice", scores[0].second.name)
        assertEquals(0, scores[0].first)
        assertEquals("Bob", scores[1].second.name)
        assertEquals(1, scores[1].first)
    }

    /**
     * Tests that [GameService.score] awards +15 bonus points in Advanced Scoring
     * when a participant has placed all their pieces.
     *
     * In Advanced Scoring, remaining pieces give negative points (-1 per square).
     * If no pieces remain, the participant receives +15 bonus points.
     *A higher score is better
     *
     * Preconditions:
     * - A game is running with Advanced Scoring enabled.
     * - Alice has placed all pieces → receives +15 bonus points.
     * - Bob has one 1x1 piece remaining → receives -1 point.
     *
     * Expected result:
     * - Alice is ranked first with 15 points.
     * - Bob is ranked second with -1 point.
     */
    @Test
    fun `score Advanced Scoring - alle Steine gelegt gibt 15 Bonuspunkte`() {
        val rootService = RootService()

        val alice = Player(name = "Alice", actionAvailable = false, color = Color.BLUE)
        alice.pieceList.clear()
        val bob = Player(name = "Bob", actionAvailable = false, color = Color.RED)
        bob.pieceList.clear()
        bob.pieceList.add(Piece(figure = arrayOf(intArrayOf(1)), id = "X1"))

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice, bob),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = false,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        var ergebnis: List<Pair<Int, Participant>>? = null
        rootService.gameService.addRefreshable(object : Refreshable {
            override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
                ergebnis = participantScores
            }
        })

        rootService.gameService.score()
        val scores = checkNotNull(ergebnis)
        assertEquals("Alice", scores[0].second.name)
        assertEquals(15, scores[0].first)
        assertEquals("Bob", scores[1].second.name)
        assertEquals(-1, scores[1].first)
    }

    /**
     * Tests that [GameService.score] awards an additional +5 bonus points in Advanced Scoring
     * when a participant's last placed piece was the smallest piece (id = "O1", 1x1).
     *
     * Preconditions:
     * - A game is running with Advanced Scoring enabled.
     * - Alice has placed all pieces (+15) and her last piece was "O1" (+5) → total: 20 points.
     * - Bob has placed all pieces (+15) but his last piece was not "O1" → total: 15 points.
     *
     * Expected result:
     * -Alice is ranked first with 20 points.
     * -Bob is ranked second with 15 points.
     */
    @Test
    fun `score Advanced Scoring - letzter Stein O1 gibt 5 Bonuspunkte`() {
        val rootService = RootService()

        val alice = Player(name = "Alice", actionAvailable = false, color = Color.BLUE)
        alice.pieceList.clear()
        alice.lastPlacedPiece = Piece(figure = arrayOf(intArrayOf(1)), id = "O1")

        val bob = Player(name = "Bob", actionAvailable = false, color = Color.RED)
        bob.pieceList.clear()

        val gameState = GameState(
            field = leeresFeld(),
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice, bob),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = false,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )
        var ergebnis: List<Pair<Int, Participant>>? = null
        rootService.gameService.addRefreshable(object : Refreshable {
            override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
                ergebnis = participantScores
            }
        })
        rootService.gameService.score()

        val scores = checkNotNull(ergebnis)
        assertEquals("Alice", scores[0].second.name)
        assertEquals(20, scores[0].first)
        assertEquals("Bob", scores[1].second.name)
        assertEquals(15, scores[1].first)
    }

    /**
     * Tests that [GameService.score] correctly combines scores in the 2-player 4-color variant.
     *
     * In this variant, each player controls 2 colors. The same participant object appears
     * twice in the participant list. The scores of both entries are added together.
     *
     * Since alice and bob are the same objects appearing twice, they share the same pieceList.
     * Both entries therefore see and count the same remaining pieces.
     *
     * Preconditions:
     * A game is running with Basic Scoring enabled.
     * participantList = [alice, bob, alice, bob]
     * Alice has 1 remaining piece → counted twice → total: 1 + 1 = 2 points.
     * Bob has 2 remaining pieces → counted twice → total: 2 + 2 = 4 points.
     *
     * Expected results:
     * The result list has exactly 2 entries.
     * Alice is ranked first with 2 points.
     * Bob is ranked second with 4 points.
     */
    @Test
    fun `score Basic Scoring - 2 Spieler 4 Farben zaehlt Punkte zusammen`() {
        val rootService = RootService()
        val alice = Player(name = "Alice", actionAvailable = false, color = Color.BLUE)
        alice.pieceList.clear()
        alice.pieceList.add(Piece(figure = arrayOf(intArrayOf(1)), id = "A1"))

        val bob = Player(name = "Bob", actionAvailable = false, color = Color.RED)
        bob.pieceList.clear()
        bob.pieceList.add(Piece(figure = arrayOf(intArrayOf(1)), id = "B1"))
        bob.pieceList.add(Piece(figure = arrayOf(intArrayOf(1)), id = "C1"))
        val gameState = GameState(
            field = Array(20) { Array<Color?>(20) { null } },
            currentParticipantIndex = 0,
            participantList = mutableListOf(alice, bob, alice, bob),
            nextGameState = null,
            previousGameState = null
        )

        rootService.game = BlokusGame(
            isBasicScoring = true,
            isNetwork = false,
            botSpeed = 1,
            currentGameState = gameState
        )

        var ergebnis: List<Pair<Int, Participant>>? = null
        rootService.gameService.addRefreshable(object : Refreshable {
            override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
                ergebnis = participantScores
            }
        })

        rootService.gameService.score()
        val scores = checkNotNull(ergebnis)
        assertEquals(2, scores.size)
        assertEquals("Alice", scores[0].second.name)
        assertEquals(2, scores[0].first)
        assertEquals("Bob", scores[1].second.name)
        assertEquals(4, scores[1].first)
    }

    /**
     * Tests that [GameService.score] throws an [IllegalStateException]
     * when no game is currently running
     *
     * Preconditions:
     * No game has been started (rootService.game is null).
     *
     * Expected result:
     * An [IllegalStateException] is thrown.
     */
    @Test
    fun `score wirft Fehler wenn kein Spiel laeuft`() {
        val rootService = RootService()
        assertFailsWith<IllegalStateException> {
            rootService.gameService.score()
        }
    }
}