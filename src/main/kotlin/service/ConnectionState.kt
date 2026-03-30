package service

import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification

/**
 * Enum to distinguish the different states that occur in networked games,
 * in particular during connection and game setup. Used in [NetworkService].
 */
enum class ConnectionState {
    /**
     * no connection active. Initial state at the start of the program and after
     * an active connection was closed.
     */
    DISCONNECTED,

    /**
     * connected to server, but no game started
     */
    CONNECTED,

    /**
     * hostGame request sent to server. waiting for confirmation [CreateGameResponse]
     */
    WAITING_FOR_HOST_CONFIRMATION,

    /**
     * join request sent to server. Waiting for confirmation [JoinGameResponse]
     */
    WAITING_FOR_JOIN_CONFIRMATION,

    /**
     Host is waiting for players to join [PlayerJoinedNotification]
     */
    WAITING_FOR_GUESTS,

    /**
     * Guest is waiting for host to start the game and send [InitMessage]
     */
    WAITING_FOR_INIT,

    /**
     * Game is running and it is my turn
     */
    PLAYING_MY_TURN,

    /**
     * Waiting for opponent(s) to finish their turn. Expecting [ActionMessage]
     */
    WAIT_FOR_MY_TURN,
}