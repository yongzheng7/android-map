package com.atom.wyz.worldwind.app

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.AtmosphereLayer
import com.atom.wyz.worldwind.layer.BackgroundLayer
import com.atom.wyz.worldwind.layer.BlueMarbleLandsatLayer
import java.util.*

class MultiGlobeActivity : AppCompatActivity() {

    var layoutResourceId: Int = R.layout.activity_multi
    protected var deviceOrientation = 0
    protected var worldWindows: ArrayList<WorldWindow> = ArrayList<WorldWindow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResourceId)
        deviceOrientation = resources.configuration.orientation
        performLayout()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun performLayout() {
        releaseWorldWindows()
        val splitter = findViewById(R.id.splitter) as ImageButton
        val globe1 = findViewById(R.id.globe_one) as FrameLayout
        val globe2 = findViewById(R.id.globe_two) as FrameLayout
        globe1.addView(
            getWorldWindow(0) ?:createWorldWindow(),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        globe2.addView(
            getWorldWindow(1) ?:createWorldWindow(),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        splitter.setOnTouchListener(
            SplitterTouchListener(
                globe1,
                globe2,
                splitter
            )
        )

    }

    private fun releaseWorldWindows() {
        for (wwd in worldWindows) {
            (wwd.parent as ViewGroup).removeView(wwd)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
        deviceOrientation = newConfig.orientation
        performLayout()
    }

    private fun createWorldWindow(): WorldWindow? { // Create the World Window (a GLSurfaceView) which displays the globe.
        val wwd = WorldWindow(this)
        wwd.layers.addLayer(BackgroundLayer())
        wwd.layers.addLayer(BlueMarbleLandsatLayer())
        wwd.layers.addLayer(AtmosphereLayer())
        worldWindows.add(wwd)
        return wwd
    }

    override fun onPause() {
        super.onPause()
        for (wwd in worldWindows) {
            wwd.onPause() // pauses the rendering thread
        }
    }

    override fun onResume() {
        super.onResume()
        for (wwd in worldWindows) {
            wwd.onResume() // resumes a paused rendering thread
        }
    }

    fun getWorldWindow(): WorldWindow? {
        return this.getWorldWindow(0)
    }

    fun getWorldWindow(index: Int): WorldWindow? {
        if (index >= worldWindows.size) {
            return null
        }
        return worldWindows[index]
    }

    private inner class SplitterTouchListener // TODO: compute this value
        (private val one: FrameLayout, private val two: FrameLayout, private val splitter: ImageButton) :
        OnTouchListener {
        private val splitterWeight = 30
        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @param v     The view the touch event has been dispatched to.
         * @param event The MotionEvent object containing full information about
         * the event.
         * @return True if the listener has consumed the event, false otherwise.
         */
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val rawX = event.rawX
                    val rawY = event.rawY
                    val parent = findViewById(R.id.multi_globe_content) as LinearLayout
                    val layout1 = one.layoutParams as LinearLayout.LayoutParams
                    val layout2 = two.layoutParams as LinearLayout.LayoutParams
                    val layout3 = splitter.layoutParams as LinearLayout.LayoutParams
                    val weightSum: Int
                    if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        weightSum = parent.width
                        layout1.weight = Math.min(
                            Math.max(0f, rawX - splitterWeight / 2f),
                            weightSum - splitterWeight.toFloat()
                        )
                        layout2.weight = Math.min(
                            Math.max(0f, weightSum - layout1.weight - splitterWeight),
                            weightSum - splitterWeight.toFloat()
                        )
                        parent.weightSum = weightSum.toFloat()
                    } else {
                        val origin = IntArray(2)
                        parent.getLocationOnScreen(origin)
                        val y = rawY - origin[1]
                        weightSum = parent.height
                        layout2.weight = Math.min(
                            Math.max(0f, y - splitterWeight / 2f),
                            weightSum - splitterWeight.toFloat()
                        )
                        layout1.weight = Math.min(
                            Math.max(0f, weightSum - layout2.weight - splitterWeight),
                            weightSum - splitterWeight.toFloat()
                        )
                        parent.weightSum = weightSum.toFloat()
                    }
                    layout3.weight = splitterWeight.toFloat()
                    one.layoutParams = layout1
                    two.layoutParams = layout2
                    splitter.layoutParams = layout3
                }
            }
            return false
        }

    }
}