package com.mironov.checkers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel


class GameLogic : ViewModel() {

    fun doTheThing() {
       val i=0
    }


    private var chipsPositionArray = arrayOf<Array<HasChip>>()

    init {
        var array = arrayOf<HasChip>()
        for(j in 0..7){
            for(i in 0..7){ array+=HasChip.EMPTY }
            chipsPositionArray+=array
            array=arrayOf<HasChip>()
        }
    }

    fun moveIsAllowed(i:Int,j:Int,chipColor:HasChip):Boolean{
        if (chipsPositionArray[j][i]==HasChip.EMPTY){
            return true
        }
        return false
    }

    fun updatePosition(i1:Int,j1:Int,i2:Int,j2:Int,chipColor:HasChip){
        //Remove chip from old position
        chipsPositionArray[j1][i1] = HasChip.EMPTY
        //Put chip to new position
        chipsPositionArray[j2][i2] = chipColor
    }

    fun setShipAtPos(i:Int,j:Int,chipColor:HasChip){
        chipsPositionArray[j][i]=chipColor
    }
}