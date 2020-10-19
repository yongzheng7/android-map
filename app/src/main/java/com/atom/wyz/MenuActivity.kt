package com.atom.wyz

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
    fun showOrHide(res : Int){
        val view = findViewById<View>(res)
        if(view.isShown){
            view.visibility = View.GONE
        }else{
            view.visibility = View.VISIBLE
        }
    }

    fun menuButton(view: View) {
        when (view.id) {
            R.id.basic_button -> {
                showOrHide(R.id.basic_layout)
            }
            R.id.math_button -> {
                showOrHide(R.id.math_layout)
            }
            R.id.pick_button -> {
                showOrHide(R.id.pick_layout)
            }
            R.id.shape_button -> {
                showOrHide(R.id.shape_layout)
            }
            R.id.dynamic_button -> {
                showOrHide(R.id.dynamic_layout)
            }
            R.id.auth_button -> {
                showOrHide(R.id.auth_layout)
            }
            R.id.size_change_button -> {
                showOrHide(R.id.size_change_layout)
            }
            R.id.look_button -> {
                showOrHide(R.id.look_layout)
            }
            R.id.omnidirectionalSensor2_Activity -> {
                startActivity(Intent(this, OmnidirectionalSensor2Activity::class.java))
            }
            R.id.cartesian_Activity -> {
                startActivity(Intent(this, CartesianActivity::class.java))
            }
            R.id.omnidirectionalSensor_Activity -> {
                startActivity(Intent(this, OmnidirectionalSensorActivity::class.java))
            }
            R.id.Wcs_Elevation_Fragment -> {
                startActivity(Intent(this, WcsElevationFragment::class.java))
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
            R.id.wtmsLayer_Activity -> {
                startActivity(Intent(this, WtmsLayerActivity::class.java))
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
            R.id.ellipse_Fragment -> {
                startActivity(Intent(this, EllipseFragment::class.java))
            }
            R.id.tianditu_Activity -> {
                startActivity(Intent(this, TiandituLayerActivity::class.java))
            }
            R.id.google_Activity -> {
                startActivity(Intent(this, GoogleLayerActivity::class.java))
            }
            R.id.tianditu2_Activity -> {
                startActivity(Intent(this, Tianditu2LayerActivity::class.java))
            }
            R.id.tianditu3_Activity -> {
                startActivity(Intent(this, Tianditu3LayerActivity::class.java))
            }
            R.id.tianditu4_Activity -> {
                startActivity(Intent(this, Tianditu4LayerActivity::class.java))
            }
        }
    }

}