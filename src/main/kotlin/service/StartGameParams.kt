package service

import entity.Color

/**
 * the class holds all the necessary parameters to start a new Blokus Game
 *
 * @property participants list that defines all participants in the game
 * each entry is a [triple] consisting of
 * [String] the unique name of the participant
 *  [Color]the game color assigned to this participant
 * [ParticipantType] the type of the control
 * @property isBigField determines the size of the game board,
 * 14x14 board for the 2-player variant and 20x20 is used for the standard board
 *
 * @property isBasicScoring sets the rules for endpoints, Basic Scoring or Advanced Scoring
 */
class StartGameParams(

    val participants : List<Triple<String, Color, ParticipantType>>,
    val isBigField : Boolean,
    val isBasicScoring : Boolean,
)
