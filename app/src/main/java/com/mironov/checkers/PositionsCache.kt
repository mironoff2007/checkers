package com.mironov.checkers

import com.mironov.checkers.model.ChipType
import com.mironov.checkers.model.Position

class PositionsCache {
    var id = 0
    private var positionsArray = arrayOf<Array<Array<ChipType>>>()
    private var whoMovesArray = arrayOf<ChipType>()

    fun addPosition(position: Array<Array<ChipType>>, turn: ChipType) {
        if(id>=whoMovesArray.size){
        positionsArray += position
        whoMovesArray += turn
        id++}
        else{
            id++
            positionsArray [id]= position
            whoMovesArray += turn
        }
    }

    fun prevPosition(): Position? {
        id--
        val position = Position()
        return if (id < whoMovesArray.size) {
            position.whoMovesArray = whoMovesArray[id]
            position.positionArray = positionsArray[id]
            position
        } else {
            null
        }
    }

    fun nextPosition(): Position? {
        id++
        val position = Position()
        return if (id < whoMovesArray.size) {
            position.whoMovesArray = whoMovesArray[id]
            position.positionArray = positionsArray[id]
            position
        } else {
            null
        }
    }
}