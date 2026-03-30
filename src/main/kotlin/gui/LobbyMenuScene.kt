package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Class which models the lobby menu scene in which the joined player sees all the joined participants.
 *
 * @property rootService reference to the service layer
 */
class LobbyMenuScene(private val rootService: RootService) : Refreshable, MenuScene(
    background = ColorVisual(r = 30, g = 30, b = 30)
) {

    /**
     * The title label.
     */
    private val titleLabel = Label(
        posX = 0,
        posY = 30,
        width = 1920,
        height = 200,
        text = "Waiting for host...",
        font = Font(
            color = Color.WHITE,
            size = 50,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * The container for the joined participants.
     */
    private val joinedParticipantsGridPane = GridPane<Label>(
        posX = 960,
        posY = 540,
        rows = 4,
        columns = 1,
        spacing = 20,
    )

    /**
     * The labels for the joined participant names.
     */
    private val joinedParticipantsLabels = Array(4) {
        Label(
            width = 1920,
            height = 40,
            text = "",
            font = Font(
                color = Color.WHITE,
                size = 30,
                fontWeight = Font.FontWeight.BOLD
            )
        )
    }

    /**
     * The list of all other participants without the current player.
     */
    private val otherParticipants: List<String>?
        get() {
            return rootService.networkService.client?.otherPlayerName
        }

    /**
     * The name of the current player.
     */
    private val currentPlayerName: String?
        get() {
            return rootService.networkService.client?.playerName
        }

    init {
        for(i in joinedParticipantsLabels.indices) {
            joinedParticipantsGridPane[0, i] = joinedParticipantsLabels[i]
        }

        addComponents(titleLabel, joinedParticipantsGridPane)
    }

    override fun refreshAfterParticipantJoined(name: String) {
        updateJoinedParticipants()
    }

    override fun refreshAfterParticipantLeft(name: String) {
        updateJoinedParticipants()
    }

    /**
     * Updates the [joinedParticipantsGridPane] by displaying [currentPlayerName] at the top
     * and the rest of [otherParticipants].
     *
     * @param delay the delay for the update (in milliseconds)
     */
    fun updateJoinedParticipants(delay: Int = 0) {
        playAnimation(DelayAnimation(delay).apply {
            onFinished = {
                joinedParticipantsGridPane[0, 0]?.text = currentPlayerName ?: ""

                otherParticipants?.forEachIndexed { index, name ->
                    joinedParticipantsGridPane[0, index+1]?.text = name
                }
            }
        })
    }
}