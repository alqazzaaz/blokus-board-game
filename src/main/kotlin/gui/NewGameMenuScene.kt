package gui

import entity.Color
import service.ParticipantType
import service.RootService
import service.StartGameParams

/**
 * Implementation of [AbstractNewGameMenuScene] in which each participant is added
 * manually by the user.
 *
 * This menu scene is used to start a local game.
 */
class NewGameMenuScene(
    rootService: RootService,
    application: BlokusApplication) : AbstractNewGameMenuScene(rootService, application) {

    override val participantTextFields = Array(4) {
        ParticipantTextField("Participant ${it+1}")
    }

    init {
        participantGridPane[0, 1] = participantTextFields[0]
        participantGridPane[0, 2] = participantTextFields[1]
        participantGridPane[0, 3] = participantTextFields[2]
        participantGridPane[0, 4] = participantTextFields[3]
    }

    override fun getStartGameParams(): StartGameParams {
        val participants = mutableListOf<Triple<String, Color, ParticipantType>>()
        participantTextFields.filter {
            it.text.isNotBlank()
        }.forEachIndexed { index, field ->
            val selectedColor = colorComboBoxes[index].selectedItem
                ?: throw IllegalArgumentException("All participants must have a color.")
            val participantType: ParticipantType = when (botComboBoxes[index].selectedItem) {
                BotType.NO -> ParticipantType.PLAYER
                BotType.EASY -> ParticipantType.EASY_BOT
                BotType.HARD -> ParticipantType.STRONG_BOT
                else -> null
            } as ParticipantType
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

        val permutatedParticipantNames = Array(4) { index ->
            participantTextFields[permutation[index]].text
        }

        val permutatedColors = Array(4) { index ->
            colorComboBoxes[permutation[index]].selectedItem
        }

        val permutatedBotType = Array(4) { index ->
            botComboBoxes[permutation[index]].selectedItem
        }

        participantTextFields.forEachIndexed { index, element ->
            element.text = permutatedParticipantNames[index]
        }

        colorComboBoxes.forEachIndexed { index, element ->
            element.selectedItem = permutatedColors[index]
        }

        botComboBoxes.forEachIndexed { index, element ->
            element.selectedItem = permutatedBotType[index]
        }

        updateColorComboBoxes()
    }

    override fun onStartButtonClicked() {
        rootService.gameService.startGame(params = getStartGameParams())
    }
}