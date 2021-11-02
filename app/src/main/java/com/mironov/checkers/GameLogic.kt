package com.mironov.checkers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel


class GameLogic : ViewModel() {

    fun doTheThing() {
       val i=0
    }


    var chipsPositionArray = arrayOf<Array<HasChip>>()

    init {
        var array = arrayOf<HasChip>()
        for(j in 0..7){
            for(i in 0..7){ array+=HasChip.EMPTY }
            chipsPositionArray+=array
            array=arrayOf<HasChip>()
        }
    }
}