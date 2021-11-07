package com.mironov.checkers

import androidx.lifecycle.ViewModel
import com.mironov.checkers.model.ChipType
import com.mironov.checkers.model.Direction


class GameLogic : ViewModel() {

    var whichTurn: ChipType = ChipType.LIGHT
    var whoMoves: ChipType = ChipType.LIGHT

    var chipsPositionArray = arrayOf<Array<ChipType>>()

    private var allowedMovesAll = arrayOf<Array<Boolean>>()

    private var allowedMovesCurrent = arrayOf<Array<Boolean>>()

    private var dummy = arrayOf<Array<Boolean>>()

    private val positionCache=PositionsCache()

    var lastMoveDirection: Direction = Direction.NONE

    var isAnyChipEaten = false
    var canEat = false
    var multipleEat = false
    var restrictMove = false
    var complexCrownEat = false

    init {
        var array = arrayOf<ChipType>()
        var arrayMoves = arrayOf<Boolean>()
        var arrayMovesAll = arrayOf<Boolean>()
        var arrayDummy = arrayOf<Boolean>()
        for (j in 0..7) {
            for (i in 0..7) {
                array += ChipType.EMPTY
                arrayMoves += false
                arrayMovesAll += false
                arrayDummy += false
            }
            chipsPositionArray += array
            allowedMovesAll += arrayMovesAll
            allowedMovesCurrent += arrayMoves
            dummy += arrayDummy
            //clear arrays
            array = arrayOf<ChipType>()
            arrayMoves = arrayOf<Boolean>()
            arrayMovesAll = arrayOf<Boolean>()
            arrayDummy = arrayOf<Boolean>()
        }
    }

    fun savePosition(){
        positionCache.addPosition(chipsPositionArray,whichTurn)
    }

    fun prevPosition(){
       val position = positionCache.prevPosition()
        whichTurn=position!!.whichTurn
        chipsPositionArray=position.positionArray.clone()
    }

    fun nextPosition(){
        val position = positionCache.nextPosition()
        whichTurn=position!!.whichTurn
        chipsPositionArray=position.positionArray.clone()
    }

    /** Returns tile id increment to step in this direction
     * @param direction Direction of move
     */
    private fun getDirectionIncrements(direction: Direction): Array<Int> {
        return when (direction) {
            Direction.DL -> arrayOf(1, -1)
            Direction.DR -> arrayOf(1, 1)
            Direction.UR -> arrayOf(-1, 1)
            Direction.UL -> arrayOf(-1, -1)
            Direction.NONE -> arrayOf(0, 0)
        }
    }

    /** Is move from i1,j1 to i2,j2 is allowed
     * @param i1 picked tile index i
     * @param j1 picked tile index j
     * @param i2 index i of tile to put chip
     * @param j2 index j of tile to put chip
     */
    fun moveIsAllowed(i1: Int, j1: Int, i2: Int, j2: Int): Boolean {
        multipleEat = false
        whoMoves = chipsPositionArray[j1][j2]
        calculateAllowedMoves(j1, i1, allowedMovesCurrent)
        if (chipsPositionArray[j2][i2] == ChipType.EMPTY) {
            return allowedMovesCurrent[j2][i2]
        }
        return false
    }

    /** Update position matrix
     * @param i1 picked tile index i
     * @param j1 picked tile index j
     * @param i2 index i of tile to put chip
     * @param j2 index j of tile to put chip
     */
    fun updatePosition(i1: Int, j1: Int, i2: Int, j2: Int, chipColor: ChipType): Boolean {
        //Remove chip from old position
        chipsPositionArray[j1][i1] = ChipType.EMPTY

        //Get direction
        lastMoveDirection = getDirection(j1, i1, j2, i2)

        //Make crown
        if (chipColor == ChipType.LIGHT && j2 == 0) {
            //Put chip to new position
            chipsPositionArray[j2][i2] = ChipType.LIGHT_CROWN
        } else if (chipColor == ChipType.DARK && j2 == 7) {
            //Put chip to new position
            chipsPositionArray[j2][i2] = ChipType.DARK_CROWN
        } else {
            //Put chip to new position
            chipsPositionArray[j2][i2] = chipColor
        }

        //Find chip to eat
        //Direction to move by j Index
        var incJ: Int = if (j2 - j1 > 0) {
            1
        } else {
            -1
        }

        var incI: Int = if (i2 - i1 > 0) {
            1
        } else {
            -1
        }

        var i = i1
        var j = j1
        while ((i != i2 - incI) && checkBoardBonds(j, i)) {
            i += incI
            j += incJ
            if (chipsPositionArray[j][i] != ChipType.EMPTY && chipsPositionArray[j][i] != whichTurn) {
                chipsPositionArray[j][i] = ChipType.EMPTY
                isAnyChipEaten = true
            }
        }

        //Check if any chip is eaten
        if (isAnyChipEaten) {
            //Find next "eat" step
            if (whoMoves == ChipType.LIGHT_CROWN || whoMoves == ChipType.DARK_CROWN) {
                canEat = false

                //Restrict backward eat after eat
                val ignoreDirection = oppositeDirection(lastMoveDirection)
                when {
                    ignoreDirection != Direction.UL -> {
                        //Check UP and LEFT
                        checkDirectionMove(j2, i2, Direction.UL, allowedMovesCurrent)
                    }
                    ignoreDirection != Direction.UR -> {
                        //Check UP and RIGHT
                        checkDirectionMove(j2, i2, Direction.UR, allowedMovesCurrent)
                    }
                    ignoreDirection != Direction.DR -> {
                        //Check DOWN and RIGHT
                        checkDirectionMove(j2, i2, Direction.DR, allowedMovesCurrent)
                    }
                    ignoreDirection != Direction.DL -> {
                        //Check DOWN and LEFT
                        checkDirectionMove(j2, i2, Direction.DL, allowedMovesCurrent)
                    }
                }

                if (canEat) {
                    restrictMove = true
                    return true
                }
            }

            if (!checkEatAllDir(j2, i2, Direction.NONE, allowedMovesCurrent)) {
                changeTurn()
                restrictMove = false
                isAnyChipEaten = false
                return false
            } else {
                restrictMove = true
                return true
            }
        } else {
            //change turn if not eaten
            changeTurn()
            restrictMove = false
            isAnyChipEaten = false
            return false
        }
    }


    private fun getDirection(j1: Int, i1: Int, j2: Int, i2: Int): Direction {
        if (j2 - j1 > 0) {
            //DOWN
            return if (i2 - i1 > 0) {
                //RIGHT
                Direction.DR
            } else {
                //LEFT
                Direction.DL
            }
        } else {
            //UP
            return if (i2 - i1 > 0) {
                //RIGHT
                Direction.UR
            } else {
                //LEFT
                Direction.UL
            }
        }

    }

    private fun changeTurn() {
        //Change who moves`
        whichTurn = if (whichTurn == ChipType.LIGHT) {
            ChipType.DARK
        } else {
            ChipType.LIGHT
        }
    }

    fun setChipAtPos(i: Int, j: Int, chipColor: ChipType) {
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
        multipleEat = false
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
        whoMoves = chipsPositionArray[j][i]
        //direction of chip move

        //If multiple eat, restrict not eat
        if (restrictMove) {
            //Check eat from current position
            checkEatAllDir(j, i, Direction.NONE, allowedMoves)

            if (whoMoves == ChipType.LIGHT_CROWN || whoMoves == ChipType.DARK_CROWN) {
                //Ignore direction last Move Direction abd find to eat
                complexCrownEat = true
                complexCrownEatFind(j, i, lastMoveDirection, allowedMoves)
                complexCrownEat = false
            }

        } else {
            //If crown, check all directions
            if (whoMoves == ChipType.LIGHT_CROWN || whoMoves == ChipType.DARK_CROWN) {
                //Check eat from current position
                checkEatAllDir(j, i, Direction.NONE, allowedMoves)

                //Check UP and LEFT
                checkDirectionMove(j, i, Direction.UL, allowedMoves)
                //Check UP and RIGHT
                checkDirectionMove(j, i, Direction.UR, allowedMoves)
                //Check DOWN and RIGHT
                checkDirectionMove(j, i, Direction.DR, allowedMoves)
                //Check DOWN and LEFT
                checkDirectionMove(j, i, Direction.DL, allowedMoves)
            }
            //check move for common chips
            else {
                //Check eat from current position
                checkEatAllDir(j, i, Direction.NONE, allowedMoves)

                if (whichTurn == ChipType.LIGHT) {
                    //Check UP and LEFT
                    checkDirectionMove(j, i, Direction.UL, allowedMoves)
                    //Check UP and RIGHT
                    checkDirectionMove(j, i, Direction.UR, allowedMoves)
                } else {
                    //Dark chips
                    //Check DOWN and RIGHT
                    checkDirectionMove(j, i, Direction.DR, allowedMoves)
                    //Check DOWN and LEFT
                    checkDirectionMove(j, i, Direction.DL, allowedMoves)
                }
            }
        }
    }

    private fun complexCrownEatFind(
        j: Int,
        i: Int,
        lastMoveDirection: Direction,
        allowedMoves: Array<Array<Boolean>>
    ) {
        val dirIncArr = getDirectionIncrements(lastMoveDirection)
        val stepJ = dirIncArr[0]
        val stepI = dirIncArr[1]

        var n = 0
        var k = 0
        //Check eat from current position
        checkEatAllDir(j, i, Direction.NONE, dummy)

        //Check UP and LEFT
        checkDirectionMove(j, i, Direction.UL, dummy)
        //Check UP and RIGHT
        checkDirectionMove(j, i, Direction.UR, dummy)
        //Check DOWN and RIGHT
        checkDirectionMove(j, i, Direction.DR, dummy)
        //Check DOWN and LEFT
        checkDirectionMove(j, i, Direction.DL, dummy)
        while (checkBoardBonds(j + n, i + k) && checkPosition(j + k, i + n, ChipType.EMPTY)) {
            //Check eat from current position
            checkEatAllDir(j, i, Direction.NONE, dummy)

            //Check UP and LEFT
            checkDirectionMove(j, i, Direction.UL, dummy)
            //Check UP and RIGHT
            checkDirectionMove(j, i, Direction.UR, dummy)
            //Check DOWN and RIGHT
            checkDirectionMove(j, i, Direction.DR, dummy)
            //Check DOWN and LEFT
            checkDirectionMove(j, i, Direction.DL, dummy)
            n += stepJ
            k += stepI
        }
    }

    private fun oppositeDirection(direction: Direction): Direction {
        return when (direction) {
            Direction.DR -> {
                Direction.UL
            }
            Direction.DL -> {
                Direction.UR
            }
            Direction.UR -> {
                Direction.DL
            }
            Direction.UL -> {
                Direction.DR
            }
            else -> {
                Direction.NONE
            }
        }
    }

    /**Check possible moves to eat opponent chip
     * @param j chip index j
     * @param i chip index i
     * @param ignoreDirection Ignore this direction on next eat step
     * @param allowedMoves Matrix of allowed moves to update
     */
    private fun checkEatAllDir(
        j: Int,
        i: Int,
        ignoreDirection: Direction,
        allowedMoves: Array<Array<Boolean>>
    ): Boolean {
        canEat = false
        //UP and LEFT
        if (ignoreDirection != Direction.DR) {
            checkEat(j, i, Direction.UL, allowedMoves)
        }
        //DOWN and RIGHT
        if (ignoreDirection != Direction.UL) {
            checkEat(j, i, Direction.DR, allowedMoves)
        }
        //DOWN and LEFT
        if (ignoreDirection != Direction.UR) {
            checkEat(j, i, Direction.DL, allowedMoves)
        }
        //UP and Right
        if (ignoreDirection != Direction.DL) {
            checkEat(j, i, Direction.UR, allowedMoves)
        }
        return canEat
    }

    private fun checkBoardBonds(j: Int, i: Int): Boolean {
        if (i in 0..7 && j in 0..7) {
            return true
        }
        return false
    }

    private fun checkPosition(j: Int, i: Int, type: ChipType): Boolean {
        if (chipsPositionArray[j][i].toString().split('_')[0] == type.toString()) {
            return true
        }
        return false
    }

    /**Check if tile is empty to move to it
     * @param j chip index j
     * @param i chip index i
     * @param allowedMoves Matrix of allowed moves to update
     */
    private fun checkDirectionMove(
        j: Int,
        i: Int,
        direction: Direction,
        allowedMoves: Array<Array<Boolean>>
    ) {
        val dirIncArr = getDirectionIncrements(direction)
        val stepJ = dirIncArr[0]
        val stepI = dirIncArr[1]

        if (whoMoves == ChipType.DARK_CROWN || whoMoves == ChipType.LIGHT_CROWN) {
            //Crown chips
            var n = stepJ
            var k = stepI
            while (checkBoardBonds(j + n, i + k) && checkPosition(j + n, i + k, ChipType.EMPTY)) {
                allowedMoves[j + n][i + k] = true
                n += stepJ
                k += stepI
            }
            checkEat(j + n - stepJ, i + k - stepI, direction, allowedMoves)

        } else {
            //Common chips
            if (checkBoardBonds(j + stepJ, i + stepI)) {
                //if empty
                if (checkPosition(j + stepJ, i + stepI, ChipType.EMPTY)) {
                    allowedMoves[j + stepJ][i + stepI] = true
                }
            }
        }
    }

    /**Check chips near to eat in spec direction
     * @param j chip index j
     * @param i chip index i
     * @param checkDirection Ignore this direction on next eat step
     * @param allowedMoves Matrix of allowed moves to update
     */
    private fun checkEat(
        j: Int,
        i: Int,
        checkDirection: Direction,
        allowedMoves: Array<Array<Boolean>>
    ) {
        val dirIncArr = getDirectionIncrements(checkDirection)
        val stepJ = dirIncArr[0]
        val stepI = dirIncArr[1]
        //If next chip color is different
        if (checkBoardBonds(j + 2 * stepJ, i + 2 * stepI)) {
            //check eat
            val oppositeChip = if (whichTurn == ChipType.LIGHT) {
                ChipType.DARK
            } else {
                ChipType.LIGHT
            }
            if (checkPosition(j + 1 * stepJ, i + 1 * stepI, oppositeChip)) {
                if (checkPosition(j + 2 * stepJ, i + 2 * stepI, ChipType.EMPTY)) {
                    //Can eat
                    allowedMoves[j + 2 * stepJ][i + 2 * stepI] = true
                    //Complex crown eat
                    if (complexCrownEat) {
                        allowedMovesCurrent[j + 2 * stepJ][i + 2 * stepI] = true
                    }
                    //If crown check to move far after eat
                    if (whoMoves == ChipType.DARK_CROWN || whoMoves == ChipType.LIGHT_CROWN) {
                        //check far moves in this direction
                        checkDirectionMove(
                            j + 2 * stepJ,
                            i + 2 * stepI,
                            checkDirection,
                            allowedMoves
                        )
                    }
                    if (multipleEat) {
                        checkEatAllDir(j + 2 * stepJ, i + 2 * stepI, checkDirection, allowedMoves)
                    }
                    canEat = true
                }
            }
        }
    }

    //Calculate All possible moves
    fun calculateAllowedMovesForAll(): Array<Array<Boolean>> {
        multipleEat = true
        //reset allowed moves
        for (j in 0..7) {
            for (i in 0..7) {
                allowedMovesAll[j][i] = false
            }
        }
        //Find chips for move
        for (j in 0..7) {
            for (i in 0..7) {
                if (chipsPositionArray[j][i].toString().split("_")[0] == whichTurn.toString()) {
                    //Check possible moves if tile is empty
                    calculateAllowedMoves(j, i, allowedMovesAll)
                }
            }
        }
        return allowedMovesAll
    }
}