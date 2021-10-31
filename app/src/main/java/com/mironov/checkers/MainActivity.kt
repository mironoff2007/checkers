package com.mironov.checkers

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.util.DisplayMetrics
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.marginLeft
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.core.view.doOnPreDraw


class MainActivity : AppCompatActivity() {

    private  var tileSize=0
    private lateinit var flowLayout: FlowLayout
    private lateinit var frameLayout: FrameLayout
    private lateinit var darkChip: View
    private var selectedChip: View? = null

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

        for(i in 0..7){
            val darkChip = this.layoutInflater.inflate(R.layout.chip, null)

            frameLayout.addView(darkChip, 0, 0)

            darkChip.layoutParams.height = tileSize
            darkChip.layoutParams.width = tileSize

            val view = tilesArray[0][i]

            val offsetViewBounds = Rect()
            //returns the visible bounds
            view.getDrawingRect(offsetViewBounds)
            // calculates the relative coordinates to the parent
            frameLayout.offsetDescendantRectToMyCoords(view, offsetViewBounds)

            val relativeTop: Int = offsetViewBounds.top
            val relativeLeft: Int = offsetViewBounds.left

            darkChip.translationX = relativeLeft.toFloat()
            darkChip.translationY = relativeTop.toFloat()

            darkChip.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        selectedChip = v
                        v.alpha = 0.5f
                    }
                }
                true
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

            if (i % 8 == 0&&i!=0) {
                j++
                tilesArray+=array
                array = arrayOf<View>()
                array +=view
            }
            else{
                array +=view
            }

            when {
                (i + j) % 2 == 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_dark))
                (i + j) % 2 != 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_light))
            }

            flowLayout.addView(view)

            view.setOnClickListener {
                val offsetViewBounds = Rect()
                //returns the visible bounds
                view.getDrawingRect(offsetViewBounds)
                // calculates the relative coordinates to the parent
                frameLayout.offsetDescendantRectToMyCoords(view, offsetViewBounds)

                val relativeTop: Int = offsetViewBounds.top
                val relativeLeft: Int = offsetViewBounds.left

                if(selectedChip!=null) {
                    selectedChip!!.translationX =
                        relativeLeft.toFloat() + view.width / 2 - selectedChip!!.width / 2
                    selectedChip!!.translationY =
                        relativeTop.toFloat() + view.height / 2 - selectedChip!!.height / 2

                    selectedChip!!.alpha = 1f
                    selectedChip = null
                    Log.d("My_tag", "Rect  x=$relativeLeft / y=$relativeTop")
                }
                Log.d("My_tag", "tile Number=" + view.tag)
            }
        }
        tilesArray+=array
    }

    fun dpToPixel(dpValue: Int): Int {
        val scale: Float = resources.getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }
}
