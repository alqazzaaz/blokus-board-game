package entity

import java.io.Serializable

/**
 * abstract class to model the needed attributes for all types of participants
 * @param name the name of the participant
 * @param actionAvailable if the participant could perform another action
 * @param color the color of the participant
 * @param pieceList the list of the remaining pieces
 * @param lastPlacedPiece the last placed piece
 */
abstract class Participant(
    open val name: String,
    open var actionAvailable: Boolean,
    open val color: Color,
    val pieceList: MutableList<Piece> = mutableListOf(),
    open var lastPlacedPiece: Piece? = null): Serializable {

    /**
     * initialize the pieceList
     * based on naming conventions from the wiki
     */
    init {
        //I5
        pieceList.add(Piece(arrayOf(intArrayOf(1,1,1,1,1)),"I5"))
        //N5
        pieceList.add(Piece(arrayOf(intArrayOf(0,1,1,1),intArrayOf(1,1,0,0)),"N5"))
        //V5
        pieceList.add(Piece(arrayOf(intArrayOf(1,0,0),intArrayOf(1,0,0),intArrayOf(1,1,1)),"V5"))
        //T5
        pieceList.add(Piece(arrayOf(intArrayOf(0,1,0),intArrayOf(0,1,0),intArrayOf(1,1,1)),"T5"))
        //U5
        pieceList.add(Piece(arrayOf(intArrayOf(1,1,1),intArrayOf(1,0,1)),"U5"))
        //L5
        pieceList.add(Piece(arrayOf(intArrayOf(1,1,1,1),intArrayOf(1,0,0,0)),"L5"))
        //Y5
        pieceList.add(Piece(arrayOf(intArrayOf(1,1,1,1),intArrayOf(0,1,0,0)),"Y5"))
        //Z5
        pieceList.add(Piece(arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1),intArrayOf(0,0,1)),"Z5"))
        //W5
        pieceList.add(Piece(arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,0),intArrayOf(0,1,1)),"W5"))
        //P5
        pieceList.add(Piece(arrayOf(intArrayOf(1,1),intArrayOf(1,1),intArrayOf(1,0)),"P5"))
        //X5
        pieceList.add(Piece(arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(0,1,0)),"X5"))
        //F5
        pieceList.add(Piece(arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(1,0,0)),"F5"))
        //Z4
        pieceList.add(Piece(arrayOf(intArrayOf(1,0),intArrayOf(1,1),intArrayOf(0,1)),"Z4"))
        //I4
        pieceList.add(Piece(arrayOf(intArrayOf(1,1,1,1)),"I4"))
        //L4
        pieceList.add(Piece(arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1)),"L4"))
        //O4
        pieceList.add(Piece(arrayOf(intArrayOf(1,1),intArrayOf(1,1)),"O4"))
        //T4
        pieceList.add(Piece(arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1)),"T4"))
        //I3
        pieceList.add(Piece(arrayOf(intArrayOf(1,1,1)),"I3"))
        //V3
        pieceList.add(Piece(arrayOf(intArrayOf(1,1),intArrayOf(0,1)),"V3"))
        //I2
        pieceList.add(Piece(arrayOf(intArrayOf(1,1)),"I2"))
        //O1
        pieceList.add(Piece(arrayOf(intArrayOf(1)),"O1"))
    }

    /**
     * Companion object for the serialVersionUID
     */
    companion object {
        private const val serialVersionUID: Long = 7
    }
}