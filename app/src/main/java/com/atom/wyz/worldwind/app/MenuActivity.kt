package com.atom.wyz.worldwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }


    fun menuButton(view: View) {
        when (view.id) {
            R.id.Zip_Activity -> {
                startActivity(Intent(this, ZipActivity::class.java))
            }
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
            R.id.basic_performance_avtivity -> {
                startActivity(Intent(this, BasicPerformanceBenchmarkActivity::class.java))
            }
            R.id.navigator_event_activity -> {
                startActivity(Intent(this, NavigatorEventActivity::class.java))
            }
            R.id.placemark_select_drag_activity -> {
                startActivity(Intent(this, PlacemarksSelectDragActivity::class.java))
            }
            R.id.placemark_pick_activity -> {
                startActivity(Intent(this, PlacemarksPickingActivity::class.java))
            }
            R.id.basic_stress_test_activity -> {
                startActivity(Intent(this, BasicStressTestActivity::class.java))
            }
            R.id.placemark_Dragger_activity -> {
                startActivity(Intent(this, PlacemarksDraggerActivity::class.java))
            }
            R.id.placemark_Dragger_2_activity -> {
                startActivity(Intent(this, PlacemarksDragger2Activity::class.java))
            }
            R.id.texture_stress_activity -> {
                startActivity(Intent(this, TextureStressTestActivity::class.java))
            }
            R.id.placemark_demo_activity -> {
                startActivity(Intent(this, PlacemarksDemoActivity::class.java))
            }
            R.id.turse_BasicGlobeActivity -> {
                startActivity(Intent(this, BasicGlobeActivity::class.java))
            }
            R.id.paths_Example_Activity -> {
                startActivity(Intent(this, PathsExampleActivity::class.java))
            }
            R.id.paths_Activity -> {
                startActivity(Intent(this, PathsActivity::class.java))
            }
            R.id.polygons_Activity -> {
                startActivity(Intent(this, PolygonsActivity::class.java))
            }
            R.id.pathsAndPolygonsActivity -> {
                startActivity(Intent(this, PathsAndPolygonsActivity::class.java))
            }
            R.id.multiGlobeActivity -> {
                startActivity(Intent(this, MultiGlobeActivity::class.java))
            }
            R.id.LabelsFragment -> {
                startActivity(Intent(this, LabelsFragment::class.java))
            }
            R.id.ShapesDashAndFillFragment -> {
                startActivity(Intent(this, ShapesDashAndFillFragment::class.java))
            }
        }
    }

}