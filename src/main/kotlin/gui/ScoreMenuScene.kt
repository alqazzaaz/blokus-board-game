package gui

import entity.Participant
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.event.MouseButtonType
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Class which represents the score menu scene in which all scores of the
 * participants are displayed.
 *
 * This scene updates if [gui.ScoreMenuScene.refreshAfterScore] is invoked.
 *
 * @property rootService reference to the service layer
 * @property application reference to the application object
 */
@Suppress("UNUSED_PARAMETER", "unused")
class ScoreMenuScene(
    private val rootService: RootService,
    private val application: BlokusApplication) : Refreshable, MenuScene(
        background = ColorVisual(r = 30, g = 30, b = 30)
    ) {

    /**
     * Label for displaying the ranking title.
     */
    private val rankingLabel = Label(
        posX = 0,
        posY = 30,
        width = 1920,
        height = 200,
        text = "Ranking",
        font = Font(
            color = Color.WHITE,
            size = 50,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * Container to store the participant ranking labels.
     */
    private val participantRankingGridPane = GridPane<Label>(
        posX = 960,
        posY = 540,
        rows = 4,
        columns = 1,
        spacing = 30
    )

    /**
     * Container to store the [closeButton] so that it is centered.
     */
    private val closeButtonGridPane = GridPane<Button>(
        posX = 960,
        posY = 900,
        rows = 1,
        columns = 1
    )

    /**
     * The close button which hides this scene.
     */
    private val closeButton = Button(
        text = "Close",
        visual = ColorVisual(r = 55, g = 55, b = 65),
        font = Font(color = Color.WHITE)
    ).apply {
        scale = 1.3
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                application.closeScoreMenuScene()
            }
        }
    }

    init {
        closeButtonGridPane[0, 0] = closeButton

        addComponents(rankingLabel, participantRankingGridPane, closeButtonGridPane)
    }

    /**
     * Updates this scene by displaying all the participant scores.
     *
     * This method displays the scores in that order in which they are given in the list.
     * The first [Pair] in the list is at the top of the ranking, the last [Pair] is at the bottom.
     *
     * @param participantScores the participant scores as a list of pairs
     *          in which each participant is associated with its score
     */
    override fun refreshAfterScore(participantScores: List<Pair<Int, Participant>>) {
        var currentPlace = 1
        var currentScore = participantScores[0].first
        for(i in participantScores.indices) {
            val currentPair = participantScores[i]

            // if the current participant has the same score as the previous one, it gets the same place,
            // otherwise the place is set to the current position in the list + 1
            if(currentPair.first != currentScore) {
                currentPlace = i+1
                currentScore = currentPair.first
            }

            participantRankingGridPane[0, i] = Label(
                text = "${currentPlace}. ${currentPair.second.name}: ${currentPair.first} points.",
                width = 1920,
                font = Font(
                    color = Color.WHITE,
                    size = 25,
                    fontWeight = Font.FontWeight.BOLD
                )
            )
        }
        application.showScoreMenuScene()
    }
}