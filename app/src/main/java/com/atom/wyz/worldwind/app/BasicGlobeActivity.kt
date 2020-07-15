package com.atom.wyz.worldwind.app

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.BackgroundLayer
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerFactory
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.ogc.wms.WmsCapabilities
import com.atom.wyz.worldwind.ogc.wms.WmsLayerCapabilities
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

open class BasicGlobeActivity :  AppCompatActivity(){

    protected lateinit var wwd: WorldWindow
    protected var layoutResourceId: Int = R.layout.activity_turse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutResourceId)
        createWorldWindow()
    }

    protected open fun createWorldWindow() : WorldWindow{
        wwd = WorldWindow(this)
        val globeLayout = findViewById<FrameLayout>(R.id.globe)
        globeLayout.addView(wwd)
        wwd.layers.addLayer(BackgroundLayer())
        //wwd.layers.addLayer(BlueMarbleLandsatLayer())
        InitializeWmsLayersTask().execute()
        return wwd
    }

    protected inner class InitializeWmsLayersTask : AsyncTask<Void?, String?, Void?>() {
        protected var layers: LayerList = LayerList()
        protected override fun doInBackground(vararg params: Void?): Void? {
            // TIP: 10.0.2.2 is used to access the host development machine from emulator
            val WWSK_GWC = "http://10.0.2.2:8080/worldwind-geoserver/gwc/service/wms" // GeoWebCache
            val WWSK_WMS = "http://10.0.2.2:8080/worldwind-geoserver/ows" // WMS
            val SSGF = "http://10.0.1.7:8080/worldwind-geoserver/ows"
            val TMIS = "http://10.0.1.7:5000/WmsServer"
            try {
                // Build a WMS server GetCapabilties request
                val serviceUri = Uri.parse(WWSK_GWC).buildUpon()
                    .appendQueryParameter("VERSION", "1.3.0")
                    .appendQueryParameter("SERVICE", "WMS")
                    .appendQueryParameter("REQUEST", "GetCapabilities")
                    .build()

                // Connect and read capabilities document
                val conn = URL(serviceUri.toString()).openConnection()
                conn.connectTimeout = 3000
                conn.readTimeout = 30000
                val inputStream: InputStream = BufferedInputStream(conn.getInputStream())
                // Parse the capabilities
                val wmsCapabilities: WmsCapabilities = WmsCapabilities.getCapabilities(inputStream)
                val namedLayers: List<WmsLayerCapabilities>? = wmsCapabilities.getNamedLayers()

                // Setup the factory that will create WMS layers from the capabilities
                val layerFactory = LayerFactory()
                val callback: LayerFactory.Callback = object : LayerFactory.Callback {
                    override fun creationSucceeded(factory: LayerFactory, layer: Layer) {
                        wwd.layers.addLayer(layer)
                    }

                    override  fun creationFailed(
                        factory: LayerFactory,
                        layer: Layer,
                        ex: Throwable?
                    ) {
                        Log.e(
                            "gov.nasa.worldwind",
                            "WMS layer creation failed: " + layer.toString(),
                            ex
                        )
                    }
                }

                // Create all the WMS layers
                if (namedLayers != null) {
                    for (layerCaps in namedLayers) {
                        // Set the new layer's properties from the layer capabilities;
                        // the callback will add the layer to the layer list.
                        // TODO: Why is serverAddress needed, isn't the layer caps sufficient?
                        val layer: Layer = layerFactory.createFromWms(WWSK_GWC, layerCaps.getName()!!, callback)
                        layer.displayName = ("> SSGF - " + layerCaps.getTitle())
                        layer.putUserProperty(
                            "BBOX",
                            layerCaps.getGeographicBoundingBox()!!
                        ) // TODO: use for highlighting layers in view
                        layer.putUserProperty(
                            "MAX_SCALE_DENOM",
                            layerCaps.getMaxScaleDenominator()!!
                        ) // TODO: use for sorting the layers
                        layer.putUserProperty(
                            "MIN_SCALE_DENOM",
                            layerCaps.getMinScaleDenominator()!!
                        ) // TODO: use for sorting the layers
                        layer.enabled = (false)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }
            return null
        }
    }
}