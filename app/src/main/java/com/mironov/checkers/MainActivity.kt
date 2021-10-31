package com.mironov.checkers

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.util.DisplayMetrics
import androidx.core.view.marginLeft


class MainActivity : AppCompatActivity() {

    private lateinit var flowLayout: FlowLayout

    private var screenWidth=0

    private fun findViews() {
        flowLayout = findViewById(R.id.flowLayout)
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

    @SuppressLint( "SetTextI18n", "InflateParams", "UseCompatLoadingForDrawables")
    private fun addLayouts() {

        flowLayout.removeAllViews()


        val margin=flowLayout.marginLeft
        val marginDp=dpToPixel(margin)

        val tileSize=(screenWidth-2*marginDp)/ 8
        var j=0
        for (i in 0..63) {
            if(i%8==0){j++}

            val selected = booleanArrayOf(false)
            val view: View = this.layoutInflater.inflate(R.layout.tile, null)
            val imageView = view.findViewById<View>(R.id.tileImage) as ImageView

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
                selected[0] = !selected[0]
            }
        }
    }
    fun dpToPixel( dpValue: Int): Int {
        val scale: Float = resources.getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }
}
