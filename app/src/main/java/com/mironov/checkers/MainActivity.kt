package com.mironov.checkers

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.util.DisplayMetrics
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.marginLeft


class MainActivity : AppCompatActivity() {

    private lateinit var flowLayout: FlowLayout
    private lateinit var frameLayout: FrameLayout
    private lateinit var darkChip:View

    private var screenWidth=0

    private fun findViews() {
        flowLayout = findViewById(R.id.flowLayout)
        frameLayout = findViewById(R.id.board)
        darkChip = this.layoutInflater.inflate(R.layout.chip, null)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        findViews()
    }

    override fun onResume() {
        super.onResume()
        addLayouts()
    }


    private fun addLayouts() {

        flowLayout.removeAllViews()
        frameLayout.addView(darkChip,0,0)

        darkChip.layoutParams.height=80
        darkChip.layoutParams.width=80

        val tileSize=(screenWidth-2*flowLayout.marginLeft)/ 8
        var j=0
        for (i in 0..63) {
            if(i%8==0){j++}

            val selected = booleanArrayOf(false)
            val view: View = this.layoutInflater.inflate(R.layout.tile, null)
            val imageView = view.findViewById<View>(R.id.tileImage) as ImageView

            view.tag=i
            imageView.layoutParams.height=tileSize
            imageView.layoutParams.width=tileSize

            //Set spacing here
            view.layoutParams=FlowLayout.LayoutParams(1,1)

            when {
                (i+j) % 2 == 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_dark))
                (i+j) % 2 != 0 -> imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_cell_light))
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

                darkChip.translationX= relativeLeft.toFloat()+view.width/2-darkChip.width/2
                darkChip.translationY= relativeTop.toFloat()+view.height/2-darkChip.height/2

                Log.d("My_tag","Rect  x=$relativeLeft / y=$relativeTop")
                Log.d("My_tag","tile Number="+view.tag)
            }
        }
    }
    fun dpToPixel( dpValue: Int): Int {
        val scale: Float = resources.getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }
}
