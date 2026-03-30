package service.bot

import entity.Color
import entity.Piece
import entity.GameState

/**
 * is used for any operations regarding the field in the bot service.
 */
class BoardHandlerService {

    private val allFigurePositions = AllFigurePositionings.positionings

    /**
     * computes every possible opening move for a current game state and chosen piece
     * since opening pieces are pre computed for the hard bot.
     * returns a list of all possible Turns
     */
    fun getPossibleFirstPositions(
        botScoreService: BotScoreService,
        currentGameState: GameState,
        pieceId: String
    ): MutableList<BotTurn> {
        //val currentPlayer = currentGameState.participantList[currentGameState.currentParticipantIndex]
        val fieldSize = currentGameState.field.size

        val allPossiblePositionsForPiece: MutableList<BotTurn> = mutableListOf()

        val availableCorners = mutableListOf(
            Pair(0, 0),
            Pair(0, fieldSize - 1),
            Pair(fieldSize - 1, 0),
            Pair(fieldSize - 1, fieldSize - 1)
        )

        val seenPositions = mutableSetOf<BotTurnCache>()
        for (rotation in allFigurePositions[pieceId]?: throw IllegalStateException("no rotations found")) {
            allPossiblePositionsForPiece.addAll(
                getAllPossiblePositionsForPiece(
                    botScoreService,
                    rotation,
                    availableCorners,
                    currentGameState,
                    pieceId,
                    seenPositions,
                    true
                )
            )
        }

        return allPossiblePositionsForPiece
    }

    /**
     * computes all possible moves for a game state.
     * score mode - no score is being computed (is used by score)
     */
    fun getPossiblePositions(
        botScoreService: BotScoreService,
        currentGameState: GameState,
        scoreMode: Boolean = false
    ): MutableList<BotTurn> {
        val currentPlayer = currentGameState.participantList[currentGameState.currentParticipantIndex]
        val availablePieces = currentPlayer.pieceList
        val playerColor = currentPlayer.color

        val allPossiblePositionsForPiece: MutableList<BotTurn> = mutableListOf()
        val availableCorners = getAllAllowedCorners(currentGameState, playerColor)

        for (piece in availablePieces) {
            if ((piece.id == "O1" || piece.id == "X5") &&
                allPossiblePositionsForPiece.size > BotSettings.MAXTURNPOSSIBILITIES) {
                break
            }
            val seenPositions = mutableSetOf<BotTurnCache>()
            for (rotation in allFigurePositions[piece.id] ?: continue) {
                val possiblePos = getAllPossiblePositionsForPiece(
                    botScoreService,
                    rotation,
                    availableCorners,
                    currentGameState,
                    piece.id,
                    seenPositions,
                    scoreMode
                )
                allPossiblePositionsForPiece.addAll(possiblePos)

            }
        }

        return allPossiblePositionsForPiece
    }

    /**
     * calculates all possible moves for a given rotation of a piece.
     * iterates over given free corners to do so.
     * also taken in seen positions to skip already calculated stuff.
     * the score mode is for not calculating the score.
     */
    fun getAllPossiblePositionsForPiece(
        botScoreService: BotScoreService,
        rotation: AllFigurePositionings.Rotation,
        availableCorners: List<Pair<Int, Int>>,
        currentGameState: GameState,
        pieceId: String,
        seenPositions: MutableSet<BotTurnCache>,
        scoreMode: Boolean
    ): MutableList<BotTurn>{

        val allPossiblePositionsForPiece: MutableList<BotTurn> = mutableListOf()
        val piece = Piece(rotation.figure, pieceId)
        val isFirstMove = currentGameState.participantList[
            currentGameState.currentParticipantIndex
        ].lastPlacedPiece == null
        val playerColor = currentGameState.participantList[
            currentGameState.currentParticipantIndex
        ].color


        for (pos in availableCorners) {
            for (pieceCorner in rotation.corners) {
                val x = pos.first - pieceCorner.first
                val y = pos.second - pieceCorner.second

                val botCacheObj = BotTurnCache(x, y, piece.figure)
                if (botCacheObj in seenPositions ||
                    !isLegalMove(
                        currentGameState.field, piece, Pair(x, y), playerColor, isFirstMove
                    )) { continue}
                seenPositions.add(botCacheObj)

                val turn = BotTurn(
                    x,
                    y,
                    piece,
                    0.0
                )
                if (!scoreMode) {
                    turn.turnScore = botScoreService.getTurnScore(currentGameState, turn, playerColor)
                    allPossiblePositionsForPiece.add(turn)
                } else {
                    allPossiblePositionsForPiece.add(turn)
                }
            }
        }
        return allPossiblePositionsForPiece
    }


    // just getting all allowed placing points by iterating over the board
    private fun getAllAllowedCorners(currentGameState: GameState, playerColor: Color): MutableList<Pair<Int, Int>> {
        val boardSize = currentGameState.field.size
        val allCorners: MutableList<Pair<Int, Int>> = mutableListOf()

        for (x in 0 until boardSize) {
            for (y in 0 until boardSize) {
                if (isACorner(currentGameState.field, playerColor, x, y)) {
                    allCorners.add(Pair(x, y))
                }
            }
        }

        return allCorners
    }

    /**
     * checks if a given field element is a corner based onthe board, players color and the osition.
     * returns a boolean
     */
    fun isACorner(board: Array<Array<Color?>>, playerColor: Color, posX: Int, posY: Int): Boolean {
        if (acessBoard(board, posX, posY) != null) {
            return false
        }

        val sides = listOf(
            acessBoard(board, posX - 1, posY),
            acessBoard(board, posX + 1, posY),
            acessBoard(board, posX, posY - 1),
            acessBoard(board, posX, posY + 1)
        )

        val corners = listOf(
            acessBoard(board, posX - 1, posY - 1),
            acessBoard(board, posX + 1, posY + 1),
            acessBoard(board, posX + 1, posY - 1),
            acessBoard(board, posX - 1, posY + 1)
        )

        if (sides.contains(playerColor)) {
            return false
        }

        if (corners.contains(playerColor)) {
            return true
        }
        return false
    }

    /**
     * instead of board x, y and asking every time if x and y is in indicies
     * returns a Color?
     */
    fun acessBoard(board: Array<Array<Color?>>, posX: Int, posY: Int): Color? {
        if (posX !in board.indices) return null
        if (posY !in board.indices) return null
        return board[posX][posY]
    }

    /**
     * places a figure on the field and returns the new field (deep copy is being made in progress)
     */
    fun placeFigure(field: Array<Array<Color?>>, turn: BotTurn, color: Color): Array<Array<Color?>> {
        val figure = turn.figure.figure
        val newField = field.map { it.copyOf() }.toTypedArray()

        for (row in figure.indices) {
            for (col in figure[row].indices) {
                val fieldX = turn.posX + row
                val fieldY = turn.posY + col
                if (fieldX in newField.indices && fieldY in newField[fieldX].indices && figure[row][col] == 1) {
                    newField[fieldX][fieldY] = color
                }
            }
        }
        return newField
    }

    private fun isLegalMove(
        field: Array<Array<Color?>>,
        piece: Piece,
        coordinates: Pair<Int, Int>,
        color: Color,
        isFirstMove: Boolean
    ) : Boolean {
        val fieldSize = field.size
        val fig = piece.figure
        val figRows = fig.size
        val figCols = fig[0].size

        // Check that the piece fits within the field
        if (coordinates.first < 0 || coordinates.first + figRows > fieldSize) return false
        if (coordinates.second < 0 || coordinates.second + figCols > fieldSize) return false

        var touchesCorner = false
        for (i in 0 until figRows) {
            for (j in 0 until figCols) {
                if (fig[i][j] == 0) { continue }

                val boardRow = coordinates.first + i
                val boardCol = coordinates.second + j

                // Cell must be empty
                if (field[boardRow][boardCol] != null) return false

                val sides = listOf(
                    acessBoard(field, boardRow - 1, boardCol),
                    acessBoard(field, boardRow + 1, boardCol),
                    acessBoard(field, boardRow, boardCol - 1),
                    acessBoard(field, boardRow, boardCol + 1)
                )
                if (sides.contains(color)) {
                    return false
                }

                val corners = listOf(
                    acessBoard(field, boardRow - 1, boardCol - 1),
                    acessBoard(field, boardRow + 1, boardCol + 1),
                    acessBoard(field, boardRow + 1, boardCol - 1),
                    acessBoard(field, boardRow - 1, boardCol + 1)
                )
                if (corners.contains(color)) {
                    touchesCorner = true
                }

            }
        }
        // First piece: must cover a board corner
        if (isFirstMove) {
            return isLegalFirstMove(fieldSize, fig, coordinates)
        }

        // Non-first piece: must touch at least one diagonal of own color
        return touchesCorner
    }

    /**
     * checks, if the first move is legal
     */
    fun isLegalFirstMove(fieldSize: Int, fig: Array<IntArray>, coordinates: Pair<Int, Int>): Boolean {
        val boardCorners = setOf(
            Pair(0, 0),
            Pair(0, fieldSize - 1),
            Pair(fieldSize - 1, 0),
            Pair(fieldSize - 1, fieldSize - 1)
        )
        val figRows = fig.size
        val figCols = fig[0].size

        var coversCorner = false
        for (i in 0 until figRows) {
            for (j in 0 until figCols) {
                val boardPos = Pair(coordinates.first + i, coordinates.second + j)

                if (fig[i][j] == 1 && boardCorners.contains(boardPos)) {
                    coversCorner = true
                }
            }
        }
        return coversCorner
    }


}