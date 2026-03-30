package service

import edu.udo.cs.sopra.ntf.ActionMessage
import entity.*

/**
 * Service responsible for handling all actions a player can perform during their turn.
 * This includes manipulating pieces before placement by rotating or flipping,
 * placing pieces on the board, and navigating through the game history via undo/redo functionality.
 *
 * @property rootService Reference to the [RootService] to access the current game state
 * and all other services.
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Rotates the given piece 90 degrees clockwise. The rotation only affects
     * the piece's figure representation and does not place or modify anything on the board.
     *
     * @param piece The [Piece] to be rotated.
     */
    fun rotate(piece: Piece) {

        // change x- & y-axis sizes
        val rowLength = piece.figure.size
        val columnLength = piece.figure[0].size

        // initialize an empty bounding box of the rotated piece
        val rotatedPiece = Array(columnLength) { IntArray(rowLength) }

        // go through the old piece to set up the values on the correct spots of the new rotated piece
        for (i in 0 until rowLength) {
            for (j in 0 until columnLength) {
                rotatedPiece[j][rowLength - 1 - i] = piece.figure[i][j]
            }
        }

        // change the original piece to be rotated
        piece.figure = rotatedPiece

        // force a refresh
        onAllRefreshables { refreshAfterRotate() }
    }

    /**
     * Flips the given piece along its vertical axis, creating a mirror effect.
     * The flip only affects the piece's figure representation and does not
     * place or modify anything on the board.
     *
     * @param piece The [Piece] to be flipped.
     */
    fun flip(piece: Piece) {
        piece.figure = piece.figure.map { it.reversedArray() }.toTypedArray()
        onAllRefreshables { refreshAfterFlip() }
    }

    /**
     * Returns a deep copy of a participant
     * @param p: the participant to be copied
     * returns the exact copy of the given participant
     */
    fun copyParticipant(p: Participant): Participant {
        // Deep-copy each piece
        val copiedPieces = p.pieceList.map { piece ->
            Piece(
                figure = piece.figure.map { it.copyOf() }.toTypedArray(),
                id = piece.id
            )
        }.toMutableList()

        // Copy lastPlacedPiece separately
        val copiedLastPiece = p.lastPlacedPiece?.let { lp ->
            Piece(figure = lp.figure.map { it.copyOf() }.toTypedArray(), id = lp.id)
        }

        val copy = when (p) {
            is Player -> Player(p.name, p.actionAvailable, p.color, copiedLastPiece)
            is Bot -> Bot(p.name, p.actionAvailable, p.color, copiedLastPiece, p.isEasy)
            is NetworkParticipant -> NetworkParticipant(p.name, p.actionAvailable, p.color, copiedLastPiece)
            is MultiControlledParticipant -> MultiControlledParticipant(
                p.name,
                p.actionAvailable,
                p.color,
                copiedLastPiece,
                p.controlledBy.toMutableList()
            )

            else -> throw IllegalStateException("Unknown participant type")
        }

        // Replace the pieceList with our deep copy
        copy.pieceList.clear()
        copy.pieceList.addAll(copiedPieces)

        return copy
    }

    private fun snapshotState(currentState: GameState): GameState {
        return GameState(
            field = currentState.field.map { it.copyOf() }.toTypedArray(),
            currentParticipantIndex = currentState.currentParticipantIndex,
            previousGameState = currentState.previousGameState,
            participantList = currentState.participantList.map { copyParticipant(it) },
        )
    }

    /**
     * Places the given piece on the board at the specified coordinates according
     * to the Blokus rules. Each new piece must touch at least one corner of an
     * existing piece of the same color but must never touch one along a side.
     * The first piece of each participant must cover a corner of the board.
     *
     * @param piece The [Piece] to be placed on the board.
     * @param coordinates A [Pair] of row and column indicating the position
     * on the board where the piece should be placed.
     */
    fun place(piece: Piece, coordinates: Pair<Int, Int>) {
        val game = rootService.game
        checkNotNull(game)
        val gameState = game.currentGameState
        val participant = gameState.participantList[gameState.currentParticipantIndex]

        require(rootService.gameService.isLegalMove(piece, coordinates))

        if (participant is MultiControlledParticipant) {
            val temp = participant.controlledBy[0]
            participant.controlledBy[0] = participant.controlledBy[1]
            participant.controlledBy[1] = participant.controlledBy[2]
            participant.controlledBy[2] = temp
        }

        val shouldSendToNetwork = game.isNetwork
                && rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN
        val networkPieceData = if (shouldSendToNetwork) {
            rootService.networkService.pieceToOpponent(piece)
        } else null

        val snapshot = snapshotState(gameState)

        for (row in piece.figure.indices) {
            for (col in piece.figure[row].indices) {
                if (piece.figure[row][col] == 1) {
                    gameState.field[coordinates.first + row][coordinates.second + col] = participant.color
                }
            }
        }

        participant.pieceList.remove(piece)
        participant.lastPlacedPiece = piece

        snapshot.nextGameState = gameState
        gameState.previousGameState = snapshot
        gameState.nextGameState = null

        if (shouldSendToNetwork && networkPieceData != null) {
            val message = ActionMessage(
                isMirrored = networkPieceData.second,
                coords = coordinates.second to coordinates.first,
                rotation = networkPieceData.third,
                blockType = networkPieceData.first
            )
            rootService.networkService.client?.sendGameActionMessage(message)
            rootService.networkService.updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }

        onAllRefreshables { refreshAfterPlace() }
        rootService.gameService.nextParticipant()

        if (shouldSendToNetwork) {
            onAllRefreshables { refreshAfterNextParticipant() }
        }
    }

    /**
     * Undoes the last human player's move and all bot moves that followed it.
     * The game history is traversed backwards until the previous human player's
     * turn is reached, skipping all bot moves in between automatically.
     * This function is disabled for network games and bot-only games.
     */
    fun undo() {
        val game = rootService.game
        checkNotNull(game) { "No game currently running" }
        check(!game.isNetwork) { "Undo not possible in network games" }

        val allBots = game.currentGameState.participantList.none { it is Player }
        check(!allBots) { "Undo not possible in bot-only games" }

        val previousCurrent = game.currentGameState

        var state = game.currentGameState.previousGameState
        checkNotNull(state) { "No previous state available" }

        // Skip backward over bot moves until human turn is reached
        while (state != null) {
            val participant = state.participantList[state.currentParticipantIndex]

            val check = (participant is Player || ((participant is MultiControlledParticipant)
                    && participant.controlledBy[0] is Player))
                    && participant.actionAvailable

            if (check) break
            state = state.previousGameState
        }

        checkNotNull(state) { "No human turn found to undo" }
        state.nextGameState = previousCurrent
        game.currentGameState = state

        onAllRefreshables { refreshAfterUndo() }
    }

    /**
     * Redoes the last undone move and all bot moves that followed it.
     * The game history is traversed forwards until the next human player's
     * turn is reached, skipping all bot moves in between automatically.
     * This function is disabled for network games and bot-only games.
     */
    fun redo() {
        val game = rootService.game
        checkNotNull(game) { "No game currently running" }
        check(!game.isNetwork) { "Redo not possible in network games" }

        val allBots = game.currentGameState.participantList.none { it is Player }
        check(!allBots) { "Redo not possible in bot-only games" }

        var state = game.currentGameState.nextGameState
        checkNotNull(state) { "No next state available" }

        // Skip forward over bot moves until human turn is reached
        while (state != null) {
            val participant = state.participantList[state.currentParticipantIndex]

            val check = (participant is Player || ((participant is MultiControlledParticipant)
                    && participant.controlledBy[0] is Player))
                    && participant.actionAvailable
            if (check)
                break

            state = state.nextGameState
        }

        checkNotNull(state) { "No human turn found to redo" }
        game.currentGameState = state

        onAllRefreshables { refreshAfterRedo() }
    }
}