package service.bot

import service.AbstractRefreshingService
import service.RootService
import service.bot.BotSettings
import entity.*
import service.PlayerActionService
import kotlin.concurrent.thread

/**
 * Provides services for bots. Generates a suitable bot action and
 * performs it
 *
 * @param rootService is to be provided to access other layers of the service layer
 * ink. boards, pieces
 */
class BotTurnService(private val rootService: RootService): AbstractRefreshingService()  {
    private val boardHandlerService = BoardHandlerService()
    private val botScoreService = BotScoreService()
    private val playerActionService = PlayerActionService(rootService)



    /**
     * finds a suitable action for active bot in difficulty easy
     * and performs it
     *
     * Conditions:
     * - a bot is the active Participant
     * - an action can be performed
     *
     * Results:
     * - changed game state to a made turn
     */
    fun performEasyTurn() {
        val currentGameState = rootService.game?.currentGameState
        if (currentGameState == null) {throw IllegalStateException("The game state must exist")}

        // if no actions left - fuck it
        if (!currentGameState.participantList[currentGameState.currentParticipantIndex].actionAvailable) {
            return
        }

        thread {
            val easyTurn = generateEasyTurn(currentGameState)

            Thread.sleep((1000 * (rootService.game?.botSpeed ?: 1)).toLong())
            performTurn(currentGameState, easyTurn)
        }
    }

    /**
     * finds a suitable action for active bot in difficulty hard
     * and performs it
     *
     * Conditions:
     * - a bot is the active Participant
     * - an action can be performed
     *
     * Results:
     * - changed game state to a made turn
     */
    fun performHardTurn() {
        val currentGameState = rootService.game?.currentGameState
        if (currentGameState == null) { throw IllegalStateException("The game state must exist") }

        if (!currentGameState.participantList[currentGameState.currentParticipantIndex].actionAvailable) {
            return
        }

        val hardTurn = generateHardTurn(currentGameState)

        if (hardTurn == null) {
            currentGameState.participantList[currentGameState.currentParticipantIndex].actionAvailable = false
            rootService.gameService.nextParticipant()
            return
        }
        thread {
            Thread.sleep((1000 * (rootService.game?.botSpeed ?: 1)).toLong())
            performTurn(currentGameState, hardTurn)
        }
    }

    private fun generateEasyTurn(currentGameState: GameState): BotTurn {
        val currentParticipant = currentGameState.participantList[currentGameState.currentParticipantIndex]
        if (currentParticipant.lastPlacedPiece == null) {
            return generateFirstHardTurn(currentGameState)
        }
        val allPossibleTurns = boardHandlerService.getPossiblePositions(
            botScoreService, currentGameState, scoreMode = true
        )

        return allPossibleTurns.random()
    }

    private fun performTurn(currentGameState: GameState, botTurn: BotTurn) {
        // overriding the piece in the bots piece list
        val currentPiece = botTurn.figure
        val participant = currentGameState.participantList[currentGameState.currentParticipantIndex]
        participant.pieceList.removeIf { it.id == currentPiece.id }
        participant.pieceList.add(currentPiece)

        rootService.playerActionService.place(botTurn.figure, Pair(botTurn.posX, botTurn.posY))
    }

    // starts the build
    private fun generateHardTurn(currentGameState: GameState): BotTurn? {
        val currentParticipant = currentGameState.participantList[currentGameState.currentParticipantIndex]
        if (currentParticipant.lastPlacedPiece == null) {
            return generateFirstHardTurn(currentGameState)
        }
        val root = deepCopyGameState(currentGameState)
        val bestTurn = getOptimalTurn(root)
        return bestTurn
    }

    private fun generateFirstHardTurn(currentGameState: GameState): BotTurn {
        val figureId = BotSettings.BESTOPENFIGURES.random()
        val root = deepCopyGameState(currentGameState)

        val allPossibleFirstMoves = boardHandlerService.getPossibleFirstPositions(
            botScoreService, root, figureId
        )
        return allPossibleFirstMoves.random()
    }

    // root children are handled a bit differently
    private fun getOptimalTurn(parent: GameState): BotTurn? {
        val allPossibleTurns = boardHandlerService.getPossiblePositions(botScoreService, parent)

        if (allPossibleTurns.isEmpty()) return null

        val activePlayerColor = parent.participantList[parent.currentParticipantIndex].color
        val sortedPossibleTurns = allPossibleTurns.sortedByDescending { it.turnScore }
        var bestScore = -1.0
        var bestTurn: BotTurn? = null

        // change of number of rounds
        var maxDepth = 21
        val roundN = 21 - parent.participantList[parent.currentParticipantIndex].pieceList.size
        if (roundN > BotSettings.MAXDEPTHCHANGE) {
            maxDepth = BotSettings.MAXGAMETREEDEPTHEND
        } else {
            maxDepth = BotSettings.MAXGAMETREEDEPTHBEGINNING
        }

        for (i in 0 until minOf(BotSettings.MAXTURNPOSSIBILITIES, sortedPossibleTurns.size)) {
            val newChildGameState = nextGameState(parent, sortedPossibleTurns[i], activePlayerColor)
            var newScore = createGameTreeElement(newChildGameState, 0, maxDepth)
            if (newScore == null) {
                newScore = sortedPossibleTurns[i].turnScore
            }
            if (newScore > bestScore) {
                bestScore = newScore
                bestTurn = sortedPossibleTurns[i]
            }
        }

        return bestTurn ?: sortedPossibleTurns.first()
    }

    // main part of the tree
    private fun createGameTreeElement(parent: GameState, depth: Int, maxDepth: Int): Double? {
        val allPossibleTurns = boardHandlerService.getPossiblePositions(botScoreService, parent)
        val activePlayer = parent.participantList[parent.currentParticipantIndex]
        val activePlayerColor = activePlayer.color
        val sortedPossibleTurns = allPossibleTurns.sortedByDescending { it.turnScore }

        if (depth == maxDepth) {
            if (activePlayer.pieceList.size < BotSettings.SCORECHANGE) {
                return botScoreService.calculateScore(
                    parent.field,
                    listOf(activePlayerColor)).toDouble()
            }
            if (sortedPossibleTurns.isEmpty()) { return null }
            return sortedPossibleTurns[0].turnScore
        }
        var bestScore = -1.0

        for (i in 0 until minOf(BotSettings.MAXCHILDRENNUM, sortedPossibleTurns.size)) {
            val newChildGameState = nextGameState(parent, sortedPossibleTurns[i], activePlayerColor)

            var newScore = createGameTreeElement(newChildGameState, depth + 1, maxDepth)

            // null means that there are no possible turns after sortedPossibleTurns[i]
            // if this is the case we still want for th score to be the best for
            // the original player ((depth + 1) % 4 == 0 is the original player)
            // in the other case null is to be returned
            // and the score shall be calculated by the next -4 depth
            if (newScore == null && (depth + 1) % 4 == 0) {
                newScore = sortedPossibleTurns[i].turnScore
            }
            // if newScore is null and it is now our original player
            // then it is not to be ignored
            if (newScore != null) {
                if (newScore > bestScore) {
                    bestScore = newScore
                }
            }
        }
        // no available turns
        if (bestScore == -1.0) {
            return null
        }

        return bestScore
    }

    private fun alterPieceList(currentGameState: GameState, pieceId: String) {
        val participant = currentGameState.participantList[currentGameState.currentParticipantIndex]
        participant.pieceList.removeIf { it.id == pieceId }
    }

    private fun nextGameState(
        oldState: GameState,
        turn: BotTurn,
        activePlayerColor: Color,
    ): GameState {
        val newField = boardHandlerService.placeFigure(oldState.field, turn, activePlayerColor)
        val newState = deepCopyGameStateWithNewField(oldState, newField)

        alterPieceList(newState, turn.figure.id)
        newState.currentParticipantIndex = (newState.currentParticipantIndex + 1) % newState.participantList.size

        return newState
    }

    private fun deepCopyGameState(state: GameState): GameState {
        return GameState(
            field = state.field.map { it.copyOf() }.toTypedArray(),
            currentParticipantIndex = state.currentParticipantIndex,
            previousGameState = null,
            participantList = state.participantList.map { playerActionService.copyParticipant(it) },
        )
    }

    private fun deepCopyGameStateWithNewField(state: GameState, newField: Array<Array<Color?>>): GameState {
        return GameState(
            field = newField,
            currentParticipantIndex = state.currentParticipantIndex,
            previousGameState = null,
            participantList = state.participantList.map { playerActionService.copyParticipant(it) },
        )
    }
}