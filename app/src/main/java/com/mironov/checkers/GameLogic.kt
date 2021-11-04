package com.mironov.checkers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class GameLogic : ViewModel() {

    var whichTurn: HasChip = HasChip.LIGHT

    var chipsPositionArray = arrayOf<Array<HasChip>>()
    @Volatile
    private var allowedMovesAll = arrayOf<Array<Boolean>>()
    @Volatile
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
        calculateAllowedMoves(j1, i1, allowedMovesCurrent)
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
        val inc = if (j2 - j1 > 0) {
            1
        } else {
            -1
        }
        var j=j1
        for (i in i1 until i2) {
            if (chipsPositionArray[j][i] != HasChip.EMPTY && chipsPositionArray[j][i] != whichTurn) {
                chipsPositionArray[j][i] = HasChip.EMPTY
            }
            j = j + inc
        }
        j=j1
        for (i in i2 until i1) {
            if (chipsPositionArray[j][i] != HasChip.EMPTY && chipsPositionArray[j][i] != whichTurn) {
                chipsPositionArray[j][i] = HasChip.EMPTY
            }
            j = j + inc
        }


        //Check if any chip is eaten
        if(isAnyChipEaten){
            //Find next "eat" step
            if(! checkEatAllDir(j2,i2,Direction.NONE,allowedMovesCurrent)){
                changeTurn()
                isAnyChipEaten=false
            }
        }
        else{
            //change turn if not eaten
            changeTurn()
            isAnyChipEaten=false
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
        j: Int,
        i: Int,
        allowedMoves: Array<Array<Boolean>>
    ) {
        //direction of chip move
        var directionInc = 0
        directionInc = if (whichTurn == HasChip.LIGHT) -1 else 1

        //check move
        if (checkBoardBonds(j + directionInc, i)) {
            //Check left
            checkDirectionMove(j, i, directionInc, 1, allowedMoves)
            //Check right
            checkDirectionMove(j, i, directionInc, -1, allowedMoves)
        }
        //check eat
        checkEatAllDir(j, i,Direction.NONE, allowedMoves)
    }

    private fun checkEatAllDir(
        j: Int,
        i: Int,
        ignoreDirection:Direction,
        allowedMoves: Array<Array<Boolean>>
    ) {
        if(ignoreDirection!=Direction.DR){checkEat(j, i,  1,  1, Direction.UL, allowedMoves)}
        if(ignoreDirection!=Direction.UL){checkEat(j, i, -1, -1, Direction.DR,allowedMoves)}
        if(ignoreDirection!=Direction.UR){checkEat(j, i, -1,  1, Direction.DL,allowedMoves)}
        if(ignoreDirection!=Direction.DL){checkEat(j, i,  1, -1, Direction.UR,allowedMoves)}

    }

    private fun checkBoardBonds(j: Int, i: Int): Boolean {
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
    private fun checkDirectionMove(
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
        }
    }

    private fun checkEat(
        j: Int,
        i: Int,
        dirJInc: Int,
        dirIInc: Int,
        direction:Direction,
        allowedMoves: Array<Array<Boolean>>
    ): Boolean {
        //If next chip color is different
        if (checkBoardBonds(j + 2 * dirJInc, i + 2 * dirIInc)) {
            //check eat
            val oppositeChip = if (whichTurn == HasChip.LIGHT) {
                HasChip.DARK
            } else {
                HasChip.LIGHT
            }
            if (checkPosition(j + 1 * dirJInc, i + 1 * dirIInc, oppositeChip)) {
                if (checkPosition(j + 2 * dirJInc, i + 2 * dirIInc, HasChip.EMPTY)) {
                    allowedMoves[j + 2 * dirJInc][i + 2 * dirIInc] = true
                            checkEatAllDir(j + 2 * dirJInc, i + 2 * dirIInc,direction, allowedMoves)
                    return true
                }
            }
        }
        return false
    }

    //Calculate All possible moves
    fun calculateAllowedMovesForAll(): Array<Array<Boolean>> {
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
                    calculateAllowedMoves(j, i, allowedMovesAll)
                }
            }
        }
        return allowedMovesAll
    }
}