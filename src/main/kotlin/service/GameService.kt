package service

import entity.*

/**
 * Service layer class that provides the logic for actions not directly
 * related to a single participant.
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
class GameService(private val rootService: RootService): AbstractRefreshingService() {

    /**
     * [startGame] initializes a new game of Blokus.
     * It will add all participants, while also considering an order + scoring-selection.
     *
     * @param params This parameter represents a data class
     * of the type [StartGameParams] with all information needed to start a game.
     * For further details look into [StartGameParams].
     *
     * Preconditions:
     * -There has to be no game in progress.
     * -There needs to be 2-4 participants
     * -The selection between 14x14 and 20x20 board needs to be done manually in the case for 2 players
     * -The scoring method needs to be chosen.
     *
     * Postconditions:
     * -The game needs to be initialized.
     * -The field needs to be initialized via refreshAfterInitializeGame
     *
     * @returns This method doesn't return anything ('Unit').
     *
     * @throws IllegalStateException if there is a game already in progress.
     * @throws IllegalArgumentException if the [params] config is invalid.
     */
    fun startGame(params: StartGameParams) {
        //error handling
        var game = rootService.game
        check(game==null) {"Game is already running"}

        require(params.participants.size in 2..4) {
            "Game must have between 2 and 4 players"
        }
        require(params.participants.groupingBy { it.second }.eachCount().filter { it.value > 1 }.count() < 1) {
            "no duplicate colors allowed"
        }
        require(params.participants.groupingBy { it.first }.eachCount().filter { it.value > 1 }.count() < 1) {
            "no duplicate names allowed"
        }

        //default setup
        val boardSize : Int = if(params.isBigField){
            20
        } else {
            14
        }
        val board = Array(boardSize) {
            Array<Color?>(boardSize) {null}
        }
        val playerList = mutableListOf<Participant>()

        //add participants to the player list (excluding multi controlled ones, those will be handled later)
        for(data in params.participants) {
            val name = data.first
            val color = data.second
            val typ = data.third

            when (typ) {
                ParticipantType.NETWORK_PARTICIPANT -> {
                    val networkParticipant = NetworkParticipant(name, true, color)
                    playerList.add(networkParticipant)
                }

                ParticipantType.PLAYER -> {
                    val realPlayer = Player(name, true, color)
                    playerList.add(realPlayer)
                }

                ParticipantType.EASY_BOT -> {
                    val easyBot = Bot(name, true, color, null, true)
                    playerList.add(easyBot)
                }

                else -> { // STRONG_BOT
                    val strongBot = Bot(name, true, color, null, false)
                    playerList.add(strongBot)
                }
            }
        }


        //check for 2 player case
        if(playerList.size == 2 && boardSize == 20) {
            //check if chosen colors are legal
            require(setOf(playerList[0].color, playerList[1].color) != setOf(Color.BLUE,Color.RED) &&
                    setOf(playerList[0].color, playerList[1].color) != setOf(Color.YELLOW,Color.GREEN) ) {
                "wrong color choices"
            }

            //find the fitting color for player 1
            val p1Color: Color
            if(playerList[0].color==Color.BLUE) { p1Color = Color.RED }
            else if(playerList[0].color==Color.RED) { p1Color = Color.BLUE }
            else if(playerList[0].color==Color.GREEN) { p1Color = Color.YELLOW }
            else { p1Color = Color.GREEN }

            //find the fitting color for player 2
            val p2Color: Color
            if(playerList[1].color==Color.BLUE) { p2Color = Color.RED }
            else if(playerList[1].color==Color.RED) { p2Color = Color.BLUE }
            else if(playerList[1].color==Color.GREEN) { p2Color = Color.YELLOW }
            else { p2Color = Color.GREEN }

            //local variable for thread safety
            var participant = playerList[0]
            //if it's a player
            if (participant is Player) playerList.add(Player(participant.name,true,p1Color))
            //if it's a Bot
            else if(participant is Bot){
                if(participant.isEasy){
                    playerList.add(Bot(participant.name,true,p1Color,isEasy = true))
                }
                else{
                    playerList.add(Bot(participant.name,true,p1Color,isEasy = false))
                }
            }
            //then it's a network player
            else{
                playerList.add(NetworkParticipant(participant.name, true, p1Color))
            }

            participant = playerList[1]
            //if it's a player
            if (participant is Player) playerList.add(Player(participant.name,true,p2Color))
            //if it's a Bot
            else if(participant is Bot){
                if(participant.isEasy){
                    playerList.add(Bot(participant.name,true,p2Color,isEasy = true))
                }
                else{
                    playerList.add(Bot(participant.name,true,p2Color,isEasy = false))
                }
            }
            //then it's a network player
            else{
                playerList.add(NetworkParticipant(participant.name, true, p2Color))
            }
        }

        //check for 3 player case
        if(params.participants.size==3){
            val colors = mutableListOf(Color.RED, Color.BLUE, Color.GREEN,Color.YELLOW)

            //find out which color is unpicked
            params.participants.forEach { colors.remove(it.second) }

            val mcp = MultiControlledParticipant("OUR Player",
                true,
                colors[0],
                null,
                playerList.toMutableList().sortedBy { it.color.ordinal }.toMutableList()
            )
            playerList.add(mcp)
        }

        //order by colors (if not inside the 4 player case)
        if(params.participants.size!=4) {
            playerList.sortBy { it.color.ordinal }
        }

        val initialGameState = GameState(
            field = board,
            currentParticipantIndex = 0,
            nextGameState = null,
            previousGameState = null,
            participantList = playerList,
        )

        var isNetworkGame = false

        for (participant in params.participants) {
            if(participant.third == ParticipantType.NETWORK_PARTICIPANT) {
                isNetworkGame = true
                break
            }
        }

        val blokusGame = BlokusGame( isBasicScoring = params.isBasicScoring,
            isNetwork = isNetworkGame,
            botSpeed = 1,
            currentGameState = initialGameState,
        )

        rootService.game = blokusGame
        game = blokusGame
        //decrement the currentParticipantIndex so that nextParticipant can start the game
        game.currentGameState.currentParticipantIndex = (game.currentGameState.currentParticipantIndex-1 +
                game.currentGameState.participantList.size).mod(game.currentGameState.participantList.size)

        onAllRefreshables { refreshAfterInitializeGame() }
        rootService.gameService.nextParticipant()
    }

    //private helper function to check if given participant could make a move in his next turn
    private fun possibleMove(participant: Participant): Boolean {
        val game = rootService.game
        checkNotNull(game)

        // no possible move, if actionAvailable was set to false in the past
        if (!participant.actionAvailable) {
            return false
        }

        //>= 19 because 2 moves are (at least) guaranteed to be possible
        if (participant.pieceList.size >= 19) {
            return true
        }

        val fieldSize = game.currentGameState.field.size

        for (item in participant.pieceList) {
            val originalFigure = item.figure.map { it.copyOf() }.toTypedArray()

            repeat(4) {
                for (i in 0 until fieldSize) {
                    for (j in 0 until fieldSize) {
                        if (isLegalMove(item, Pair(i, j))) {
                            // Restore before returning
                            item.figure = originalFigure
                            return true
                        }
                    }
                }
                rootService.playerActionService.rotate(item)
            }

            rootService.playerActionService.flip(item)

            repeat(4) {
                for (i in 0 until fieldSize) {
                    for (j in 0 until fieldSize) {
                        if (isLegalMove(item, Pair(i, j))) {
                            item.figure = originalFigure
                            return true
                        }
                    }
                }
                rootService.playerActionService.rotate(item)
            }

            item.figure = originalFigure
        }

        return false
    }
    /**
     * [nextParticipant] enables the participant switch after a completed action.
     * It can check also, if all the possible actions of the participants have already been executed.
     * In that case it can end the game also.
     *
     * Preconditions:
     * -A game is in progress.
     *
     * Postconditions:
     * -The field needs to be updated via refreshAfterNextParticipant
     *
     * @returns This method doesn't return anything ('Unit').
     *
     * @throws IllegalStateException if there is no game in progress.
     */
    fun nextParticipant() {
        val game = rootService.game
        check(game != null) { "there is no game running yet" }

        val currentState = game.currentGameState
        val players = currentState.participantList
        val amountOfPlayers = players.size

        var turns = 0

        while (turns < amountOfPlayers) {
            currentState.currentParticipantIndex += 1

            if (currentState.currentParticipantIndex >= amountOfPlayers) {
                currentState.currentParticipantIndex = 0
            }
            val nextPlayer = players[currentState.currentParticipantIndex]

            nextPlayer.actionAvailable = possibleMove(nextPlayer)

            if (nextPlayer.actionAvailable) {

                if (game.isNetwork) {
                    val client = rootService.networkService.client
                    if (nextPlayer.name == client?.playerName
                        || (nextPlayer is MultiControlledParticipant
                                && nextPlayer.controlledBy[0].name == client?.playerName))
                        rootService.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
                    else
                        rootService.networkService.updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
                }

                onAllRefreshables { refreshAfterNextParticipant() }

                if (nextPlayer is Bot) {
                    if (nextPlayer.isEasy) {
                        rootService.botTurnService.performEasyTurn()
                    } else {
                        rootService.botTurnService.performHardTurn()
                    }
                } else if (nextPlayer is MultiControlledParticipant) {
                    val nextMCP = nextPlayer.controlledBy[0]
                    if (nextMCP is Bot) {
                        if (nextMCP.isEasy) {
                            rootService.botTurnService.performEasyTurn()
                        } else {
                            rootService.botTurnService.performHardTurn()
                        }
                    }
                }

                return
            }
            turns++
        }

        score()
    }

    /**
     * [score] will analyze the game after the last possible piece has been played. It will calculate a score for each
     * participant.
     *
     * Preconditions:
     * -A game is in progress.
     * -All participants (except the possible MultiControlledParticipant)
     * are having no action available anymore (actionAvailable == false)
     * -In the special case of 3 participants there can be also a button SkipToEnd, which needs to be pressed.
     *
     * Postcondition:
     * -The ranking scene needs to be shown via refreshAfterScore.
     *
     * @returns This method doesn't return anything ('Unit').
     *
     * @throws IllegalStateException if there is no game in progress.
     */
    fun score() {
        //simple error handling
        val game = rootService.game
        checkNotNull(game)

        //list which will collect all the score/participant pairs
        var scores = mutableListOf<Pair<Int,Participant>>()

        //branch between the 2 possible scoring methods
        if (game.isBasicScoring){
            for (item in game.currentGameState.participantList){
                var temp = 0
                for (piece in item.pieceList) {
                    piece.figure.forEach { temp += it.count{ it == 1}}
                }
                if(item !is MultiControlledParticipant) scores.add(Pair(temp,item))
            }
        }
        else{
            for(item in game.currentGameState.participantList){
                var temp = 0
                //goes over all pieces
                for (piece in item.pieceList){
                    //goes over all (inner) IntArrays and counts the 1's (so the number of squares)
                    //then subtracts them on temp so temp is the negative of the sum of all squares
                    //from the current piece
                    piece.figure.forEach { temp -= it.count{ it == 1}}
                }
                //if all pieces are on the board the player gets +15 points
                if (temp == 0) temp = 15

                //temp variable for thread safety
                val lastPiece = item.lastPlacedPiece
                if (lastPiece != null && temp == 15 && lastPiece.id == "O1") {
                    // if the player has no pieces left (temp = 15 points) and
                    // if the last piece was the O1 piece the player gets +5 points
                    temp += 5
                }
                //add the score / participant pair to the ranking
                if(item !is MultiControlledParticipant) scores.add(Pair(temp,item))
            }
        }
        //check for the 2 player 4 color case
        if(game.currentGameState.participantList.map { it.name }.toSet().size <
            game.currentGameState.participantList.size){
            val newScores = mutableListOf<Pair<Int,Participant>>()
            newScores.add(Pair(scores[0].first + scores[2].first,scores[0].second))
            newScores.add(Pair(scores[1].first + scores[3].first,scores[1].second))
            scores = newScores
        }

        //sort based on scoring method
        if(game.isBasicScoring){
            scores.sortBy { it.first }
        }
        else{
            scores.sortByDescending { it.first }
        }
        onAllRefreshables { refreshAfterScore(scores) }
    }

    /**
     * [setBotSpeed] will add a delay, after a bot has performed an action.
     *
     * @param seconds is a positive Integer, which will result in a delay (in seconds)
     *
     * Preconditions:
     * -A game is in progress.
     *
     * Postconditions:
     * -A delay needs to be added (the next participant needs to be chosen after [seconds] seconds, not earlier).
     *
     * @returns This method doesn't return anything ('Unit').
     *
     * @throws IllegalStateException if there is no game in progress.
     * @throws IllegalArgumentException if no Integer has been added
     */
    fun setBotSpeed(seconds: Int) {
        val game = rootService.game

        check(game != null){
            "there is no game running yet"
        }

        require(seconds >= 0){"time must not be negative"}

        game.botSpeed = seconds
    }


    /**
     * [isLegalMove] will check, if a selected (and also in future possibly placed) [piece] is legal
     * at the position [coordinates].
     *
     * @param piece is the previously selected [Piece] of the active participant.
     * @param coordinates is the position, in which the [piece] wants to be placed.
     *
     * Preconditions:
     * -A [piece] needs to be selected.
     * -There needs to be a position, in which the [piece] has to be placed
     *
     * Postconditions:
     * -It will return a true or false, depending on if the position is valid or not.
     *
     * @returns This method returns a Boolean.
     *
     * @throws IllegalStateException if there is no game in progress.
     * @throws IllegalArgumentException if the piece isn't from the active participant
     * or the coordinates are outside the field
     */
    fun isLegalMove(piece: Piece, coordinates: Pair<Int, Int>) : Boolean {
        val game = rootService.game
        checkNotNull(game)

        val state = game.currentGameState
        val currentParticipant = state.participantList[state.currentParticipantIndex]

        // Check that this piece belongs to the current player
        require(currentParticipant.pieceList.contains(piece))

        val field = state.field
        val fieldSize = field.size
        val fig = piece.figure
        val figRows = fig.size
        val figCols = fig[0].size
        val color = currentParticipant.color

        // Check that the piece fits within the field
        if (coordinates.first < 0 || coordinates.first + figRows > fieldSize) return false
        if (coordinates.second < 0 || coordinates.second + figCols > fieldSize) return false

        var touchesCorner = false

        for (i in 0 until figRows) {
            for (j in 0 until figCols) {
                if (fig[i][j] == 1) {
                    val boardRow = coordinates.first + i
                    val boardCol = coordinates.second + j

                    // Cell must be empty
                    if (field[boardRow][boardCol] != null) return false

                    // Must not be orthogonally adjacent to own color
                    // Up
                    if (boardRow > 0 && field[boardRow - 1][boardCol] == color) return false
                    // Down
                    if (boardRow < fieldSize - 1 && field[boardRow + 1][boardCol] == color) return false
                    // Left
                    if (boardCol > 0 && field[boardRow][boardCol - 1] == color) return false
                    // Right
                    if (boardCol < fieldSize - 1 && field[boardRow][boardCol + 1] == color) return false

                    // Check diagonal adjacency to own color (needed for non-first moves)
                    if (boardRow > 0 && boardCol > 0
                        && field[boardRow - 1][boardCol - 1] == color) touchesCorner = true
                    if (boardRow > 0 && boardCol < fieldSize - 1
                        && field[boardRow - 1][boardCol + 1] == color) touchesCorner = true
                    if (boardRow < fieldSize - 1 && boardCol > 0
                        && field[boardRow + 1][boardCol - 1] == color) touchesCorner = true
                    if (boardRow < fieldSize - 1 && boardCol < fieldSize - 1
                        && field[boardRow + 1][boardCol + 1] == color) touchesCorner = true
                }
            }
        }

        // First piece: must cover a board corner
        if (currentParticipant.lastPlacedPiece == null) {
            val boardCorners = setOf(
                Pair(0, 0),
                Pair(0, fieldSize - 1),
                Pair(fieldSize - 1, 0),
                Pair(fieldSize - 1, fieldSize - 1)
            )

            var coversCorner = false
            for (i in 0 until figRows) {
                for (j in 0 until figCols) {
                    if (fig[i][j] == 1) {
                        val boardPos = Pair(coordinates.first + i, coordinates.second + j)
                        if (boardCorners.contains(boardPos)) {
                            coversCorner = true
                        }
                    }
                }
            }
            return coversCorner
        }

        // Non-first piece: must touch at least one diagonal of own color
        return touchesCorner
    }
}