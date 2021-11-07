package com.mironov.checkers.model

class Position {
    var positionArray= arrayOf<Array<ChipType>>()
    var whoMovesArray:ChipType=ChipType.EMPTY

    init {
        var array = arrayOf<ChipType>()

        for (j in 0..7) {
            for (i in 0..7) {
                array += ChipType.EMPTY

            }
            positionArray += array
        }
    }
}