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


class MainActivity : AppCompatActivity() {

    private lateinit var gameLogic: GameLogic
    private var tileSize = 0
    private lateinit var gameBoard: FlowLayout
    private lateinit var gameArea: FrameLayout

    private lateinit var outBoardTop: LinearLayout
    private lateinit var outBoardBot: LinearLayout

    private lateinit var darkChip: View
    private var selectedChip: View? = null

    private var chipIsSelected = false

    private var tilesArray = arrayOf<Array<View>>()

    private var screenWidth = 0

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
            R.id.make_crown-> {
                if(selectedChip!=null){
                    selectedChip!!.findViewById<ImageView>(R.id.crown).setImageDrawable(resources.getDrawable(R.drawable.ic_crown))
                }
                true
            }
            R.id.make_common-> {
                if(selectedChip!=null){
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
        initListeners()
    }


    private fun initListeners() {
        outBoardTop.setOnClickListener {
            if (selectedChip != null) {
                selectedChip!!.visibility = View.GONE;chipIsSelected= false
            }
        }
        outBoardBot.setOnClickListener {
            if (selectedChip != null) {
                selectedChip!!.visibility = View.GONE;chipIsSelected = false
            }
            gameLogic.doTheThing()
        }
    }

    override fun onResume() {
        super.onResume()
    }


    @SuppressLint("ClickableViewAccessibility")
    fun addChips() {
        //init black chips
        for (j in 0..2 step 1) {
            var firstTile = 0
            if (j % 2 == 0) {
                firstTile = 1
            }
            for (i in firstTile..7 step 2) {
                initChip(R.layout.dark_chip, tilesArray[j][i])
                gameLogic.chipsPositionArray[j][i]=HasChip.DARK
                selectedChip!!.tag="$i,$j,"+HasChip.DARK
            }
        }

        //init light chips
        for (j in 5..7 step 1) {
            var firstTile = 0
            if (j % 2 == 0) {
                firstTile = 1
            }
            for (i in firstTile..7 step 2) {
                initChip(R.layout.light_chip, tilesArray[j][i])
                gameLogic.chipsPositionArray[j][i]=HasChip.LIGHT
                selectedChip!!.tag="$i,$j,"+HasChip.LIGHT
            }
        }
    }

    private fun addLayouts() {

        gameBoard.removeAllViews()
        gameArea.doOnPreDraw { addChips() }

        var j = 0

        var array = arrayOf<View>()
        var i=-1
        for (tileId in 0..63) {

            val view: View = this.layoutInflater.inflate(R.layout.tile, null)
            val imageView = view.findViewById<View>(R.id.tileImage) as ImageView


            tileSize = (screenWidth - 2 * gameBoard.marginLeft) / 8
            imageView.layoutParams.height = tileSize
            imageView.layoutParams.width = tileSize

            //Set spacing here
            view.layoutParams = FlowLayout.LayoutParams(1, 1)



            i++
            if (tileId % 8 == 0 && tileId != 0) {
                j++
                tilesArray += array
                array = arrayOf<View>()
                array += view
                i=0
            } else {
                array += view
            }

            view.tag = "$i,$j,$tileId"

            when {
                (tileId + j) % 2 != 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_dark))
                (tileId + j) % 2 == 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_light))
            }

            gameBoard.addView(view)

            view.setOnClickListener {
                val coordinates = getCoordinates(view)

                //Move Chip to tile
                if (selectedChip != null) {

                    //Get chip position index and Color
                    val chipData= selectedChip!!.tag.toString().split(',')
                    val i1=chipData[0].toInt()
                    val j1=chipData[1].toInt()
                    val chipColor=HasChip.valueOf(chipData[2])

                    //Get tile index position
                    val tileIndexes=view.tag.toString().split(',')
                    val i2=tileIndexes[0].toInt()
                    val j2=tileIndexes[1].toInt()


                    if(gameLogic.moveIsAllowed(i2,j2,chipColor)) {
                        //Remove chip from old position
                        gameLogic.chipsPositionArray[j1][i1] = HasChip.EMPTY
                        //Put chip to new position
                        gameLogic.chipsPositionArray[j2][i2] = chipColor
                        //Move chip on UI
                        selectedChip!!.translationX =
                            coordinates[0] + view.width / 2 - selectedChip!!.width / 2
                        selectedChip!!.translationY =
                            coordinates[1] + view.height / 2 - selectedChip!!.height / 2

                        //Update chip pos tag
                        selectedChip!!.tag="$i2,$j2,$chipColor"

                        //Deselect chip
                        selectedChip!!.alpha = 1f
                        selectedChip = null
                        chipIsSelected = false
                    }
                    else{
                        Toast.makeText(this,"Нельзя так ходить",Toast.LENGTH_LONG)
                    }
                }
                Log.d("My_tag", "tile Number=" + view.tag)
            }
        }
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
    fun initChip(layout: Int, tile: View) {
        val chip = this.layoutInflater.inflate(layout, null)

        gameArea.addView(chip, tileSize, tileSize)

        val imageView=chip.findViewById<ImageView>(R.id.chip)
        imageView.layoutParams.height = tileSize
        imageView.layoutParams.width = tileSize

        //Init chip at position of tile
        val coordinates = getCoordinates(tile)
        chip.translationX = coordinates[0]
        chip.translationY = coordinates[1]

        selectedChip=chip
        chip.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val a = v.alpha

                    if (a < 1) {
                        //put chip
                        selectedChip = null
                        v.alpha = 1f
                        chipIsSelected = false
                    } else {
                        //pick chip
                        if (!chipIsSelected) {
                            v.alpha = 0.5f
                            chipIsSelected = true
                            selectedChip = v
                        }
                    }
                }
            }
            true
        }
    }
}
