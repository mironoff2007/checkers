package com.mironov.checkers

import androidx.lifecycle.ViewModel


class GameLogic : ViewModel() {

    var witchTurn: HasChip = HasChip.LIGHT

    private var chipsPositionArray = arrayOf<Array<HasChip>>()
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
    fun moveIsAllowed(i1: Int, j1: Int, i2: Int, j2: Int, chipColor: HasChip): Boolean {
        calculateAllowedMoves(i1, j1, chipColor, allowedMovesCurrent)
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
        //Change who moves
        if (witchTurn == HasChip.LIGHT) {
            witchTurn = HasChip.DARK
        } else {
            witchTurn = HasChip.LIGHT
        }
    }

    fun setShipAtPos(i: Int, j: Int, chipColor: HasChip) {
        chipsPositionArray[j][i] = chipColor
    }

    //Get allowed directions for Current chip
    /**
     * @param i picked tile index i
     * @param j picked tile index j
     * @param whichChipTurn white or black turn
     */
    fun getAllowedMoves(
        i: Int,
        j: Int,
        whichChipTurn: HasChip
    ): Array<Array<Boolean>> {
        calculateAllowedMoves(i, j, whichChipTurn, allowedMovesCurrent)
        return allowedMovesCurrent
    }

    //Calculate allowed directions for Current chip
    /**
     * @param i picked tile index i
     * @param j picked tile index j
     * @param whichChipTurn white or black turn
     * @param allowedMoves Matrix of allowed moves to update
     */
    private fun calculateAllowedMoves(
        i: Int,
        j: Int,
        whichChipTurn: HasChip,
        allowedMoves: Array<Array<Boolean>>
    ) {
        //reset allowed moves
        for (j in 0..7) {
            for (i in 0..7) {
                allowedMoves[j][i] = false
            }
        }
        //direction of chip move
        var directionInc = 0
        directionInc = if (whichChipTurn == HasChip.LIGHT) -1 else 1
        if (j + directionInc > 0) {
            //Check left
            if (i - 1 >= 0) {
                if (chipsPositionArray[j + directionInc][i - 1] == HasChip.EMPTY) {
                    allowedMoves[j + directionInc][i - 1] = true
                }
            }
            //Check right
            if (i + 1 <= 7) {
                if (chipsPositionArray[j + directionInc][i + 1] == HasChip.EMPTY) {
                    allowedMoves[j + directionInc][i + 1] = true
                }
            }
        }
    }

    //Calculate All possible moves
    fun calculateAllowedMovesForAll(whichChipTurn: HasChip) {
        //Find chips for move
        for (j in 0..7) {
            for (i in 0..7) {
                if (chipsPositionArray[j][i] == whichChipTurn) {
                    //Check possible moves if tile is empty
                    calculateAllowedMoves(i, j, whichChipTurn, allowedMovesAll)
                }
            }
        }
    }
}