package gui

import entity.Color
import service.ParticipantType
import service.RootService
import service.StartGameParams
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.util.Font

/**
 * Implementation of [AbstractNewGameMenuScene] in which each participant except for the host
 * is added automatically by a server connection.
 *
 * The host is only able to instantiate each participant's color.
 *
 * This menu scene is used to start a remote game.
 */
class HostNewGameMenuScene(
    rootService: RootService,
    application: BlokusApplication) : AbstractNewGameMenuScene(rootService, application) {

    /**
     * Inner class which models the participant name text field for the network players.
     *
     * @property isHost whether this text field is the input of the host name
     * @param prompt the prompt shown in the text field
     */
    private inner class NetworkParticipantTextField(
        var isHost: Boolean,
        prompt: String) : ParticipantTextField(prompt) {
        init {
            isDisabled = true
        }
    }

    override val participantTextFields: Array<ParticipantTextField> = Array(4) { index ->
        if(index == 0) {
            NetworkParticipantTextField(isHost = true, prompt = "You")
        } else {
            NetworkParticipantTextField(isHost = false, "Remote $index")
        }
    }

    /**
     * The session ID label.
     */
    private val sessionIDLabel = Label(
        posX = 0,
        posY = 0,
        width = 1920,
        alignment = Alignment.CENTER_LEFT,
        font = Font(
            color = tools.aqua.bgw.core.Color.WHITE,
            size = 20,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    init {
        participantGridPane[0, 1] = participantTextFields[0]
        participantGridPane[0, 2] = participantTextFields[1]
        participantGridPane[0, 3] = participantTextFields[2]
        participantGridPane[0, 4] = participantTextFields[3]

        for(i in 1..3) {
            botComboBoxes[i].appeared = false
        }

        addComponents(sessionIDLabel)
    }

    /**
     * Updates the host name inside the scene.
     *
     * It retrieves the host username from the network layer
     * and inserts it in the host field.
     *
     * @param delay the delay of the update (in milliseconds)
     */
    fun updateHostName(delay: Int = 0) {
        playAnimation(DelayAnimation(delay).apply {
            onFinished = {
                val hostName = rootService.networkService.client?.playerName
                if (hostName != null) {
                    participantTextFields.first { (it as NetworkParticipantTextField).isHost }.text = hostName
                }
            }
        })
    }

    /**
     * Updates the session ID inside the scene.
     *
     * It retrieves the sessionID from the network layer.
     *
     * @param delay the delay of the update (in milliseconds)
     */
    fun updateSessionID(delay: Int = 0) {
        playAnimation(DelayAnimation(delay).apply {
            onFinished = {
                val sessionID = rootService.networkService.client?.sessionID
                sessionIDLabel.text = "SessionID: $sessionID"
            }
        })
    }

    override fun getStartGameParams(): StartGameParams {
        val participants = mutableListOf<Triple<String, Color, ParticipantType>>()
        participantTextFields.filter {
            it.text.isNotBlank()
        }.forEachIndexed { index, field ->
            val selectedColor = colorComboBoxes[index].selectedItem
                ?: throw IllegalArgumentException("All participants must have a color.")
            var participantType = ParticipantType.NETWORK_PARTICIPANT
            if((participantTextFields[index] as NetworkParticipantTextField).isHost) {
                participantType = when (botComboBoxes[index].selectedItem) {
                    BotType.NO -> ParticipantType.PLAYER
                    BotType.EASY -> ParticipantType.EASY_BOT
                    BotType.HARD -> ParticipantType.STRONG_BOT
                    else -> null
                } as ParticipantType
            }
            participants.add(
                Triple(
                    field.text,
                    selectedColor,
                    participantType
                )
            )
        }
        return StartGameParams(participants,
            isBigField = playerNamesCount != 2 || !fieldSizeToggleButton.isSelected,
            isBasicScoring = !scoringToggleButton.isSelected)
    }

    override fun shuffleParticipants() {
        require(playerNamesCount == 4) {
            "Shuffling participants is only possible with 4 participants."
        }
        val permutation = listOf(0, 1, 2, 3).shuffled()

        val permutatedPrompts = Array(4) { index ->
            participantTextFields[permutation[index]].prompt
        }

        val permutatedParticipantNames = Array(4) { index ->
            participantTextFields[permutation[index]].text
        }

        val permutatedHostState = Array(4) { index ->
            (participantTextFields[permutation[index]] as NetworkParticipantTextField).isHost
        }

        val permutatedColors = Array(4) { index ->
            colorComboBoxes[permutation[index]].selectedItem
        }

        val permutatedBotType = Array(4) { index ->
            botComboBoxes[permutation[index]].selectedItem
        }

        val permutatedBotAppearance = Array(4) { index ->
            botComboBoxes[permutation[index]].appeared
        }

        participantTextFields.forEachIndexed { index, element ->
            element.text = permutatedParticipantNames[index]
            element.prompt = permutatedPrompts[index]
            (element as NetworkParticipantTextField).isHost = permutatedHostState[index]
        }

        colorComboBoxes.forEachIndexed { index, element ->
            element.selectedItem = permutatedColors[index]
        }

        botComboBoxes.forEachIndexed { index, element ->
            element.selectedItem = permutatedBotType[index]
            element.appeared = permutatedBotAppearance[index]
        }

        updateColorComboBoxes()
    }

    override fun onStartButtonClicked() {
        rootService.networkService.startNewHostedGame(params = getStartGameParams())
    }

    override fun refreshAfterParticipantJoined(name: String) {
        participantTextFields.first {
            !(it as NetworkParticipantTextField).isHost && it.text.isBlank()
        }.text = name
    }

    override fun refreshAfterParticipantLeft(name: String) {
        participantTextFields.first {
            !(it as NetworkParticipantTextField).isHost && it.text == name
        }.text = ""
    }
}