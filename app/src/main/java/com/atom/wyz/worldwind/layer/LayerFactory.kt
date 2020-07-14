package com.atom.wyz.worldwind.layer

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.ogc.WmsCapabilities
import com.atom.wyz.worldwind.ogc.WmsLayerCapabilities
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.ogc.WmsTileFactory
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.util.concurrent.RejectedExecutionException

open class LayerFactory() {
    companion object{
        protected const val DEFAULT_WMS_RADIANS_PER_PIXEL =
            10.0 / WorldWind.WGS84_SEMI_MAJOR_AXIS
    }
    interface Callback {
        fun creationSucceeded(factory: LayerFactory, layer: Layer)
        fun creationFailed(
            factory: LayerFactory,
            layer: Layer,
            ex: Throwable?
        )
    }

    protected var mainLoopHandler = Handler(Looper.getMainLooper())

    fun createFromGeoPackage(
        pathName: String,
        callback: Callback
    ): Layer {
        val layer = RenderableLayer()
        layer.pickEnabled = (false)
        val task = GeoPackageAsyncTask(
                this,
                pathName,
                layer,
                callback
            )
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }
        return layer
    }

    fun createFromWms(
        serviceAddress: String,
        layerNames: String,
        callback: Callback
    ): Layer {
        val layer = RenderableLayer()
        layer.pickEnabled = (false)
        val task = WmsAsyncTask(this, serviceAddress, layerNames, layer, callback)
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }
        return layer
    }

    private fun createGeoPackageLayerAsync(
        pathName: String?,
        layer: Layer?,
        callback: Callback?
    ) {

    }

    private fun createWmsLayerAsync(
        serviceAddress: String,
        layerNames: String,
        layer: Layer,
        callback: Callback
    ) {
        // Retrieve and parse the WMS capabilities at the specified service address, looking for the named layers
        // specified by the comma-delimited layerNames
        val serviceUri = Uri.parse(serviceAddress).buildUpon()
            .appendQueryParameter("VERSION", "1.3.0")
            .appendQueryParameter("SERVICE", "WMS")
            .appendQueryParameter("REQUEST", "GetCapabilities")
            .build()
        Log.e("createWmsLayerAsync" ,"serviceUri > $serviceUri")
        val conn = URL(serviceUri.toString()).openConnection()
        conn.connectTimeout = 3000
        conn.readTimeout = 30000
        val inputStream: InputStream = BufferedInputStream(conn.getInputStream())
        val wmsCapabilities = WmsCapabilities.getCapabilities(inputStream)
        val wmsLayerConfig = WmsLayerConfig()
        wmsLayerConfig.wmsVersion = wmsCapabilities.getVersion()!!

        val requestUrl = wmsCapabilities.getRequestURL("GetMap", "Get")
        checkNotNull(requestUrl) {
            Logger.makeMessage(
                "LayerFactory",
                "createWmsLayerAsync",
                "Unable to resolve GetMap URL"
            )
        }
        wmsLayerConfig.serviceAddress = requestUrl

        val layerCapabilities: WmsLayerCapabilities? = wmsCapabilities.getLayerByName(layerNames)
        requireNotNull(layerCapabilities) {
            Logger.makeMessage(
                "LayerFactory",
                "createWmsLayerAsync",
                "Provided layer did not match available layers"
            )
        }
        wmsLayerConfig.layerNames = layerCapabilities.getName()!!

        val coordinateSystems: Set<String> = layerCapabilities.getReferenceSystem()!!
        if (coordinateSystems.contains("EPSG:4326")) {
            wmsLayerConfig.coordinateSystem = "EPSG:4326"
        } else if (coordinateSystems.contains("CRS:84")) {
            wmsLayerConfig.coordinateSystem = "CRS:84"
        } else {
            throw RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createWmsLayerAsync",
                    "Coordinate systems not compatible"
                )
            )
        }

        val imageFormats =
            wmsCapabilities.getImageFormats()
        if (imageFormats!!.contains("image/png")) {
            wmsLayerConfig.imageFormat = "image/png"
        } else {
            wmsLayerConfig.imageFormat = imageFormats!!.iterator().next()
        }

        val levelSetConfig = LevelSetConfig()

        val sector = layerCapabilities.getGeographicBoundingBox()
        if (sector != null) {
            levelSetConfig.sector?.set(sector)
        }

        if (layerCapabilities.getMinScaleDenominator() != null && layerCapabilities.getMinScaleDenominator() != 0.0) {
            // WMS 1.3.0 scale configuration. Based on the WMS 1.3.0 spec page 28. The hard coded value 0.00028 is
            // detailed in the spec as the common pixel size of 0.28mm x 0.28mm. Configures the maximum level not to
            // exceed the specified min scale denominator.
            val minMetersPerPixel: Double = layerCapabilities.getMinScaleDenominator()!! * 0.00028
            val minRadiansPerPixel =
                minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
            levelSetConfig.numLevels = levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel)
        } else if (layerCapabilities.getMinScaleHint() != null && layerCapabilities.getMinScaleHint() != 0.0) {
            // WMS 1.1.1 scale configuration, where ScaleHint indicates approximate resolution in ground distance
            // meters. Configures the maximum level not to exceed the specified min scale denominator.
            val minMetersPerPixel: Double = layerCapabilities.getMinScaleHint()!!
            val minRadiansPerPixel =
                minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
            levelSetConfig.numLevels = levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel)
        } else {
            // Default scale configuration when no minimum scale denominator or scale hint is provided.
            val defaultRadiansPerPixel = DEFAULT_WMS_RADIANS_PER_PIXEL
            levelSetConfig.numLevels = levelSetConfig.numLevelsForResolution(defaultRadiansPerPixel)
        }

        val surfaceImage = TiledSurfaceImage()
        val finalLayer = layer as RenderableLayer

        surfaceImage.tileFactory = (WmsTileFactory(wmsLayerConfig))
        surfaceImage.levelSet = (LevelSet(levelSetConfig))

        // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
        // that the image displays on all WorldWindows the layer may be attached to.

        // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
        // that the image displays on all WorldWindows the layer may be attached to.
        mainLoopHandler.post {
            finalLayer.addRenderable(surfaceImage)
            callback.creationSucceeded(this@LayerFactory, finalLayer)
            WorldWind.requestRedraw()
        }
    }

    protected class GeoPackageAsyncTask(
        private var factory: LayerFactory,
        private var pathName: String,
        private var layer: Layer,
        private var callback: Callback
    ) : Runnable {
        override fun run() {
            try {
                factory.createGeoPackageLayerAsync(pathName, layer, callback)
            } catch (ex: Throwable) {
                callback.creationFailed(factory, layer, ex)
            }
        }
    }

    protected class WmsAsyncTask(
        private val factory: LayerFactory,
        private val serviceAddress: String,
        private val layerNames: String,
        private val layer: Layer,
        private val callback: Callback
    ) : Runnable {
        override fun run() {
            try {
                factory.createWmsLayerAsync(
                    serviceAddress,
                    layerNames,
                    layer,
                    callback
                )
            } catch (ex: Throwable) {
                callback.creationFailed(factory, layer, ex)
            }
        }
    }
}