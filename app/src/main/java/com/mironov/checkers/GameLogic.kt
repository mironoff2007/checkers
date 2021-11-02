package com.mironov.checkers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel


class GameLogic : ViewModel() {

    fun doTheThing() {
       val i=0
    }


    private var chipsPositionArray = arrayOf<Array<HasChip>>()
    private var allowedMoves = arrayOf<Array<Boolean>>()

    init {
        var array = arrayOf<HasChip>()
        var arrayMoves = arrayOf<Boolean>()
        for(j in 0..7){
            for(i in 0..7){
                array+=HasChip.EMPTY
                arrayMoves+=false
            }
            chipsPositionArray+=array
            allowedMoves+=arrayMoves
            array=arrayOf<HasChip>()
            arrayMoves=arrayOf<Boolean>()
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

    fun calculateAllowedMoves(whichChipTurn:HasChip){
        //direction of chip move
        var directionInc =0
        directionInc = if(whichChipTurn==HasChip.LIGHT) -1  else  1
    //Find chips for move
        for(j in 0..7){
            for(i in 0..7){
                if(chipsPositionArray[j][i]==whichChipTurn){
                //Check possible moves if tile is empty
                    if(j+directionInc>0){
                        //Check left
                        if(i-1>0){
                            if( chipsPositionArray[j+directionInc][i-1]==HasChip.EMPTY){
                               allowedMoves[j+directionInc][i-1]=true
                            }
                        }
                        //Check right
                        if(i+1<7){
                            if( chipsPositionArray[j+directionInc][i+1]==HasChip.EMPTY){
                                allowedMoves[j+directionInc][i+1]=true
                            }
                        }

                    }

                }
            }
        }
    }
}