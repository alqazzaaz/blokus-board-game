package service

import entity.Piece
import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.BlockType
import edu.udo.cs.sopra.ntf.GameMode
import edu.udo.cs.sopra.ntf.InitMessage
import entity.Color
import edu.udo.cs.sopra.ntf.Rotation
import entity.MultiControlledParticipant
import entity.Participant


/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games. Bridges between the [BlokusNetworkClient] and the other services
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 * @param client The [BlokusNetworkClient] to handle massages
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {


    /**Network client. Nullable for offline games. */
    var client: BlokusNetworkClient? = null
        private set

    /** current state of the connection in a network game*/
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /**List of all pieces in the orientation defined in the ntf*/
    val pieces = listOf<Triple<BlockType, String, Array<IntArray>>>(
        Triple(BlockType.I5, "I5", arrayOf(intArrayOf(1,1,1,1,1))),
        Triple(BlockType.N5, "N5", arrayOf(intArrayOf(0,1,1,1),intArrayOf(1,1,0,0))),
        Triple(BlockType.V5, "V5", arrayOf(intArrayOf(1,0,0),intArrayOf(1,0,0),intArrayOf(1,1,1))),
        Triple(BlockType.T5, "T5", arrayOf(intArrayOf(0,1,0),intArrayOf(0,1,0),intArrayOf(1,1,1))),
        Triple(BlockType.U5, "U5", arrayOf(intArrayOf(1,1,1),intArrayOf(1,0,1))),
        Triple(BlockType.L5, "L5", arrayOf(intArrayOf(1,1,1,1),intArrayOf(1,0,0,0))),
        Triple(BlockType.Y5, "Y5", arrayOf(intArrayOf(1,1,1,1),intArrayOf(0,1,0,0))),
        Triple(BlockType.Z5, "Z5", arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1),intArrayOf(0,0,1))),
        Triple(BlockType.W5, "W5", arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,0),intArrayOf(0,1,1))),
        Triple(BlockType.P5, "P5", arrayOf(intArrayOf(1,1),intArrayOf(1,1),intArrayOf(1,0))),
        Triple(BlockType.X5, "X5", arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(0,1,0))),
        Triple(BlockType.F5, "F5", arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(1,0,0))),
        Triple(BlockType.Z4, "Z4", arrayOf(intArrayOf(1,0),intArrayOf(1,1),intArrayOf(0,1))),
        Triple(BlockType.I4, "I4", arrayOf(intArrayOf(1,1,1,1))),
        Triple(BlockType.L4, "L4", arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1))),
        Triple(BlockType.O4, "O4", arrayOf(intArrayOf(1,1),intArrayOf(1,1))),
        Triple(BlockType.T4, "T4", arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1))),
        Triple(BlockType.I3, "I3", arrayOf(intArrayOf(1,1,1))),
        Triple(BlockType.V3, "V3", arrayOf(intArrayOf(1,1),intArrayOf(0,1))),
        Triple(BlockType.I2, "I2", arrayOf(intArrayOf(1,1))),
        Triple(BlockType.O1, "O1", arrayOf(intArrayOf(1)))
    )

    /**
     * Connects to server and creates a new game session
     * @param secret Server secret
     * @param name Player name
     * @param sessionID Identifier of the hosted session (for guests to join)
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(secret: String, name: String, sessionID: String?) {
        //Try to connect to server
        if (!connect(secret, name, ParticipantType.PLAYER )) error("Connection failed")
        updateConnectionState(ConnectionState.CONNECTED)

        //Create new game with session ID and updates the connection state
        //If sessionID is null it will be created by the server and returned by a CreateGameMessage
        if (sessionID.isNullOrBlank()) client?.createGame(GAME_ID, "Welcome, have fun!")
        else client?.createGame(GAME_ID, sessionID, "Welcome, have fun!")

        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to server and joins a game session as guest player
     * @param secret Server secret
     * @param name Player name
     * @param playerType The type of the player
     * @param sessionID Identifier of the joined session (as defined by jost on create)
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     * @throws IllegalArgumentException if playerType is not Bot or player
     */
    fun joinGame(secret: String, name: String, playerType: ParticipantType, sessionID: String){
        //Try to connect and update the connection state
        if (!connect(secret, name, playerType)) error("Connection failed")
        updateConnectionState(ConnectionState.CONNECTED)

        //check for playerType
        require(playerType in listOf(ParticipantType.PLAYER, ParticipantType.EASY_BOT, ParticipantType.STRONG_BOT))

        //Join game and update connection state
        client?.joinGame(sessionID,"Hello, let's have some fun ;)")

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * Connects to server, sets the [client] if successful and
     * returns `true` on success
     * @param secret Network secret. Must not be blank
     * @param name Player name. Must not be blank
     * @param playerType the type of the player
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game     *
     */
    private fun connect(secret: String, name: String, playerType: ParticipantType) : Boolean {
        //Check if already connected
        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        {"already connected to another game"}

        //Check params
        require(secret.isNotBlank()){"Server secret must be given"}
        require(name.isNotBlank()){"Player name must be given"}

        //check for playerType
        require(playerType in listOf(ParticipantType.PLAYER, ParticipantType.EASY_BOT, ParticipantType.STRONG_BOT))

        //create a new instance of Blokus network client with given params
        val newClient =
            BlokusNetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = SECRET,
                networkService = this
            )
        newClient.playerType = playerType

        //connect client to server and check if it was successful. If successful sets the
        //new client as the client for the NetworkService and returns true
        //connection state will be updated by the caller
        return if (newClient.connect()){
            this.client = newClient
            true
        } else false
    }

    /**
     * Disconnect the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active
     */
    fun disconnect(){
        //Leaves a potential game and disconnects from the server
        client?.apply {
            if (sessionID != null) leaveGame("That was fun, goodbye!")
            if (isOpen) disconnect()
        }

        //remove client and update connection state
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Set up the game using [GameService.startGame] and send the game init message
     * the guests players. [connectionState] needs to be [ConnectionState.PLAYING_MY_TURN]
     * or [ConnectionState.WAIT_FOR_MY_TURN] depending on the playing order.
     */
    fun startNewHostedGame(params: StartGameParams){
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)

        rootService.gameService.startGame(params)
        val game = rootService.game
        checkNotNull(game)

        client?.playerType = params.participants.first{ it.first == client?.playerName }.third

        var gameMode = when (params.participants.size){
            4 -> GameMode.FOUR_PLAYER
            3 -> GameMode.THREE_PLAYER
            2 -> GameMode.TWO_PLAYER
            else -> error("Invalid number of players")
        }

        if (!params.isBigField) {
            gameMode = GameMode.TWO_PLAYER_SMALL
        }

        // Sende die ORIGINALEN params, nicht die expandierte Liste
        var players = params.participants.map { it.first to colourToOpponent(it.second) }

        fun color2p4c(color: Color): edu.udo.cs.sopra.ntf.Color = when (color) {
            Color.BLUE, Color.RED -> edu.udo.cs.sopra.ntf.Color.BLUE
            Color.GREEN, Color.YELLOW -> edu.udo.cs.sopra.ntf.Color.YELLOW
        }

        if (gameMode == GameMode.TWO_PLAYER) {
            players = params.participants.map { it.first to color2p4c(it.second) }
        }


        val message = InitMessage(
            if(params.participants.size==4)players else players.sortedBy { it.second },
            gameMode = gameMode,
            isAdvancedScoring = !params.isBasicScoring
        )

        val participant = game.currentGameState.participantList[game.currentGameState.currentParticipantIndex]
        val controlsMCP = participant is MultiControlledParticipant
                && participant.controlledBy[0].name == client?.playerName
        if (currentPlayerName() == client?.playerName || controlsMCP)
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        else updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)

        client?.sendGameActionMessage(message)
        onAllRefreshables { refreshAfterNextParticipant() }
    }

    /**
     * Initializes the entity structure with the data given by the [InitMessage] sent by the host.
     * [connectionState] needs to be [ConnectionState.WAITING_FOR_INIT].
     * This method should be called from the [BlokusNetworkClient] when the host sends the init message.
     * See [BlokusNetworkClient.onInitReceived]
     * @throws IllegalStateException if not currently waiting for an init message
     */
    fun startNewJoinedGame(message: InitMessage, playerName: String){
        check(connectionState == ConnectionState.WAITING_FOR_INIT)

        val playerType = client?.playerType
        checkNotNull(playerType)

        val participants = message.players.map { (string, color) ->
            if (string == client?.playerName) Triple(string, colourFromOpponent(color), playerType)
            else Triple(string, colourFromOpponent(color), ParticipantType.NETWORK_PARTICIPANT)
        }

        var isBigField = true
        if (message.gameMode == GameMode.TWO_PLAYER_SMALL) isBigField = false

        val isBasicScoring = !message.isAdvancedScoring

        val params = StartGameParams(participants, isBigField, isBasicScoring)
        rootService.gameService.startGame(params)

        val game = rootService.game
        checkNotNull(game) {"Game should not be null right after starting it"}

        //Update connection state depending on the active player
        val participant = game.currentGameState.participantList[game.currentGameState.currentParticipantIndex]
        val controlsMCP = participant is MultiControlledParticipant
                && participant.controlledBy[0].name == client?.playerName
        if (currentPlayerName() == client?.playerName || controlsMCP)
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        else updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)

        onAllRefreshables { refreshAfterNextParticipant() }
    }

    /**
     *Send a [ActionMessage] to the opponents
     * @param piece the placed piece represented by a [Piece]
     * @param coordinates The position where the piece was place (x-position, y-position)
     * related to the left top corner
     * @throws IllegalStateException if it is not currently my turn
     */
    fun sendPlayedPieceToOpponents(piece: Piece, coordinates: Pair<Int, Int>){
        check(connectionState == ConnectionState.PLAYING_MY_TURN)

        val game = rootService.game
        checkNotNull(game)

        val pieceToOpponent = pieceToOpponent(piece)

        val message = ActionMessage(
            isMirrored = pieceToOpponent.second,
            coords = coordinates.second to coordinates.first,
            rotation = pieceToOpponent.third,
            blockType = pieceToOpponent.first
        )

        client?.sendGameActionMessage(message)
    }

    /**
     *Plays the opponent's turn by handling the [ActionMessage] sent through the server
     * @param message The message to handle
     * @throws IllegalStateException if currently not expecting an opponent's turn
     */
    fun receivePlayedPieceFromOpponents(message: ActionMessage){
        check(connectionState == ConnectionState.WAIT_FOR_MY_TURN)
        {"Currently not expecting a played piece from opponent"}


        val game = rootService.game
        checkNotNull(game)

        val piece = pieceFromOpponent(message.blockType, message.isMirrored, message.rotation)

        currentPlayer().pieceList.removeIf { it.id == piece.id }
        currentPlayer().pieceList.add(piece)

        val coords = message.coords

        rootService.playerActionService.place(piece, coords.second to coords.first)

        val currentParticipant = game.currentGameState.participantList[game.currentGameState.currentParticipantIndex]
        val controlsMCP = currentParticipant is MultiControlledParticipant
                && currentParticipant.controlledBy[0].name == client?.playerName
        if (currentPlayerName() == client?.playerName || controlsMCP) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }

        onAllRefreshables { refreshAfterNextParticipant() }
    }

    /**
     * Updates the [connectionState] to [newState]
     */
    fun updateConnectionState(newState: ConnectionState){
        this.connectionState = newState
    }

    /**Method to update the GUI after a player joined*/
    fun participantJoined(name: String){
        onAllRefreshables { refreshAfterParticipantJoined(name) }
    }

    /**Method to update the GUI after a player left*/
    fun participantLeft(name: String){
        onAllRefreshables { refreshAfterParticipantLeft(name) }
    }

    /**returns the name of the current player*/
    private fun currentPlayerName(): String{
        val game = rootService.game
        checkNotNull(game)
        return game.currentGameState.participantList[game.currentGameState.currentParticipantIndex].name
    }

    /**returns the current player*/
    private fun currentPlayer(): Participant{
        val game = rootService.game
        checkNotNull(game)
        return game.currentGameState.participantList[game.currentGameState.currentParticipantIndex]
    }


    /**Translate colour enum to colour enum from ntf*/
    private fun colourToOpponent(colour: Color): edu.udo.cs.sopra.ntf.Color =
        when (colour){
            Color.BLUE -> edu.udo.cs.sopra.ntf.Color.BLUE
            Color.YELLOW -> edu.udo.cs.sopra.ntf.Color.YELLOW
            Color.RED -> edu.udo.cs.sopra.ntf.Color.RED
            Color.GREEN -> edu.udo.cs.sopra.ntf.Color.GREEN
        }

    /**Translate colour from ntf to blokus colour*/
    private fun colourFromOpponent(colour: edu.udo.cs.sopra.ntf.Color): Color =
        when (colour) {
            edu.udo.cs.sopra.ntf.Color.BLUE -> Color.BLUE
            edu.udo.cs.sopra.ntf.Color.YELLOW -> Color.YELLOW
            edu.udo.cs.sopra.ntf.Color.RED -> Color.RED
            edu.udo.cs.sopra.ntf.Color.GREEN -> Color.GREEN
        }

    /**Translates from local piece to network piece*/
    fun pieceToOpponent(piece: Piece): Triple<BlockType, Boolean, Rotation>{
        //filter the pieces to get the enum name of the played piece
        val pieceName = pieces.first { it.second == piece.id }.first

        //Resolves the actual orientation
        val conformation = conformation(pieceName, piece)

        return Triple(pieceName, conformation.first, conformation.second)
    }

    /**Translates from network piece to local piece*/
    private fun pieceFromOpponent(blockType: BlockType, isMirrored: Boolean, rotation: Rotation ): Piece{
        //Get the placed piece and its id in the standard conformation from the look-up table
        val piece = Piece(
            pieces.first { it.first == blockType }.third.copyOf(),
            pieces.first { it.first == blockType }.second )

        //Bring the piece into the played conformer
        if (isMirrored) rootService.playerActionService.flip(piece)
        when (rotation) {
            Rotation.TWOHUNDREDANDSEVENTY -> repeat(3) { rootService.playerActionService.rotate(piece) }
            Rotation.ONEHUNDREDANDEIGHTY -> repeat(2) { rootService.playerActionService.rotate(piece) }
            Rotation.NINETY -> rootService.playerActionService.rotate(piece)
            Rotation.NONE -> {}
        }
        return piece
    }

    /**Resolves the orientation of a local piece in relation to the pieces as defined in the ntf*/
    private fun conformation(pieceName: BlockType, piece: Piece): Pair<Boolean, Rotation> {
        val pieceNtf = Piece(pieces.first { it.first == pieceName }.third.copyOf(),
            pieces.first { it.first == pieceName }.second)

        repeat(4) { index ->
            if (piece.figure.contentDeepEquals(pieceNtf.figure)) return Pair(false, rot(index))
            rootService.playerActionService.rotate(pieceNtf)
        }

        rootService.playerActionService.flip(pieceNtf)
        repeat(4) { index ->
            if (piece.figure.contentDeepEquals(pieceNtf.figure)) return Pair(true, rot(index))
            rootService.playerActionService.rotate(pieceNtf)
        }

        error("wrong piece")
    }

    /**Translate number of rotations to rotation enum of ntf*/
    private fun rot(numberRotation: Int): Rotation =
        when (numberRotation){
            0 -> Rotation.NONE
            1 -> Rotation.NINETY
            2 -> Rotation.ONEHUNDREDANDEIGHTY
            3 -> Rotation.TWOHUNDREDANDSEVENTY
            else -> error("too many rotations")
        }

    /** Server details for SoPra*/
    companion object{
        /**URL of the BGW net server hosted for SoPra participants*/
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Secret to the server*/
        const val SECRET = "blocksAgain"

        /** GameID of Blokus*/
        const val GAME_ID = "Blokus"
    }
}