package gui

import entity.Color
import service.ParticipantType
import tools.aqua.bgw.core.BoardGameApplication
import service.RootService
import service.StartGameParams

/**
 * Represents the main application for the SoPra board game.
 * The application initializes the [RootService] and displays the scenes.
 */
class BlokusApplication : BoardGameApplication("SoPra Game") {

    /**
     * The root service instance. This is used to call service methods and access the entity layer.
     */
    val rootService: RootService = RootService()

    /**
     * The main game scene displayed in the application.
     */
    private val blokusGameScene = BlokusGameScene(rootService)

    /**
     * The new game menu scene displayed in the application.
     */
    private val newGameMenuScene = NewGameMenuScene(rootService, this)

    /**
     * The host new game menu scene displayed in the application.
     */
    private val hostNewGameMenuScene = HostNewGameMenuScene(rootService, this)

    /**
     * The lobby menu scene displayed in the application.
     */
    private val lobbyMenuScene = LobbyMenuScene(rootService)

    /**
     * The pre menu scene displayed in the application.
     */
    private val preMenuScene = PreMenuScene(rootService, this)

    /**
     * The score menu scene displayed in the application.
     */
    private val scoreMenuScene = ScoreMenuScene(rootService, this)

    /**
     * Initializes the application by displaying the [HelloScene].
     */
    init {
        this.showGameScene(blokusGameScene)
        this.showMenuScene(preMenuScene)

        rootService.gameService.addRefreshable(blokusGameScene)
        rootService.playerActionService.addRefreshable(blokusGameScene)
        rootService.networkService.addRefreshable(blokusGameScene)
        rootService.ioService.addRefreshable(blokusGameScene)
        rootService.gameService.addRefreshable(newGameMenuScene)
        rootService.playerActionService.addRefreshable(newGameMenuScene)
        rootService.networkService.addRefreshable(newGameMenuScene)
        rootService.ioService.addRefreshable(newGameMenuScene)
        rootService.gameService.addRefreshable(hostNewGameMenuScene)
        rootService.playerActionService.addRefreshable(hostNewGameMenuScene)
        rootService.networkService.addRefreshable(hostNewGameMenuScene)
        rootService.ioService.addRefreshable(hostNewGameMenuScene)
        rootService.gameService.addRefreshable(lobbyMenuScene)
        rootService.playerActionService.addRefreshable(lobbyMenuScene)
        rootService.networkService.addRefreshable(lobbyMenuScene)
        rootService.ioService.addRefreshable(lobbyMenuScene)
        rootService.gameService.addRefreshable(preMenuScene)
        rootService.playerActionService.addRefreshable(preMenuScene)
        rootService.networkService.addRefreshable(preMenuScene)
        rootService.ioService.addRefreshable(preMenuScene)
        rootService.gameService.addRefreshable(scoreMenuScene)
        rootService.playerActionService.addRefreshable(scoreMenuScene)
        rootService.networkService.addRefreshable(scoreMenuScene)
        rootService.ioService.addRefreshable(scoreMenuScene)
    }

    /**
     * Shows the [newGameMenuScene].
     */
    fun showNewGameMenuScene() {
        showMenuScene(newGameMenuScene)
    }

    /**
     * Shows the [hostNewGameMenuScene] and insert the host name.
     */
    fun showHostNewGameMenuScene() {
        showMenuScene(hostNewGameMenuScene.apply {
            updateHostName(2000)
            updateSessionID(2000)
        })
    }

    /**
     * Shows the [lobbyMenuScene].
     */
    fun showLobbyMenuScene() {
        showMenuScene(lobbyMenuScene.apply {
            updateJoinedParticipants(2000)
        })
    }

    /**
     * Shows the [scoreMenuScene].
     */
    fun showScoreMenuScene() {
        showMenuScene(scoreMenuScene)
    }

    /**
     * Closes the [scoreMenuScene] and refreshes [blokusGameScene] one more time.
     */
    fun closeScoreMenuScene() {
        hideMenuScene()
        blokusGameScene.refreshAfterNextParticipant()
    }
}

