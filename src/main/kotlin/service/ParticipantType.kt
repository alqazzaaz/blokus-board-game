package service

/**

 * the class represents the different types of participants who can participate in the game
 * this enumeration is used during game configuration to specify
 * how a certain color is controlled on the playing field
 *
 * @property NETWORK_PARTICIPANT a player who plays via the BGW network module
 * @property PLAYER a local human player sitting at this computer
 * @property STRONG_BOT a computer-controlled player with advanced Level
 * @property EASY_BOT a simple computer-controlled player
 * @property MULTI_CONTROLLED_PARTICIPANT represents the split color in the 3 player variant,
 * which is alternately controlled by the other three real players
 */

enum class ParticipantType {
    NETWORK_PARTICIPANT,
    PLAYER,
    STRONG_BOT,
    EASY_BOT,
    MULTI_CONTROLLED_PARTICIPANT,
}
