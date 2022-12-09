package com.mironov.checkers.ui

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginLeft
import androidx.lifecycle.ViewModelProvider
import com.mironov.checkers.GameLogic
import com.mironov.checkers.R
import com.mironov.checkers.databinding.ActivityMainBinding
import com.mironov.checkers.model.ChipType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var tileSize = 0
    private lateinit var gameLogic: GameLogic
    private lateinit var gameBoard: FlowLayout
    private lateinit var gameArea: FrameLayout

    private lateinit var outBoardTop: LinearLayout
    private lateinit var outBoardBot: LinearLayout

    private lateinit var buttonNext: Button
    private lateinit var buttonPrev: Button

    private var selectedChip: View? = null

    private var chipIsSelected = false

    private var tilesArray = arrayOf<Array<View>>()
    private var chipsArray = arrayOf<Array<View?>>()

    private var screenWidth = 0

    private var multipleEat = false

    private fun findViews() {
        gameBoard = binding.flowLayout
        gameArea = binding.gameArea
        outBoardTop = binding.outBoardTop
        outBoardBot = binding.outBoardBot
        buttonNext = binding.buttonNext
        buttonPrev = binding.buttonPrev
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        gameLogic = ViewModelProvider(this).get(GameLogic::class.java)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        findViews()
        gameBoard.numberViewsInLine = 8
        addLayouts()
        initButtonsListeners()
        buttonNext.isEnabled = false
        buttonPrev.isEnabled = false
    }

    private fun initButtonsListeners() {
        buttonNext.setOnClickListener {
            if (gameLogic.nextPosition()) {
                buttonNext.isEnabled = true
                buttonPrev.isEnabled = true
                chipIsSelected = false
            } else {
                buttonNext.isEnabled = false
            }
            //Draw all moves
            drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())
            updateChips()
        }

        buttonPrev.setOnClickListener {
            if (gameLogic.prevPosition()) {
                buttonNext.isEnabled = true
                buttonPrev.isEnabled = true
                chipIsSelected = false
            } else {
                buttonPrev.isEnabled = false
            }
            //Draw all moves
            drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())
            updateChips()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addChips() {
        for (n in 0..7) {
            val array = arrayOfNulls<View?>(8)
            chipsArray += array
        }

        drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())
        updateChips()
        gameLogic.savePosition()
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
            tile.layoutParams = FlowLayout.LayoutParams(0, 0)

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
                        multipleEat = gameLogic.updatePosition(i1, j1, i2, j2, chipColor)
                        chipIsSelected = false

                        //Save position
                        gameLogic.savePosition()

                        //Update buttons state
                        buttonPrev.isEnabled = true
                        buttonNext.isEnabled = false

                        //Draw all moves
                        drawPossibleMoves(gameLogic.calculateAllowedMovesForAll())

                        //Draw chips
                        updateChips()

                        if (multipleEat) {
                            touchView(chipsArray[j2][i2]!!)
                        }

                    }
                }
            }
        }
        //Add tiles line to array of tiles
        tilesArray += array
    }

    private fun updateChips() {
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
                if (chipType != ChipType.EMPTY) {
                    initChip(chipType, tilesArray[j][i], gameLogic.chipAllowedToMove[j][i])
                    selectedChip!!.tag = "$i,$j," + chipType
                    chipsArray[j][i] = selectedChip
                }
            }
        }
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
    fun initChip(chipType: ChipType, tile: View, allowedToMove: Boolean) {
        var layout = 0
        if (chipType == ChipType.DARK || chipType == ChipType.DARK_CROWN) {
            layout = R.layout.dark_chip
        } else {
            layout = R.layout.light_chip
        }

        val chip = this.layoutInflater.inflate(layout, null)

        //Mark chip which can move
        val allowedCircle = chip.findViewById<ImageView>(R.id.allowedToMove)
        if (allowedToMove) {
            allowedCircle.setImageDrawable(resources.getDrawable(R.drawable.ic_allowed_circle))
        }

        if (chipType == ChipType.DARK_CROWN || chipType == ChipType.LIGHT_CROWN) {
            chip.findViewById<ImageView>(
                R.id.crown
            )
                .setImageDrawable(resources.getDrawable(R.drawable.ic_crown))
        }

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

                    if (a < 1 && !multipleEat) {
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

    private fun touchView(view: View) {
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                0F,
                0F,
                0
            )
        )
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
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
