package com.mironov.checkers

import androidx.lifecycle.ViewModel


class GameLogic : ViewModel() {

    var whichTurn: HasChip = HasChip.LIGHT

     var chipsPositionArray = arrayOf<Array<HasChip>>()
    private var allowedMovesAll = arrayOf<Array<Boolean>>()
    private var allowedMovesCurrent = arrayOf<Array<Boolean>>()

    init {
        var array = arrayOf<HasChip>()
        var arrayMoves = arrayOf<Boolean>()
        var arrayMovesAll = arrayOf<Boolean>()
        for (j in 0..7) {
            for (i in 0..7) {
                array += HasChip.EMPTY
                arrayMoves += false
                arrayMovesAll += false
            }
            chipsPositionArray += array
            allowedMovesAll += arrayMovesAll
            allowedMovesCurrent += arrayMoves
            //clear arrays
            array = arrayOf<HasChip>()
            arrayMoves = arrayOf<Boolean>()
            arrayMovesAll = arrayOf<Boolean>()
        }
    }

    /** Is move from i1,j1 to i2,j2 is allowed
     * @param i1 picked tile index i
     * @param j1 picked tile index j
     * @param i2 index i of tile to put chip
     * @param j2 index j of tile to put chip
     */
    fun moveIsAllowed(i1: Int, j1: Int, i2: Int, j2: Int): Boolean {
        calculateAllowedMoves(i1, j1, allowedMovesCurrent)
        if (chipsPositionArray[j2][i2] == HasChip.EMPTY) {
            return allowedMovesCurrent[j2][i2]
        }
        return false
    }

    fun updatePosition(i1: Int, j1: Int, i2: Int, j2: Int, chipColor: HasChip) {
        //Remove chip from old position
        chipsPositionArray[j1][i1] = HasChip.EMPTY
        //Put chip to new position
        chipsPositionArray[j2][i2] = chipColor


        //Find chip to eat
        //Direction to move by j Index
        val inc=if(j2-j1>0){1}else{-1}
        for (i in i1 until i2){
            val j=j1+inc
            if(chipsPositionArray[j][i]!=HasChip.EMPTY&&chipsPositionArray[j][i]!=whichTurn){
                chipsPositionArray[j][i]=HasChip.EMPTY
            }
        }


        //Change who moves`
        if (whichTurn == HasChip.LIGHT) {
            whichTurn = HasChip.DARK
        } else {
            whichTurn = HasChip.LIGHT
        }
    }

    fun setChipAtPos(i: Int, j: Int, chipColor: HasChip) {
        chipsPositionArray[j][i] = chipColor
    }

    //Get allowed directions for Current chip
    /**
     * @param i picked tile index i
     * @param j picked tile index j
     */
    fun getAllowedMoves(
        i: Int,
        j: Int
    ): Array<Array<Boolean>> {
        //reset allowed moves
        for (j in 0..7) {
            for (i in 0..7) {
                allowedMovesCurrent[j][i] = false
            }
        }
        calculateAllowedMoves(i, j, allowedMovesCurrent)
        return allowedMovesCurrent
    }

    //Calculate allowed directions for Current chip
    /**
     * @param i picked tile index i
     * @param j picked tile index j
     * @param allowedMoves Matrix of allowed moves to update
     */
    private fun calculateAllowedMoves(
        i: Int,
        j: Int,
        allowedMoves: Array<Array<Boolean>>
    ) {
        //direction of chip move
        var directionInc = 0
        directionInc = if (whichTurn == HasChip.LIGHT) -1 else 1
        if (checkBoardBonds(j + directionInc, i)) {
            //Check left
            checkDirection(j, i, directionInc,  1, allowedMoves)
            //Check right
            checkDirection(j, i, directionInc, -1, allowedMoves)
        }
    }


    private fun checkBoardBonds(i: Int, j: Int): Boolean {
        if (i in 0..7 && j in 0..7) {
            return true
        }
        return false
    }

    private fun checkPosition(j: Int, i: Int, type: HasChip): Boolean {
        if (chipsPositionArray[j][i] == type) {
            return true
        }
        return false
    }

    /**
     * * @param j chip index j
     * @param i chip index i
     * @param dirJInc direction to increment j (+is down)
     * @param dirIInc direction to increment i (+is left)
     * @param allowedMoves Matrix of allowed moves to update
     */
    private fun checkDirection(
        j: Int,
        i: Int,
        dirJInc: Int,
        dirIInc: Int,
        allowedMoves: Array<Array<Boolean>>
    ) {
        if (checkBoardBonds(j + dirJInc, i + dirIInc)) {
            //if empty
            if (checkPosition(j + dirJInc, i + dirIInc, HasChip.EMPTY)) {
                allowedMoves[j + dirJInc][i + dirIInc] = true
            }
            //If next chip color is different
            else if (checkBoardBonds(j + 2 * dirJInc, i + 2 * dirIInc)) {
                //check eat
                    val oppositeChip = if(whichTurn==HasChip.LIGHT){HasChip.DARK}else{HasChip.LIGHT}
                if (checkPosition(j + 1 * dirJInc, i + 1 * dirIInc, oppositeChip)) {
                    if (checkPosition(j + 2 * dirJInc, i + 2 * dirIInc, HasChip.EMPTY)) {
                        allowedMoves[j + 2 * dirJInc][i + 2 * dirIInc] = true
                    }
                }
            }
        }
    }

    //Calculate All possible moves
    fun calculateAllowedMovesForAll():Array<Array<Boolean>>  {
        //reset allowed moves
        for (j in 0..7) {
            for (i in 0..7) {
                allowedMovesAll[j][i] = false
            }
        }
        //Find chips for move
        for (j in 0..7) {
            for (i in 0..7) {
                if (chipsPositionArray[j][i] == whichTurn) {
                    //Check possible moves if tile is empty
                    calculateAllowedMoves(i, j, allowedMovesAll)
                }
            }
        }
        return allowedMovesAll
    }
}