package com.atom.wyz.worldwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.ogc.WmsLayer

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }


    fun menuButton(view: View) {
        when (view.id) {
            R.id.base_activity -> {
                startActivity(Intent(this, BasicWorldWindActivity::class.java))
            }
            R.id.camera_activity -> {
                startActivity(Intent(this, CameraControlActivity::class.java))
            }
            R.id.camera_view_activity -> {
                startActivity(Intent(this, CameraViewActivity::class.java))
            }
            R.id.lookat_view_activity -> {
                startActivity(Intent(this, LookAtViewActivity::class.java))
            }
            R.id.day_light_activity -> {
                startActivity(Intent(this, DayNightCycleActivity::class.java))
            }
            R.id.show_tessellation_activity -> {
                startActivity(Intent(this, ShowTessellationActivity::class.java))
            }
            R.id.surface_image_activity -> {
                startActivity(Intent(this, SurfaceImageActivity::class.java))
            }
            R.id.wms_activity -> {
                startActivity(Intent(this, WmsLayerActivity::class.java))
            }
            R.id.placemark_activity -> {
                startActivity(Intent(this, PlacemarksActivity::class.java))
            }
            R.id.placemark2_activity -> {
                startActivity(Intent(this, Placemarks2Activity::class.java))
            }
            R.id.placemark_stress_activity -> {
                startActivity(Intent(this, PlacemarksStressTestActivity::class.java))
            }
        }
    }

}