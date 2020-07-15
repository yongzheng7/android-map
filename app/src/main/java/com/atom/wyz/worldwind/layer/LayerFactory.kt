package com.atom.wyz.worldwind.layer

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.gpkg.GeoPackage
import com.atom.wyz.worldwind.ogc.gpkg.GpkgSpatialReferenceSystem
import com.atom.wyz.worldwind.ogc.gpkg.GpkgTileFactory
import com.atom.wyz.worldwind.ogc.wms.WmsCapabilities
import com.atom.wyz.worldwind.ogc.wms.WmsLayerCapabilities
import com.atom.wyz.worldwind.ogc.wms.WmsLayerConfig
import com.atom.wyz.worldwind.ogc.wms.WmsTileFactory
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.concurrent.RejectedExecutionException

open class LayerFactory() {
    companion object {
        protected const val DEFAULT_WMS_RADIANS_PER_PIXEL =
            10.0 / WorldWind.WGS84_SEMI_MAJOR_AXIS
        protected var compatibleImageFormats: List<String> =
            listOf(
                "image/png",
                "image/jpg",
                "image/gif",
                "image/bmp",
                "image/jpeg"
            )

        @Throws(Exception::class)
        protected fun retrieveWmsCapabilities(
            serviceAddress: String
        ): WmsCapabilities {
            var inputStream: InputStream? = null
            val wmsCapabilities: WmsCapabilities
            try {

                // Build the appropriate request Uri given the provided service address
                val serviceUri = Uri.parse(serviceAddress).buildUpon()
                    .appendQueryParameter("VERSION", "1.3.0")
                    .appendQueryParameter("SERVICE", "WMS")
                    .appendQueryParameter("REQUEST", "GetCapabilities")
                    .build()

                // Open the connection as an input stream
                val conn = URL(serviceUri.toString()).openConnection()
                conn.connectTimeout = 3000
                conn.readTimeout = 30000
                inputStream = BufferedInputStream(conn.getInputStream())
                // Parse and read the input stream
                wmsCapabilities = WmsCapabilities.getCapabilities(inputStream)
            } catch (e: Exception) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "retrieveWmsCapabilities",
                        "Unable to open connection and read from service address"
                    )
                )
            } finally {
                inputStream?.close()
            }
            return wmsCapabilities
        }

        protected fun parseLayerNames(
            wmsCapabilities: WmsCapabilities,
            layerNames: List<String?>
        ): List<WmsLayerCapabilities> {
            val layers: MutableList<WmsLayerCapabilities> =
                ArrayList()
            for (layerName in layerNames) {
                val layerCapabilities =
                    wmsCapabilities.getLayerByName(layerName)
                if (layerCapabilities != null) {
                    layers.add(layerCapabilities)
                }
            }
            return layers
        }

        fun getLayerConfigFromWmsCapabilities(
            layerCapabilities: List<WmsLayerCapabilities>
        ): WmsLayerConfig {

            // Construct the WmsTiledImage renderable from the WMS Capabilities properties
            val wmsLayerConfig = WmsLayerConfig()
            val wmsCapabilities = layerCapabilities.iterator().next().getServiceCapabilities()
            when (val version = wmsCapabilities?.getVersion()) {
                "1.3.0" -> {
                    wmsLayerConfig.wmsVersion = version
                }
                "1.1.1" -> {
                    wmsLayerConfig.wmsVersion = version
                }
                else -> {
                    throw java.lang.RuntimeException(
                        Logger.makeMessage(
                            "LayerFactory",
                            "getLayerConfigFromWmsCapabilities",
                            "Version not compatible"
                        )
                    )
                }
            }
            val requestUrl = wmsCapabilities.getRequestURL("GetMap", "Get")
            if (requestUrl == null) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "getLayerConfigFromWmsCapabilities",
                        "Unable to resolve GetMap URL"
                    )
                )
            } else {
                wmsLayerConfig.serviceAddress = requestUrl
            }

            var sb: StringBuilder? = null
            var matchingCoordinateSystems: MutableSet<String?>? = null
            for (layerCapability in layerCapabilities) {
                val layerName = layerCapability.getName()
                if (sb == null) {
                    sb = StringBuilder()
                } else {
                    sb.append(",").append(layerName)
                }
                val layerCoordinateSystems: Set<String?>? = layerCapability.getReferenceSystem()
                if (matchingCoordinateSystems == null) {
                    matchingCoordinateSystems = HashSet()
                    matchingCoordinateSystems.addAll(layerCoordinateSystems!!)
                } else {
                    matchingCoordinateSystems.retainAll(layerCoordinateSystems!!)
                }
            }

            wmsLayerConfig.layerNames = sb.toString()
            when {
                matchingCoordinateSystems!!.contains("EPSG:4326") -> {
                    wmsLayerConfig.coordinateSystem = "EPSG:4326"
                }
                matchingCoordinateSystems.contains("CRS:84") -> {
                    wmsLayerConfig.coordinateSystem = "CRS:84"
                }
                else -> {
                    throw java.lang.RuntimeException(
                        Logger.makeMessage(
                            "LayerFactory",
                            "getLayerConfigFromWmsCapabilities",
                            "Coordinate systems not compatible"
                        )
                    )
                }
            }
            val imageFormats: MutableSet<String>? = wmsCapabilities.getImageFormats()
            for (compatibleImageFormat in compatibleImageFormats) {
                if (imageFormats?.contains(compatibleImageFormat) == true) {
                    wmsLayerConfig.imageFormat = compatibleImageFormat
                    break
                }
            }
            if (wmsLayerConfig.imageFormat == null) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "getLayerConfigFromWmsCapabilities",
                        "Image Formats Not Compatible"
                    )
                )
            }

            return wmsLayerConfig
        }

        fun getLevelSetConfigFromWmsCapabilities(layerCapabilities: List<WmsLayerCapabilities>): LevelSetConfig {
            val levelSetConfig = LevelSetConfig()
            var minScaleDenominator = Double.MAX_VALUE
            var minScaleHint = Double.MAX_VALUE
            val sector = Sector()
            for (layerCapability in layerCapabilities) {
                val layerMinScaleDenominator =
                    layerCapability.getMinScaleDenominator()
                if (layerMinScaleDenominator != null) {
                    minScaleDenominator =
                        minScaleDenominator.coerceAtMost(layerMinScaleDenominator)
                }
                val layerMinScaleHint = layerCapability.getMinScaleHint()
                if (layerMinScaleHint != null) {
                    minScaleHint = minScaleHint.coerceAtMost(layerMinScaleHint)
                }
                val layerSector = layerCapability.getGeographicBoundingBox()
                if (layerSector != null) {
                    sector.union(layerSector)
                }
            }

            if (!sector.isEmpty()) {
                levelSetConfig.sector.set(sector)
            } else {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "getLevelSetConfigFromWmsCapabilities",
                        "Geographic Bounding Box Not Defined"
                    )
                )
            }

            when {
                minScaleDenominator != Double.MAX_VALUE -> {
                    // WMS 1.3.0 scale configuration. Based on the WMS 1.3.0 spec page 28. The hard coded value 0.00028 is
                    // detailed in the spec as the common pixel size of 0.28mm x 0.28mm. Configures the maximum level not to
                    // exceed the specified min scale denominator.
                    val minMetersPerPixel = minScaleDenominator * 0.00028
                    val minRadiansPerPixel =
                        minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
                    levelSetConfig.numLevels =
                        levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel)
                }
                minScaleHint != Double.MAX_VALUE -> {
                    // WMS 1.1.1 scale configuration, where ScaleHint indicates approximate resolution in ground distance
                    // meters. Configures the maximum level not to exceed the specified min scale denominator.
                    val minMetersPerPixel = minScaleHint
                    val minRadiansPerPixel =
                        minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
                    levelSetConfig.numLevels =
                        levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel)
                }
                else -> {
                    // Default scale configuration when no minimum scale denominator or scale hint is provided.
                    val defaultRadiansPerPixel =
                        DEFAULT_WMS_RADIANS_PER_PIXEL
                    levelSetConfig.numLevels =
                        levelSetConfig.numLevelsForResolution(defaultRadiansPerPixel)
                }
            }
            return levelSetConfig
        }
    }

    interface Callback {
        // 图层创建成功
        fun creationSucceeded(
            factory: LayerFactory,
            layer: Layer
        )

        // 图层创建失败
        fun creationFailed(
            factory: LayerFactory,
            layer: Layer,
            ex: Throwable?
        )
    }

    private val mainLoopHandler = Handler(Looper.getMainLooper())


    open fun createFromWmsLayerCapabilities(layerCapabilities: WmsLayerCapabilities,callback: Callback ): Layer {
        return this.createFromWmsLayerCapabilities(listOf(layerCapabilities) , callback)
    }

    open fun createFromWmsLayerCapabilities(layerCapabilities: List<WmsLayerCapabilities> , callback: Callback ): Layer {
        require(!(layerCapabilities.isEmpty())) {
            Logger.logMessage(
                Logger.ERROR,
                "LayerFactory",
                "createFromWmsLayerCapabilities",
                "missing layers"
            )
        }
        val layer: Layer = RenderableLayer()
        createWmsLayer(layerCapabilities, layer, callback)
        return layer
    }

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
        } catch (logged: RejectedExecutionException) {
            // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }
        return layer
    }

    open fun createFromWms(
        serviceAddress: String,
        layerName: String,
        callback: Callback
    ): Layer {
        return createFromWms(serviceAddress, listOf(layerName), callback)
    }

    fun createFromWms(
        serviceAddress: String,
        layerNames: List<String>,
        callback: Callback
    ): Layer {
        require(!(layerNames.isEmpty())) {
            Logger.logMessage(
                Logger.ERROR,
                "LayerFactory",
                "createFromWms",
                "missingLayerNames"
            )
        }
        val layer = RenderableLayer()
        layer.pickEnabled = (false)
        val task = WmsAsyncTask(this, serviceAddress, layerNames, layer, callback)
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) {
            // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }
        return layer
    }

    // 创建基于本地数据库的图层
    private fun createFromGeoPackageAsync(
        pathName: String,
        layer: Layer,
        callback: Callback
    ) {
        val geoPackage = GeoPackage(pathName)
        val gpkgRenderables = RenderableLayer()
        val finalLayer = layer as RenderableLayer
        for (contents in geoPackage.content) {
            if (contents.dataType == null || !contents.dataType.equals("tiles", true)) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage content data_type: " + contents.dataType
                )
                continue
            }
            val srs: GpkgSpatialReferenceSystem =
                geoPackage.spatialReferenceSystem.get(contents.srsId)
            if (!srs.organization
                    .equals("EPSG", true) || srs.organizationCoordSysId != 4326
            ) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage spatial reference system: " + srs.srsName
                )
                continue
            }
            val tileMatrixSet = geoPackage.getTileMatrixSet(contents.tableName!!)
            if (tileMatrixSet == null || tileMatrixSet.srsId != contents.srsId) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage tile matrix set"
                )
                continue
            }
            val tileMatrices = geoPackage.getTileMatrix(contents.tableName!!)
            if (tileMatrices == null || tileMatrices.size() == 0) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Undefined GeoPackage tile matrices"
                )
                continue
            }
            var maxZoomLevel = 0
            var idx = 0
            val len = tileMatrices.size()
            while (idx < len) {
                val zoomLevel: Int = tileMatrices.valueAt(idx).zoomLevel
                if (maxZoomLevel < zoomLevel) {
                    maxZoomLevel = zoomLevel
                }
                idx++
            }
            val config = LevelSetConfig()
            config.sector[contents.minY, contents.minX, contents.maxY - contents.minY] =
                contents.maxX - contents.minX
            config.firstLevelDelta = 180.0
            config.numLevels = maxZoomLevel + 1
            config.tileWidth = 256
            config.tileHeight = 256
            val surfaceImage = TiledSurfaceImage()
            surfaceImage.levelSet = (LevelSet(config))
            surfaceImage.tileFactory = (GpkgTileFactory(contents))
            gpkgRenderables.addRenderable(surfaceImage)
        }
        // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
        // that the image displays on all WorldWindows the layer may be attached to.
        mainLoopHandler.post {
            finalLayer.addAllRenderables(gpkgRenderables)
            callback.creationSucceeded(this@LayerFactory, finalLayer)
            WorldWind.requestRedraw()
        }
    }

    protected open fun createWmsLayer(
        layerCapabilities: List<WmsLayerCapabilities>,
        layer: Layer,
        callback: Callback
    ){
        // 解析wms文档
        val wmsCapabilities =
            layerCapabilities.iterator().next().getServiceCapabilities()
        // Check if the server supports multiple layer request
        val layerLimit = wmsCapabilities?.getServiceInformation()?.getLayerLimit()
        if (layerLimit != null) {
            if (layerLimit < layerCapabilities.size) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "createFromWmsAsync",
                        "The number of layers specified exceeds the services limit"
                    )
                )
            }
        }
        val wmsLayerConfig =
            getLayerConfigFromWmsCapabilities(layerCapabilities)
        val levelSetConfig =
            getLevelSetConfigFromWmsCapabilities(layerCapabilities)
        // Collect WMS Layer Titles to set the Layer Display Name
        var sb: StringBuilder? = null
        for (layerCapability in layerCapabilities) {
            val s = layerCapability.getTitle() ?: continue
            if (sb == null) {
                sb = StringBuilder(s)
            } else {
                sb.append(",").append(s)
            }
        }

        layer.displayName = (sb.toString())


        val surfaceImage = TiledSurfaceImage()
        val finalLayer = layer as RenderableLayer
        surfaceImage.tileFactory = WmsTileFactory(wmsLayerConfig)
        surfaceImage.levelSet = LevelSet(levelSetConfig)
        mainLoopHandler.post {
            finalLayer.addRenderable(surfaceImage)
            callback.creationSucceeded(this@LayerFactory, finalLayer)
            WorldWind.requestRedraw()
        }
    }

    // 创建基于WMS XML 的图层
    private fun createFromWmsAsync(
        serviceAddress: String,
        layerNames: List<String>,
        layer: Layer,
        callback: Callback
    ) {
        // 解析wms文档
        // Parse and read the WMS Capabilities document at the provided service address
        val wmsCapabilities: WmsCapabilities = retrieveWmsCapabilities(serviceAddress)
        val layerCapabilities: List<WmsLayerCapabilities> =
            parseLayerNames(wmsCapabilities, layerNames)
        if (layerCapabilities.isEmpty()) {
            throw RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createFromWmsAsync",
                    "Provided layers did not match available layers"
                )
            )
        }
        createWmsLayer(layerCapabilities, layer, callback)
    }

    protected class GeoPackageAsyncTask(
        private var factory: LayerFactory,
        private var pathName: String,
        private var layer: Layer,
        private var callback: Callback
    ) : Runnable {
        override fun run() {
            try {
                factory.createFromGeoPackageAsync(pathName, layer, callback)
            } catch (ex: Throwable) {
                factory.mainLoopHandler.post { callback.creationFailed(factory, layer, ex) }
            }
        }
    }

    protected class WmsAsyncTask(
        private val factory: LayerFactory,
        private val serviceAddress: String,
        private val layerNames: List<String>,
        private val layer: Layer,
        private val callback: Callback
    ) : Runnable {
        override fun run() {
            try {
                factory.createFromWmsAsync(
                    serviceAddress,
                    layerNames,
                    layer,
                    callback
                )
            } catch (ex: Throwable) {
                factory.mainLoopHandler.post { callback.creationFailed(factory, layer, ex) }
            }
        }
    }
}