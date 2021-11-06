package com.mironov.checkers

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginLeft
import androidx.lifecycle.ViewModelProvider
import android.os.SystemClock

class MainActivity : AppCompatActivity() {

    private lateinit var gameLogic: GameLogic
    private var tileSize = 0
    private lateinit var gameBoard: FlowLayout
    private lateinit var gameArea: FrameLayout

    private lateinit var outBoardTop: LinearLayout
    private lateinit var outBoardBot: LinearLayout

    private var selectedChip: View? = null

    private var chipIsSelected = false

    private var tilesArray = arrayOf<Array<View>>()
    private var chipsArray = arrayOf<Array<View?>>()

    private var screenWidth = 0

    private  var multipleEat=false

    private fun findViews() {
        gameBoard = findViewById(R.id.flowLayout)
        gameArea = findViewById(R.id.gameArea)
        outBoardTop = findViewById(R.id.outBoardTop)
        outBoardBot = findViewById(R.id.outBoardBot)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset_game_action -> {
                val intent = intent
                finish()
                startActivity(intent)
                true
            }
            R.id.make_crown -> {
                if (selectedChip != null) {
                    selectedChip!!.findViewById<ImageView>(R.id.crown)
                        .setImageDrawable(resources.getDrawable(R.drawable.ic_crown))
                }
                true
            }
            R.id.make_common -> {
                if (selectedChip != null) {
                    selectedChip!!.findViewById<ImageView>(R.id.crown).setImageResource(0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameLogic = ViewModelProvider(this).get(GameLogic::class.java)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        findViews()
        addLayouts()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addChips() {
        for (n in 0..7) {
            val array = arrayOfNulls<View?>(8)
            chipsArray += array
        }

        //custom positions
        if(true){

            var arr = arrayOf(
                arrayOf(ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.DARK,  ChipType.EMPTY,ChipType.DARK,  ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.EMPTY, ChipType.LIGHT_CROWN,ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.DARK,  ChipType.EMPTY,ChipType.EMPTY,  ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY, ChipType.DARK,ChipType.EMPTY, ChipType.EMPTY),
                arrayOf(ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY, ChipType.EMPTY,ChipType.EMPTY, ChipType.EMPTY))

            //Draw chips on UI
            for (j in 0..7) {
                for (i in 0..7) {
                    val chipType = arr[j][i]
                    if(chipType!=ChipType.EMPTY){
                        initChip(chipType, tilesArray[j][i])
                        gameLogic.setChipAtPos(i, j, chipType)
                        selectedChip!!.tag = "$i,$j," + chipType
                        chipsArray[j][i] = selectedChip}
                }
            }
        }
        else{
            //init black chips
            for (j in 0..2 step 1) {
                var firstTile = 0
                if (j % 2 == 0) {
                    firstTile = 1
                }
                for (i in firstTile..7 step 2) {
                    initChip(ChipType.DARK, tilesArray[j][i])
                    gameLogic.setChipAtPos(i, j, ChipType.DARK)
                    selectedChip!!.tag = "$i,$j," + ChipType.DARK
                    chipsArray[j][i] = selectedChip
                }
            }

            //init light chips
            for (j in 5..7 step 1) {
                var firstTile = 0
                if (j % 2 == 0) {
                    firstTile = 1
                }
                for (i in firstTile..7 step 2) {
                    initChip(ChipType.LIGHT, tilesArray[j][i])
                    gameLogic.setChipAtPos(i, j, ChipType.LIGHT)
                    selectedChip!!.tag = "$i,$j," + ChipType.LIGHT
                    chipsArray[j][i] = selectedChip
                }
            }

            drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())
        }
    }

    private fun addLayouts() {
        gameBoard.removeAllViews()

        //Draw chips after board draw
        gameArea.doOnPreDraw { addChips() }

        //Line and column indexes
        var i = -1
        var j = 0
        var array = arrayOf<View>()
        //Init tiles
        for (tileId in 0..63) {

            val tile: View = this.layoutInflater.inflate(R.layout.tile, null)
            val tileImage = tile.findViewById<View>(R.id.tileImage) as ImageView

            //Calculate tile size
            tileSize = (screenWidth - 2 * gameBoard.marginLeft) / 8
            tileImage.layoutParams.height = tileSize
            tileImage.layoutParams.width = tileSize

            //Set spacing here
            tile.layoutParams = FlowLayout.LayoutParams(1, 1)

            //Calc indexes, move to next line
            i++
            if (tileId % 8 == 0 && tileId != 0) {
                j++
                tilesArray += array
                array = arrayOf<View>()
                array += tile
                i = 0
            } else {
                array += tile
            }

            //Add tag to tile
            tile.tag = "$i,$j,$tileId"

            //Color tile
            when {
                (tileId + j) % 2 != 0 -> tileImage.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_dark))
                (tileId + j) % 2 == 0 -> tileImage.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_light))
            }

            gameBoard.addView(tile)

            //On chip put to some tile
            tile.setOnClickListener {
                //Get tile coordinates
                val coordinates = getCoordinates(tile)

                //Move Chip to tile
                if (selectedChip != null) {

                    //Get chip position index and Color
                    val chipData = selectedChip!!.tag.toString().split(',')
                    val i1 = chipData[0].toInt()
                    val j1 = chipData[1].toInt()
                    val chipColor = ChipType.valueOf(chipData[2])

                    //Get tile index position
                    val tileIndexes = tile.tag.toString().split(',')
                    val i2 = tileIndexes[0].toInt()
                    val j2 = tileIndexes[1].toInt()

                    //Check if move to tile is allowed
                    if (gameLogic.moveIsAllowed(i1, j1, i2, j2)) {

                        //Update logic
                        multipleEat=gameLogic.updatePosition(i1, j1, i2, j2, chipColor)
                        chipIsSelected=false

                        //UI clear chips
                        for (j in 0..7) {
                            for (i in 0..7) {
                                val chip = chipsArray[j][i]
                                gameArea.removeView(chip)
                                chip?.setOnTouchListener(null)
                                chipsArray[j][i] = null
                            }
                        }

                        //Update chips on UI
                        for (j in 0..7) {
                            for (i in 0..7) {
                                val chipType = gameLogic.chipsPositionArray[j][i]
                                if(chipType!=ChipType.EMPTY){
                                initChip(chipType, tilesArray[j][i])
                                    selectedChip!!.tag = "$i,$j," + chipType
                                    chipsArray[j][i] = selectedChip}
                            }
                        }

                        //Draw all moves
                        drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())

                        if(multipleEat){
                            touchView(chipsArray[j2][i2]!!)
                        }

                    } else {
                        Toast.makeText(this, "Нельзя так ходить", Toast.LENGTH_LONG)
                    }
                }
                Log.d("My_tag", "tile Number=" + tile.tag)
            }
        }
        //Add tiles line to array of tiles
        tilesArray += array
    }

    private fun getCoordinates(view: View): Array<Float> {
        val offsetViewBounds = Rect()
        //returns the visible bounds
        view.getDrawingRect(offsetViewBounds)
        // calculates the relative coordinates to the parent
        gameArea.offsetDescendantRectToMyCoords(view, offsetViewBounds)

        val relativeTop: Float = offsetViewBounds.top.toFloat()
        val relativeLeft: Float = offsetViewBounds.left.toFloat()

        return arrayOf(relativeLeft, relativeTop)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initChip(chipType: ChipType, tile: View) {
        var layout=0
        if (chipType == ChipType.DARK||chipType == ChipType.DARK_CROWN) {
           layout=R.layout.dark_chip
        } else {
            layout=R.layout.light_chip
        }

        val chip = this.layoutInflater.inflate(layout, null)

        if(chipType == ChipType.DARK_CROWN||chipType == ChipType.LIGHT_CROWN){chip.findViewById<ImageView>(R.id.crown)
            .setImageDrawable(resources.getDrawable(R.drawable.ic_crown))}

        gameArea.addView(chip, tileSize, tileSize)

        //Set size of chip
        val chipImage = chip.findViewById<ImageView>(R.id.chip)
        chipImage.layoutParams.height = tileSize
        chipImage.layoutParams.width = tileSize

        //Init chip at position of tile
        val coordinates = getCoordinates(tile)
        chip.translationX = coordinates[0]
        chip.translationY = coordinates[1]

        selectedChip = chip

        //On chip pick
        chip.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val a = v.alpha

                    if (a < 1&&!multipleEat) {
                        //put chip
                        selectedChip = null
                        v.alpha = 1f
                        chipIsSelected = false
                        //Draw all moves
                        drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())
                    } else {
                        //pick chip
                        if (!chipIsSelected) {
                            selectedChip = v
                            //Get chip position index and Color
                            val chipData = selectedChip!!.tag.toString().split(',')
                            val i = chipData[0].toInt()
                            val j = chipData[1].toInt()
                            val chipColor = ChipType.valueOf(chipData[2]).toString().split("_")[0]

                            //Check with color is turn
                            if (gameLogic.whichTurn.toString() == chipColor) {
                                //pick chip
                                v.alpha = 0.5f
                                chipIsSelected = true

                                //Get and Draw allowed moves
                                drawPossibleMoves(gameLogic.getAllowedMoves(j, i))
                            } else {
                                //unpick
                                selectedChip = null
                            }
                        }
                    }
                }
            }
            true
        }
    }

    fun touchView(view: View) {
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN ,
                0F,
                0F,
                0
            )
        )
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP ,
                0F,
                0F,
                0
            )
        )
    }
    private fun clearAllowedTiles() {
        for (j in 0..7) {
            for (i in 0..7) {
                //Update Tiles
                val tileAllowedImage =
                    tilesArray[j][i].findViewById<View>(R.id.tileIsAllowed) as ImageView
                tileAllowedImage.setImageResource(0)
            }
        }
    }

    private fun drawPossibleMoves(allowedMoves: Array<Array<Boolean>>) {
        clearAllowedTiles()
        for (j in 0..7) {
            for (i in 0..7) {
                //Update Tiles
                val tileAllowedImage =
                    tilesArray[j][i].findViewById<View>(R.id.tileIsAllowed) as ImageView
                if (allowedMoves[j][i]) {
                    tileAllowedImage.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_allowed_move
                        )
                    )
                }
            }
        }
    }
}
