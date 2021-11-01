package com.mironov.checkers

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginLeft


class MainActivity : AppCompatActivity() {

    private var tileSize = 0
    private lateinit var flowLayout: FlowLayout
    private lateinit var frameLayout: FrameLayout
    private lateinit var darkChip: View
    private var selectedChip: View? = null

    private var chipIsSelected = false

    private var tilesArray = arrayOf<Array<View>>()

    private var screenWidth = 0

    private fun findViews() {
        flowLayout = findViewById(R.id.flowLayout)
        frameLayout = findViewById(R.id.board)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        findViews()
        addLayouts()
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
                initChip(R.layout.dark_chip,tilesArray[j][i])
            }
        }

        //init light chips
        for (j in 5..7 step 1) {
            var firstTile = 0
            if (j % 2 == 0) {
                firstTile = 1
            }
            for (i in firstTile..7 step 2) {
                initChip(R.layout.light_chip,tilesArray[j][i])
            }
        }

    }

    private fun addLayouts() {

        flowLayout.removeAllViews()
        frameLayout.doOnPreDraw { addChips() }

        var j = 0

        var array = arrayOf<View>()
        for (i in 0..63) {

            val view: View = this.layoutInflater.inflate(R.layout.tile, null)
            val imageView = view.findViewById<View>(R.id.tileImage) as ImageView

            view.tag = i

            tileSize = (screenWidth - 2 * flowLayout.marginLeft) / 8
            imageView.layoutParams.height = tileSize
            imageView.layoutParams.width = tileSize

            //Set spacing here
            view.layoutParams = FlowLayout.LayoutParams(1, 1)

            if (i % 8 == 0 && i != 0) {
                j++
                tilesArray += array
                array = arrayOf<View>()
                array += view
            } else {
                array += view
            }

            when {
                (i + j) % 2 != 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_dark))
                (i + j) % 2 == 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_light))
            }

            flowLayout.addView(view)

            view.setOnClickListener {
                val coordinates = getCoordinates(view)


                //Move Chip to tile
                if (selectedChip != null) {
                    selectedChip!!.translationX =
                        coordinates[0].toFloat() + view.width / 2 - selectedChip!!.width / 2
                    selectedChip!!.translationY =
                        coordinates[1].toFloat() + view.height / 2 - selectedChip!!.height / 2

                    selectedChip!!.alpha = 1f
                    selectedChip = null
                    chipIsSelected = false

                }
                Log.d("My_tag", "tile Number=" + view.tag)
            }
        }
        tilesArray += array
    }

    fun dpToPixel(dpValue: Int): Int {
        val scale: Float = resources.getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun getCoordinates(view: View): Array<Float> {
        val offsetViewBounds = Rect()
        //returns the visible bounds
        view.getDrawingRect(offsetViewBounds)
        // calculates the relative coordinates to the parent
        frameLayout.offsetDescendantRectToMyCoords(view, offsetViewBounds)

        val relativeTop: Float = offsetViewBounds.top.toFloat()
        val relativeLeft: Float = offsetViewBounds.left.toFloat()

        return arrayOf(relativeLeft, relativeTop)
    }

    fun initChip(layout:Int,view:View){
        val darkChip = this.layoutInflater.inflate(layout, null)

        frameLayout.addView(darkChip, 0, 0)

        darkChip.layoutParams.height = tileSize
        darkChip.layoutParams.width = tileSize

        val coordinates = getCoordinates(view)

        darkChip.translationX = coordinates[0]
        darkChip.translationY = coordinates[1]

        darkChip.setOnTouchListener { v, event ->
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
