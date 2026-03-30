package service.bot

import entity.Piece

/**
 * the cache for saving already calculated but turns
 */
data class BotTurnCache (
    val posX: Int,
    val posY: Int,
    val figure: Array<IntArray>,
    ) {
    /**
     * overriden equals and cache is used for comparison and check
     * if the given move have already been made
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BotTurnCache) return false

        return posX == other.posX &&
                posY == other.posY &&
                figure.contentDeepEquals(other.figure)
    }
    override fun hashCode(): Int {
        var result = posX
        result = 31 * result + posY
        result = 31 * result + figure.contentDeepHashCode()
        return result
    }
}

/**
 * the main bot turn class, which saved a bot turn
 * as the position, figure and the score.
 */
class BotTurn (
    val posX: Int,
    val posY: Int,
    val figure: Piece,
    var turnScore: Double
    )

