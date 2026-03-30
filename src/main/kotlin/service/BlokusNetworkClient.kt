package service

import tools.aqua.bgw.net.common.response.*
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.InitMessage
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification

/**
 * [BoardGameClient] implementation for network communication
 *
 * @param playerName the name of the player using this client
 * @param host the host to connect to
 * @param secret the secret to use for the connection
 * @property networkService the [NetworkService] to potentially forward received messages to
 */
class BlokusNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /**Information wether player is player or bot*/
    var playerType: ParticipantType = ParticipantType.PLAYER

    /**The identifier of this game session; can be null if no session started yet*/
    var sessionID: String? = null

    /**Names of the guests*/
    val otherPlayerName = mutableListOf<String>()

    /**
     * Handle a [CreateGameResponse] sent by the server. Will wait for other guests players when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not implemented,
     * the method disconnects from the server and throws an
     * @throws IllegalStateException if status != success or currently not waiting for a join game
     * response
     */
    override fun onCreateGameResponse(response : CreateGameResponse){
        //Check connection state
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        {"Unexpected CreateGameResponse"}

        when (response.status){
            CreateGameResponseStatus.SUCCESS -> {
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
                sessionID = response.sessionID
        }
            else ->disconnectAndError(response.status)
        }
    }

    /**
     * Handles a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in Blokus, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise
     * @throws IllegalStateException if status is !=success or currently not waiting for a join game
     * response
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        //check connection status
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        {"Unexpected JoinGameResponse"}

        when (response.status){
            JoinGameResponseStatus.SUCCESS -> {
                //Add opponents names
                response.opponents.forEach { otherPlayerName.add(it) }
                //Set sessionID
                sessionID = response.sessionID
                //Update connection state
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handles a [PlayerJoinedNotification] sent by the server. Will wait for other
     * players until hosts starts the game
     * @throws IllegalStateException if not currently expecting other guests to join
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification){
        check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS
                || networkService.connectionState == ConnectionState.WAITING_FOR_INIT)
        {"Not awaiting any guests"}

        //Add joined player to the opponents
        otherPlayerName.add(notification.sender)

        //Tell the Gui to update
        networkService.participantJoined(notification.sender)
    }

    /**
     * Handles a [PlayerLeftNotification] sent by the server and remove their name from the list.
     */
    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        otherPlayerName.remove(notification.sender)
        networkService.participantLeft(notification.sender)
    }

    /**
     * Handles a [GameActionResponse] sent by the server. Does nothing when its
     * status is [GameActionResponseStatus.SUCCESS]. As recovery from network problems
     * is not implemented in Blokus, the method disconnects from the
     * server and throws an [IllegalStateException] otherwise
     * @throws IllegalStateException if status != success
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        check(networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
        networkService.connectionState == ConnectionState.WAIT_FOR_MY_TURN)
        {"Not currently plying in a network game"}

        when (response.status){
            //in case of success do nothing
            GameActionResponseStatus.SUCCESS -> {}
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handles an [InitMessage] sent by the server. Will start a new joined game as guest
     * @throws IllegalStateException if currently not expecting an InitMessage
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message : InitMessage, sender: String){
        networkService.startNewJoinedGame(
            message = message,
            playerName = playerName
        )
    }

    /**
     * Handles an [ActionMessage] sent by the server. Will update the game stat
     * @throws IllegalStateException if currently not expecting an action message
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onActionReceived(message: ActionMessage, sender: String){
        networkService.receivePlayedPieceFromOpponents(message)
    }

    /**disconnects and sets an error*/
    private fun disconnectAndError(message: Any){
        networkService.disconnect()
        error(message)
    }
}