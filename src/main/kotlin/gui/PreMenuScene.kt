package gui

import service.ParticipantType
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode
import tools.aqua.bgw.event.MouseButtonType
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

/**
 * Class which represents the pre menu scene in which the user selects whether
 * a local, a remote or an old game (by loading the game) should be played.
 *
 * @property rootService reference to the service layer
 * @property application reference to the application object
 */
class PreMenuScene(
    private val rootService: RootService,
    private val application: BlokusApplication) : Refreshable, MenuScene(
        background = ColorVisual(r = 30, g = 30, b = 30)
    ) {

    /**
     * Inner class to model the buttons.
     *
     * @param text the text displayed in the button
     */
    private class PreMenuSceneButton(text: String) : Button(
        text = text,
        visual = ColorVisual(r = 55, g = 55, b = 65),
        font = Font(color = Color.WHITE)
    ) {
        init {
            scale = 2.0
        }
    }

    /**
     * Inner class to model the text fields.
     *
     * @param prompt the prompt displayed in the text field
     */
    private class PreMenuSceneTextField(prompt: String) : TextField(
        prompt = prompt,
        visual = ColorVisual(r = 55, g = 55, b = 65),
        font = Font(color = Color.WHITE)
    ) {
        init {
            scale = 1.3
        }
    }

    /**
     * Inner class which models the outer container for the user input.
     *
     * It stores the [createGameTextFieldGridPane], the [joinGameTextFieldGridPane]
     * and the [joinGameBotGridPane].
     */
    private class OuterInputGridPane : GridPane<GridPane<out ComponentView>>(
        rows = 1,
        columns = 2,
        spacing = 70
    )

    /**
     * Enum which is used to select the bot state of a participant.
     */
    private enum class BotType {
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
     * Container to store the buttons so that they are centered.
     */
    private val buttonGridPane = GridPane<Button>(
        posX = 960,
        posY = 540,
        rows = 4,
        columns = 1,
        spacing = 140,
    )

    /**
     * Container to store the separate grid panes for the user input fields.
     *
     * Aligns them next to the buttons.
     */
    private val inputGridPane = GridPane<GridPane<GridPane<out ComponentView>>>(
        posX = 1300,
        posY = 540,
        rows = 2,
        columns = 1,
        spacing = 105,
    )

    /**
     * The button to start a local game.
     */
    private val localGameButton = PreMenuSceneButton(text = "Local Game").apply {
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                application.showNewGameMenuScene()
            }
        }
    }

    /**
     * The button to create a remote game.
     */
    private val createGameButton = PreMenuSceneButton(text = "Create Game").apply {
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                onCreateGameButtonClicked()
            }
        }
    }

    /**
     * The outer container for the create-game input fields.
     */
    private val createGameOuterGridPane = OuterInputGridPane()

    /**
     * Container to store the create-game text fields so that they are centered.
     */
    private val createGameTextFieldGridPane = GridPane<TextField>(
        rows = 2,
        columns = 1,
        spacing = 20,
    )

    /**
     * The text field to insert the name of the host player.
     */
    private val createGameNameTextField = PreMenuSceneTextField(prompt = "Name")

    /**
     * The text field to insert the session ID from the host player.
     */
    private val createGameSessionIDTextField = PreMenuSceneTextField(prompt = "Session ID")

    /**
     * The button to join a remote game.
     */
    private val joinGameButton = PreMenuSceneButton(text = "Join Game").apply {
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                onJoinGameButtonClicked()
            }
        }
    }

    /**
     * The outer container for the join-game input fields.
     */
    private val joinGameOuterGridPane = OuterInputGridPane()

    /**
     * Container to store the join-game text fields so that they are centered.
     */
    private val joinGameTextFieldGridPane = GridPane<TextField>(
        rows = 2,
        columns = 1,
        spacing = 20,
    )

    /**
     * Container to store the join-game bot input elements.
     *
     * It stores the [joinGameBotLabel] and [joinGameBotComboBox].
     */
    private val joinGameBotGridPane = GridPane<ComponentView>(
        rows = 3,
        columns = 1,
        spacing = 20,
    )

    /**
     * The text field to insert the name of the guest player.
     */
    private val joinGameNameTextField = PreMenuSceneTextField(prompt = "Name")

    /**
     * The text field to insert the session ID from the guest player.
     */
    private val joinGameSessionIDTextField = PreMenuSceneTextField(prompt = "Session ID")

    /**
     * The combo box to select the bot state when joining a game.
     */
    private val joinGameBotComboBox = ComboBox<BotType>(
        visual = ColorVisual(r = 55, g = 55, b = 65),
        items = BotType.entries,
        disallowUnselect = true,
        font = Font(color = Color.WHITE)
    ).apply {
        scale = 1.3
    }

    /**
     * The bot label.
     */
    private val joinGameBotLabel = Label(
        text = "Bot?",
        font = Font(
            color = Color.WHITE,
            size = 20,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * The button to load an old game.
     */
    private val loadGameButton = PreMenuSceneButton(text = "Load Game").apply {
        onMouseClicked = { event ->
            if(event.button == MouseButtonType.LEFT_BUTTON) {
                application.showFileDialog(loadGameFileDialog)
            }
        }
    }

    /**
     * The file dialog which opens when the load game button is pressed.
     */
    private val loadGameFileDialog = FileDialog(
        mode = FileDialogMode.OPEN_FILE,
        title = "Select a game file"
    ).apply {
        onPathsSelected = { files ->
            require(files.size == 1) {"Only one game file is allowed"}
            rootService.ioService.loadGame(files[0])
        }
    }

    /**
     * Label for displaying the game title.
     */
    private val titleLabel = Label(
        posX = 0,
        posY = 30,
        width = 1920,
        height = 200,
        text = "Blokus",
        font = Font(
            color = Color.WHITE,
            size = 50,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    /**
     * The error label which displays any error messages.
     */
    private val errorLabel = Label(
        posX = 0,
        posY = 950,
        width = 1920,
        font = Font(
            color = Color.WHITE,
            size = 20
        )
    )

    init {
        buttonGridPane[0, 0] = localGameButton
        buttonGridPane[0, 1] = createGameButton
        buttonGridPane[0, 2] = joinGameButton
        buttonGridPane[0, 3] = loadGameButton

        createGameTextFieldGridPane[0, 0] = createGameNameTextField
        createGameTextFieldGridPane[0, 1] = createGameSessionIDTextField

        createGameOuterGridPane[0, 0] = createGameTextFieldGridPane
        // blank container so that the elements align with the join-game containers
        createGameOuterGridPane[1, 0] = GridPane<ComponentView>(
            rows = 1,
            columns = 1,
        ).apply {
            set(0, 0, Label(
                visual = Visual.EMPTY
            ))
        }

        joinGameTextFieldGridPane[0, 0] = joinGameNameTextField
        joinGameTextFieldGridPane[0, 1] = joinGameSessionIDTextField

        joinGameBotGridPane[0, 0] = joinGameBotLabel
        joinGameBotGridPane[0, 1] = joinGameBotComboBox

        joinGameOuterGridPane[0, 0] = joinGameTextFieldGridPane
        joinGameOuterGridPane[1, 0] = joinGameBotGridPane

        inputGridPane[0, 0] = createGameOuterGridPane
        inputGridPane[0, 1] = joinGameOuterGridPane

        addComponents(buttonGridPane, inputGridPane, titleLabel, errorLabel)
    }

    /**
     * Reads the input from the text fields in [createGameTextFieldGridPane]
     * and attempts to create a remote game.
     *
     * Any error are printed in the scene.
     */
    private fun onCreateGameButtonClicked() {
        val name = createGameNameTextField.text
        val sessionID = createGameSessionIDTextField.text
        try {
            // since the secret is irrelevant, a non-blank secret will be passed
            rootService.networkService.hostGame("dummy", name, sessionID)
            application.showHostNewGameMenuScene()
        } catch (e: IllegalStateException) {
            displayErrorMessage(e.message)
        } catch (e: IllegalArgumentException) {
            displayErrorMessage(e.message)
        }
    }

    /**
     * Reads the input from the text fields in [joinGameTextFieldGridPane] and
     * [joinGameBotComboBox] and attempts to join a remote game.
     *
     * Any error are printed in the scene.
     */
    private fun onJoinGameButtonClicked() {
        val name = joinGameNameTextField.text
        val sessionID = joinGameSessionIDTextField.text
        try {
            val botType = when(joinGameBotComboBox.selectedItem) {
                BotType.NO -> ParticipantType.PLAYER
                BotType.EASY -> ParticipantType.EASY_BOT
                BotType.HARD -> ParticipantType.STRONG_BOT
                else -> {}
            } as ParticipantType
            // since the secret is irrelevant, a non-blank secret will be passed
            rootService.networkService.joinGame("dummy", name, botType, sessionID)
            application.showLobbyMenuScene()
        } catch (e: IllegalStateException) {
            displayErrorMessage(e.message)
        } catch (e: IllegalArgumentException) {
            displayErrorMessage(e.message)
        }
    }

    /**
     * Displays an error message on the screen for 5 seconds.
     *
     * @param message the error message
     */
    private fun displayErrorMessage(message: String?) {
        errorLabel.text = "ERROR: $message"
        playAnimation(DelayAnimation(5000).apply {
            onFinished = {
                errorLabel.text = ""
            }
        })
    }
}