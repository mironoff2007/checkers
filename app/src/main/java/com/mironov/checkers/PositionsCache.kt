package com.mironov.checkers

import com.mironov.checkers.model.ChipType
import com.mironov.checkers.model.Position

class PositionsCache {
    var id = -1
    var idMax=0
    private var positionsArray = arrayOf<Array<Array<ChipType>>>()
    private var whoMovesArray = arrayOf<ChipType>()

    private fun Array<Array<ChipType>>.copy() = Array(size) { get(it).clone() }

    fun addPosition(position: Array<Array<ChipType>>, turn: ChipType) {
        id++
        if(id>=whoMovesArray.size){
        positionsArray += position.copy()
        whoMovesArray += turn
            idMax=id
        }
        else{
            positionsArray [id]= position.clone()
            whoMovesArray [id]= turn
            idMax=id
        }
    }

    fun prevPosition(): Position? {
        val position = Position()
        return if (id -1>=0 ) {
            id--
            position.whichTurn = whoMovesArray[id]
            position.positionArray = positionsArray[id].copy()
            position
        } else {
            position.whichTurn = whoMovesArray[0]
            position.positionArray = positionsArray[0].copy()
            position
        }
    }

    fun nextPosition(): Position? {
        val position = Position()
        return if (id < idMax) {
            id++
            position.whichTurn = whoMovesArray[id]
            position.positionArray = positionsArray[id].copy()
            position
        } else {
            position.whichTurn = whoMovesArray[id]
            position.positionArray = positionsArray[id].copy()
            position
        }
    }
}