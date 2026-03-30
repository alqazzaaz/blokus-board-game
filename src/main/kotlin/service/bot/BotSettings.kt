package service.bot

/**
 * main bot settins object. has info about hard bot scoring properties or
 * of the game state tree
 */
object BotSettings {
    // the depth of the tree depends on the state of the game
    // in the first MAXDEPTHCHANGE rounds the MAXGAMETREEDEPTHBEGINNING is used
    // afterwards - MAXGAMETREEDEPTHEND
    const val MAXDEPTHCHANGE = 6
    // the max depth of the calculated tree in the beginning
    const val MAXGAMETREEDEPTHBEGINNING = 3
    // the max depth of the calculated tree in the end
    const val MAXGAMETREEDEPTHEND = 7
    // max number of "children of the root"
    const val MAXTURNPOSSIBILITIES = 5
    // max number of children of a usual node
    const val MAXCHILDRENNUM = 2

    // in the game three a different set of scoring methods is used.
    // for the first 21 - SCORECHANGE moves the same scoring is used
    // as for the turn selection
    // after the first 21 - SCORECHANGE moves is used the score
    // of how many blocks are on the board
    const val SCORECHANGE = 15

    // --- constants for the score
    // how important is the number of possible moves after made move
    const val POSSIBLEMOVESIMPORTANCE = 2
    // importance of the number of placed cells
    const val CELLSPLACEDIMPORTANCE = 10
    // how important it is to fuck up an opponent
    const val OPPONENTTOUCHIMPORTANCE = 0.5

    // first most optimal figures for hard bot
    val BESTOPENFIGURES = listOf(
        "W5", "N5", "Y5"
    )
}
