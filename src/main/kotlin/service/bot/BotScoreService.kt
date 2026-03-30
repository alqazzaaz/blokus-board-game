package service.bot
import entity.Color
import entity.GameState

/**
 * is used for scoring of how good a move is inside the game state tree to
 * sort out the best n moves to build the tree from
 */
class BotScoreService {
    private val boardHandlerService = BoardHandlerService()
    private val allFigurePositions = AllFigurePositionings.positionings

    /**
     * the main function for calculating how good the turn is. returns a double by
     * taking in the state, turn, and current player color
     */
    fun getTurnScore(currentGameState: GameState, turn: BotTurn, playerColor: Color): Double {
        val newField = boardHandlerService.placeFigure(currentGameState.field, turn, playerColor)
        // for calculating my possible moves at first leaving the current participant index the same
        val newGameState = GameState(
            newField,
            currentGameState.currentParticipantIndex,
            null,
            null,
            currentGameState.participantList
        )

        // important values
        val numPiecesAvailable = newGameState.participantList[newGameState.currentParticipantIndex].pieceList.size

        // calculating the goodness in relation to me
        val numPossibleLegalMoves = newPossibleLegalMoves(newGameState, newField, turn, playerColor)
        val myCellsPlaced = turn.figure.figure.sumOf { it.sum() }
        val percentFiguresUsage = numPossibleLegalMoves.toDouble() / (numPiecesAvailable * 4)
        val opponentTouchScore = opponentTouchScore(newField, turn, playerColor)

        val score: Double = percentFiguresUsage * BotSettings.POSSIBLEMOVESIMPORTANCE +
                myCellsPlaced * BotSettings.CELLSPLACEDIMPORTANCE +
                opponentTouchScore * BotSettings.OPPONENTTOUCHIMPORTANCE

        return score
    }

    // how much a placed figure touches opponents
    private fun opponentTouchScore(newField: Array<Array<Color?>>, turn: BotTurn, playerColor: Color) : Int {
        val figure = turn.figure.figure
        var score = 0
        for (posX in turn.posX until turn.posX + figure.size) {
            for (posY in turn.posY until turn.posY + figure[0].size) {
                if (newField[posX][posY] == playerColor) {
                    score += touchCount(newField, playerColor, posX, posY)
                }
            }
        }
        return score
    }

    private fun newPossibleLegalMoves(
        currentGameState: GameState,
        newField: Array<Array<Color?>>,
        turn: BotTurn,
        playerColor: Color
    ): Int {
        // step one: copute new corners which were made by the figure
        val newCorners = getNewCorners(newField, playerColor, turn)

        // step two: how many new turns does this move allow us to have?
        val currentPlayer = currentGameState.participantList[currentGameState.currentParticipantIndex]
        val availablePieces = currentPlayer.pieceList

        var numPossiblePos = 0

        for (piece in availablePieces) {

            val seenPositions = mutableSetOf<BotTurnCache>()
            for (rotation in allFigurePositions[piece.id] ?: continue) {
                numPossiblePos += boardHandlerService.getAllPossiblePositionsForPiece(
                    this,
                    rotation,
                newCorners,
                    currentGameState,
                    piece.id,
                    seenPositions,
                    true
                ).size

            }
        }
        return numPossiblePos
    }

    private fun getNewCorners(
        field: Array<Array<Color?>>, playerColor: Color, turn: BotTurn
    ): MutableList<Pair<Int, Int>> {
        val allCorners: MutableList<Pair<Int, Int>> = mutableListOf()

        val startX = turn.posX - 1
        val startY = turn.posY - 1

        val endX = turn.posX + turn.figure.figure.size + 1
        val endY = turn.posY + turn.figure.figure[0].size + 1

        for (x in startX until endX) {
            for (y in startY until endY) {
                if (boardHandlerService.isACorner(field, playerColor, x, y)) {
                    allCorners.add(Pair(x, y))
                }
            }
        }

        return allCorners
    }


    private fun touchCount(board: Array<Array<Color?>>, playerColor: Color, posX: Int, posY: Int): Int {
        val sides = listOf(
            boardHandlerService.acessBoard(board, posX - 1, posY),
            boardHandlerService.acessBoard(board, posX + 1, posY),
            boardHandlerService.acessBoard(board, posX, posY - 1),
            boardHandlerService.acessBoard(board, posX, posY + 1),
            boardHandlerService.acessBoard(board, posX - 1, posY - 1),
            boardHandlerService.acessBoard(board, posX + 1, posY + 1),
            boardHandlerService.acessBoard(board, posX + 1, posY - 1),
            boardHandlerService.acessBoard(board, posX - 1, posY + 1)
        )
        return sides.count {it != playerColor && it != null}
    }

    // thats the color based score
    /**
     * Calculates the end game score based on how many blocks of the board is placed with my color
     */
    fun calculateScore(board: Array<Array<Color?>>, friendlyColors: List<Color>): Int {
        var score = 0
        for (x in 0..board.size - 1) {
            for (y in 0..board.size - 1) {
                score += if (board[x][y] in friendlyColors) 1 else 0
            }
        }
        return score
    }
}