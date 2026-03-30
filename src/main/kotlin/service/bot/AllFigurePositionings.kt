package service.bot

/**
 * This object contains pre computed all corners for all rotations/flips for every figure
 * a corner is the figures part which can be placed onto an allowed placing cell (a corner)
 * of another figure of the same color
 */
object AllFigurePositionings {

    /**
     * the dataclass for storing a rotation.
     * contains the figure bitmap and the list of corners
     */
    data class Rotation(
        val figure: Array<IntArray>,
        val corners: List<Pair<Int, Int>>
    )

    /**
     * huge map of the described positionings. i even dont know jow many elements and i dont wanna know.
     * and yes. i have gone over every single one of them per hand.
     */
    val positionings: Map<String, List<Rotation>> = mapOf(
        "F5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0),intArrayOf(1, 1, 1),intArrayOf(1, 0, 0)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 2), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0),intArrayOf(1, 1, 1),intArrayOf(0, 0, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 2), Pair(2, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0),intArrayOf(1, 1, 1),intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 2), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1),intArrayOf(1, 1, 1),intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 2), Pair(1, 0), Pair(1, 2), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 0),intArrayOf(0, 1, 1),intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 2), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0),intArrayOf(0, 1, 1),intArrayOf(1, 1, 0)),
                corners = listOf(Pair(0, 1), Pair(1, 2), Pair(2, 1), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 1),intArrayOf(1, 1, 0),intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0),intArrayOf(1, 1, 0),intArrayOf(0, 1, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(2, 2), Pair(2, 1))
            ),
        ),
        "Y5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(2, 1), Pair(3, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 1), intArrayOf(0, 1, 0, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 3), Pair(1, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(1, 1), Pair(3, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1, 0), intArrayOf(1, 1, 1, 1)),
                corners = listOf(Pair(0, 2), Pair(1, 0), Pair(1, 3))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 1), Pair(2, 0), Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 1), intArrayOf(0, 0, 1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 3), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0, 0), intArrayOf(1, 1, 1, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 3))
            )
        ),

        "N5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 1, 1), intArrayOf(1, 1, 0, 0)),
                corners = listOf(Pair(0, 1), Pair(0, 3), Pair(1, 0), Pair(1, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1),Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 0), intArrayOf(0, 0, 1, 1)),
                corners = listOf(Pair(0, 0),Pair(0, 2), Pair(1, 2), Pair(1, 3))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(3, 0))
            ),//
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 0, 0), intArrayOf(0, 1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(1, 3))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(2, 0), Pair(2, 1), Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1, 1), intArrayOf(1, 1, 1, 0)),
                corners = listOf(Pair(0, 3), Pair(0, 2), Pair(1, 0), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 1), Pair(2, 0), Pair(2, 1), Pair(3, 0))
            )
        ),

        "W5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1), Pair(2, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0), intArrayOf(1, 0, 0)),
                corners = listOf(Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(1, 1), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1), intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
                corners = listOf(Pair(0, 2), Pair(1, 1), Pair(1, 2), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1), intArrayOf(0, 0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(1, 2), Pair(2, 2))
            )
        ),

        "X5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1), intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 2), Pair(2, 1))
            )
        ),

        "Z5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 0), intArrayOf(0, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(2, 1), Pair(2, 2))
            ),
            Rotation(
                figure = arrayOf( intArrayOf(0, 1, 1), intArrayOf(0, 1, 0),intArrayOf(1, 1, 0)),
                corners = listOf(Pair(0, 1), Pair(0, 2), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf( intArrayOf(0, 0, 1), intArrayOf(1, 1, 1),intArrayOf(1, 0, 0)),
                corners = listOf(Pair(0, 2), Pair(1, 0), Pair(1, 2), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1),intArrayOf(0, 0, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 2), Pair(2, 2))
            ),

            ),

        "T5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 0), intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1), intArrayOf(1, 0, 0)),
                corners = listOf(Pair(0, 0), Pair(1, 2), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0), intArrayOf(0, 1, 0), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 1), Pair(2, 0), Pair(2, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1), intArrayOf(0, 0, 1)),
                corners = listOf(Pair(0, 2), Pair(1, 0), Pair(2, 2))
            )
        ),
        "P5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 1), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 1), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 0), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(1, 1), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(0, 2))
            )
        ),
        "U5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 1), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(1, 0), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(2, 0), Pair(2, 1))
            )
        ),

        "I5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 4))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
                corners = listOf(Pair(0, 0), Pair(4, 0))
            )
        ),

        "V5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(2, 0), Pair(2, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 0, 0), intArrayOf(1, 0, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 0, 1), intArrayOf(0, 0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1), intArrayOf(0, 0, 1), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 2), Pair(2, 0), Pair(2, 2))
            )
        ),

        "L5" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(3, 0), Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 1), intArrayOf(1, 0, 0, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 3), Pair(1, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 0, 1), intArrayOf(1, 1, 1, 1)),
                corners = listOf(Pair(0, 3), Pair(1, 0), Pair(1, 3))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(3, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0, 0), intArrayOf(1, 1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 3))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 1), Pair(3, 0), Pair(3, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 1), intArrayOf(0, 0, 0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 3), Pair(1, 3))
            )
        ),
        "O1" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1)),
                corners = listOf(Pair(0, 0))
            )
        ),

        "I2" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1), intArrayOf(1)),
                corners = listOf(Pair(0, 0), Pair(1, 0))
            )
        ),

        "I3" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1)),
                corners = listOf(Pair(0, 0), Pair(2, 0))
            )
        ),

        "V3" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1))
            )
        ),

        "I4" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 3))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
                corners = listOf(Pair(0, 0), Pair(3, 0))
            )
        ),

        "O4" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1))
            )
        ),

        "Z4" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
                corners = listOf(Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(0, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(2, 0))
            )
        ),

        "L4" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 0), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 0, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(1, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 2), Pair(1, 0), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 1), Pair(2, 0))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(1, 1)),
                corners = listOf(Pair(0, 1), Pair(2, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 0, 1)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(1, 2))
            )
        ),

        "T4" to listOf(
            Rotation(
                figure = arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 0)),
                corners = listOf(Pair(0, 0), Pair(0, 2), Pair(1, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(0, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(2, 1))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1)),
                corners = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 2))
            ),
            Rotation(
                figure = arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 0)),
                corners = listOf(Pair(0, 0), Pair(1, 1), Pair(2, 0))
            )
        )
    )
}
