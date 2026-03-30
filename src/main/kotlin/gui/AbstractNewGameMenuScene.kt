package gui

import entity.Color
import service.Refreshable
import service.RootService
import service.StartGameParams
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.ToggleButton
import tools.aqua.bgw.core.DEFAULT_BUTTON_HEIGHT
import tools.aqua.bgw.core.DEFAULT_TOGGLE_BUTTON_WIDTH
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.event.MouseButtonType
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual
import kotlin.random.Random

/**
 * Abstract class which represents the new game menu scene in which all participants are set up.
 *
 * Each participant gets a name and a color or could be an easy or a hard bot. It can be chosen between
 * the basic and advanced scoring and the board size can be set to 14x14 if only two participants want to play.
 *
 * @property rootService reference to the service layer
 * @property application the application object
 */
abstract class AbstractNewGameMenuScene(
    protected val rootService: RootService,
    private val application: BlokusApplication) : Refreshable, MenuScene(
        background = ColorVisual(r = 30, g = 30, b = 30)
    ) {

    /**
     * Inner class which models the participant name text field.
     *
     * @param prompt the prompt shown in the text field
     */
    protected open inner class ParticipantTextField(prompt: String) : TextField(
        prompt = prompt,
        visual = ColorVisual(r = 55, g = 55, b = 65),
        font = Font(color = tools.aqua.bgw.core.Color.WHITE)
    ) {
        init {
            scale = 1.3
            onTextChanged = {
                updateFieldSizeToggleButton()
                updateColorComboBoxes()
                updateShuffleParticipantsButton()
            }
        }
    }

    /**
     * Inner class which models the color selection combo box.
     */
    protected class ColorSelectionComboBox : ComboBox<Color>(
        visual = ColorVisual(r = 55, g = 55, b = 65),
        items = Color.entries,
        font = Font(color = tools.aqua.bgw.core.Color.WHITE)
    ) {
        init {
            scale = 1.3
        }
    }

    /**
     * Inner class which models the bot selection combo box.
     *
     * @property appeared if false, this combo box becomes invisible but stays in place
     */
    protected class BotSelectionComboBox : ComboBox<BotType>(
        visual = ColorVisual(r = 55, g = 55, b = 65),
        items = BotType.entries,
        disallowUnselect = true,
        font = Font(color = tools.aqua.bgw.core.Color.WHITE)
    ) {
        var appeared = true
            set(value) {
                if(value) {
                    visual = ColorVisual(r = 55, g = 55, b = 65)
                    items = BotType.entries
                    field = true
                } else {
                    visual = Visual.EMPTY
                    items = emptyList()
                    field = false
                }
            }

        init {
            scale = 1.3
        }
    }

    /**
     * Enum which is used to select the bot state of a participant.
     */
    protected enum class BotType {
        /**
         * The participant is not a bot.
         */
        NO,

        /**
         * The participant is an easy bot.
         */
        EASY,

        /**
         * The participant is a hard bot.
         */
        HARD;

        override fun toString(): String {
            return when (this) {
                NO -> "No"
                EASY -> "Easy"
                HARD -> "Hard"
            }
        }
    }

    /**
     * The number of inserted participants, i.e. the non-blank fields.
     */
    protected val playerNamesCount: Int
        get() {
            return participantTextFields.count {
                it.text.isNotBlank()
            }
        }

    /**
     * The central container which contains the [participantGridPane],
     * [colorGridPane], [botGridPane] and [toggleButtonsGridPane].
     */
    protected val centerGridPane = GridPane<GridPane<ComponentView>>(
        posX = 960,
        posY = 540,
        rows = 1,
        columns = 4,
        spacing = 80,
    )

    /**
     * The container for the [ParticipantTextField] objects and the [shuffleParticipantsButton].
     */
    protected val participantGridPane = GridPane<ComponentView>(
        rows = 6,
        columns = 1,
        spacing = 40
    )

    /**
     * The array for the [ParticipantTextField] objects.
     */
    protected abstract val participantTextFields: Array<ParticipantTextField>

    /**
     * The button to shuffle the participants.
     */
    protected val shuffleParticipantsButton = Button(
        text = "Shuffle",
        visual = ColorVisual(r = 55, g = 55, b = 65),
        font = Font(color = tools.aqua.bgw.core.Color.WHITE)
    ).apply {
        scale = 1.3
        isDisabled = true
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                try {
                    shuffleParticipants()
                } catch (e: IllegalArgumentException) {
                    displayErrorMessage(e.message)
                } catch (e: IllegalStateException) {
                    displayErrorMessage(e.message)
                }
            }
        }
    }

    /**
     * The container for the [ColorSelectionComboBox] objects and the [shuffleColorsButton].
     */
    protected val colorGridPane = GridPane<ComponentView>(
        rows = 6,
        columns = 1,
        spacing = 40
    )

    /**
     * The color label.
     */
    protected val colorLabel = Label(
        text = "Color",
        font = Font(
            color = tools.aqua.bgw.core.Color.WHITE,
            size = 20,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * The array for the [ColorSelectionComboBox] objects.
     */
    protected val colorComboBoxes = Array<ColorSelectionComboBox>(4) {
        ColorSelectionComboBox().apply {
            onItemSelected = {
                updateColorComboBoxes()
            }
        }
    }

    /**
     * The button to shuffle the colors.
     */
    protected val shuffleColorsButton = Button(
        text = "Shuffle",
        visual = ColorVisual(r = 55, g = 55, b = 65),
        font = Font(color = tools.aqua.bgw.core.Color.WHITE)
    ).apply {
        scale = 1.3
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                shuffleColors()
            }
        }
    }

    /**
     * The container for the [BotSelectionComboBox] objects.
     */
    protected val botGridPane = GridPane<ComponentView>(
        rows = 6,
        columns = 1,
        spacing = 40
    )

    /**
     * The bot label.
     */
    protected val botLabel = Label(
        text = "Bot?",
        font = Font(
            color = tools.aqua.bgw.core.Color.WHITE,
            size = 20,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * The array for the [BotSelectionComboBox] objects.
     */
    protected val botComboBoxes = Array<BotSelectionComboBox>(4) {
        BotSelectionComboBox()
    }

    /**
     * The container for the two toggle buttons
     * [fieldSizeToggleButton] and [scoringToggleButton].
     */
    protected val toggleButtonsGridPane = GridPane<ComponentView>(
        rows = 4,
        columns = 1,
        spacing = 40
    )

    /**
     * The toggle button to enable the small field size.
     */
    protected val fieldSizeToggleButton = ToggleButton(
        text = "Small Field",
        font = Font(color = tools.aqua.bgw.core.Color.WHITE),
        width = DEFAULT_TOGGLE_BUTTON_WIDTH + 8,
    ).apply {
        scale = 1.3
        isVisible = false
        onSelectionChanged = {
            updateColorComboBoxes()
        }
    }

    /**
     * The toggle button to enable advanced scoring.
     */
    protected val scoringToggleButton = ToggleButton(
        text = "Advanced Scoring",
        font = Font(color = tools.aqua.bgw.core.Color.WHITE),
        width = DEFAULT_TOGGLE_BUTTON_WIDTH + 8,
    ).apply {
        scale = 1.3
    }

    /**
     * The title label.
     */
    protected val titleLabel = Label(
        posX = 0,
        posY = 30,
        width = 1920,
        height = 200,
        text = "Blokus",
        font = Font(
            color = tools.aqua.bgw.core.Color.WHITE,
            size = 50,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * The error label which displays any error messages.
     */
    protected val errorLabel = Label(
        posX = 0,
        posY = 800,
        width = 1920,
        font = Font(
            color = tools.aqua.bgw.core.Color.WHITE,
            size = 20
        )
    )

    /**
     * The container for the [startButton].
     */
    protected val startButtonGridPane = GridPane<Button>(
        posX = 1600,
        posY = 900,
        rows = 1,
        columns = 1
    )

    /**
     * The start button which starts the game.
     */
    protected val startButton = Button(
        text = "START THE FUN!",
        visual = ColorVisual(r = 181, g = 230, b = 29)
    ).apply {
        scale = 1.8
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                try {
                    onStartButtonClicked()
                } catch (e: IllegalArgumentException) {
                    displayErrorMessage(e.message)
                } catch (e: IllegalStateException) {
                    displayErrorMessage(e.message)
                }
            }
        }
    }

    init {
        // a blank label to align this column along with the other ones
        participantGridPane[0, 0] = Label(visual = Visual.EMPTY)
        participantGridPane[0, 5] = shuffleParticipantsButton

        colorGridPane[0, 0] = colorLabel
        colorGridPane[0, 1] = colorComboBoxes[0]
        colorGridPane[0, 2] = colorComboBoxes[1]
        colorGridPane[0, 3] = colorComboBoxes[2]
        colorGridPane[0, 4] = colorComboBoxes[3]
        colorGridPane[0, 5] = shuffleColorsButton

        botGridPane[0, 0] = botLabel
        botGridPane[0, 1] = botComboBoxes[0]
        botGridPane[0, 2] = botComboBoxes[1]
        botGridPane[0, 3] = botComboBoxes[2]
        botGridPane[0, 4] = botComboBoxes[3]
        // a blank label to align this column along with the other ones
        botGridPane[0, 5] = Label(visual = Visual.EMPTY, height = DEFAULT_BUTTON_HEIGHT)

        // a blank label to align this column along with the other ones
        toggleButtonsGridPane[0, 0] = Label(visual = Visual.EMPTY)
        toggleButtonsGridPane[0, 1] = fieldSizeToggleButton
        toggleButtonsGridPane[0, 2] = scoringToggleButton
        // a blank label to align this column along with the other ones
        toggleButtonsGridPane[0, 3] = Label(visual = Visual.EMPTY, height = DEFAULT_BUTTON_HEIGHT)

        startButtonGridPane[0, 0] = startButton

        centerGridPane[0, 0] = participantGridPane
        centerGridPane[1, 0] = colorGridPane
        centerGridPane[2, 0] = botGridPane
        centerGridPane[3, 0] = toggleButtonsGridPane

        addComponents(titleLabel, errorLabel, centerGridPane, startButtonGridPane)
    }

    /**
     * Updates the [fieldSizeToggleButton] according to [playerNamesCount].
     *
     * It enables the toggle button if exactly two participants are named.
     */
    protected fun updateFieldSizeToggleButton() {
        fieldSizeToggleButton.isVisible = playerNamesCount == 2
    }

    /**
     * Updates the combo boxes inside [colorComboBoxes].
     *
     * The combo boxes are updated in this way that they do not contain colors which
     * are already selected. If only two participants take place, the corresponding
     * combo boxes contain only the colors which are safe to pick for that case.
     */
    protected fun updateColorComboBoxes() {
        colorComboBoxes.forEach { comboBox ->
            comboBox.items = Color.entries.filter { color ->
                if(color == comboBox.selectedItem) {
                    return@filter true
                }
                colorComboBoxes.all {
                    it.selectedItem != color
                }
            }
        }
        if(playerNamesCount == 2 && !fieldSizeToggleButton.isSelected) {
            val relevantColorComboBoxes = colorComboBoxes.filterIndexed { index, _ ->
                participantTextFields[index].text.isNotBlank()
            }
            val firstColor = relevantColorComboBoxes[0].selectedItem
            val secondColor = relevantColorComboBoxes[1].selectedItem
            if(firstColor != null) {
                relevantColorComboBoxes[1].items = relevantColorComboBoxes[1].items.filter {
                    it != firstColor.getCorrespondingColor()
                }
            }
            if(secondColor != null) {
                relevantColorComboBoxes[0].items = relevantColorComboBoxes[0].items.filter {
                    it != secondColor.getCorrespondingColor()
                }
            }
        }
    }

    /**
     * Enables the [shuffleParticipantsButton] if exactly four participants are named.
     * Otherwise, the button gets disabled.
     */
    protected fun updateShuffleParticipantsButton() {
        shuffleParticipantsButton.isDisabled = playerNamesCount != 4
    }

    /**
     * Reads all the user inputs and generates the corresponding [StartGameParams] object.
     *
     * @return the [StartGameParams] object
     * @throws IllegalArgumentException if any participant does not have a color
     */
    protected abstract fun getStartGameParams(): StartGameParams

    /**
     * Shuffles the participants if four of them exist
     *
     * @throws IllegalArgumentException if not four participants were specified
     */
    protected abstract fun shuffleParticipants()

    /**
     * Shuffles the colors. It always inserts new colors and does not consider already selected colors.
     */
    protected fun shuffleColors() {
        val relevantColorComboBoxes = colorComboBoxes.filterIndexed { index, _ ->
            participantTextFields[index].text.isNotBlank()
        }

        // for two players and four colors
        if(playerNamesCount == 2 && !fieldSizeToggleButton.isSelected) {
            val colorTuples = listOf(listOf(Color.BLUE, Color.BLUE.getCorrespondingColor()),
                                        listOf(Color.YELLOW, Color.YELLOW.getCorrespondingColor()))

            val randPlayer = (0..1).shuffled()

            relevantColorComboBoxes[0].selectedItem = colorTuples[randPlayer[0]][Random.nextInt(0, 2)]
            relevantColorComboBoxes[1].selectedItem = colorTuples[randPlayer[1]][Random.nextInt(0, 2)]
        } else {
            val colorPermutation = Color.entries.shuffled()
            for (i in relevantColorComboBoxes.indices) {
                relevantColorComboBoxes[i].selectedItem = colorPermutation[i]
            }
        }

        updateColorComboBoxes()
    }

    /**
     * Displays an error message on the screen for 5 seconds.
     *
     * @param message the error message
     */
    protected fun displayErrorMessage(message: String?) {
        errorLabel.text = "ERROR: $message"
        playAnimation(DelayAnimation(5000).apply {
            onFinished = {
                errorLabel.text = ""
            }
        })
    }

    /**
     * Executes when the [startButton] is pressed.
     * It transmits the input data to the service layer.
     *
     * @throws IllegalStateException if the user input is wrong
     */
    protected abstract fun onStartButtonClicked()

    /**
     * Returns the corresponding color of this color according to the color order.
     * These are the two colors played by one participant if only two participants play.
     *
     * It is [Color.RED] for [Color.BLUE], [Color.GREEN] for [Color.YELLOW] and vice versa.
     */
    protected fun Color.getCorrespondingColor(): Color {
        return when(this) {
            Color.BLUE -> Color.RED
            Color.YELLOW -> Color.GREEN
            Color.RED -> Color.BLUE
            Color.GREEN -> Color.YELLOW
        }
    }

    /**
     * Hides all menu scenes after the game is initialized.
     */
    override fun refreshAfterInitializeGame() {
        application.hideMenuScene()
    }
}