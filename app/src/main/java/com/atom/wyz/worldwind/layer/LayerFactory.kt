package com.atom.wyz.worldwind.layer

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.ogc.WmsTileFactory
import com.atom.wyz.worldwind.ogc.WmtsTileFactory
import com.atom.wyz.worldwind.ogc.gpkg.GeoPackage
import com.atom.wyz.worldwind.ogc.gpkg.GpkgTileFactory
import com.atom.wyz.worldwind.ogc.gpkg.GpkgTileMatrixSet
import com.atom.wyz.worldwind.ogc.gpkg.GpkgTileUserMetrics
import com.atom.wyz.worldwind.ogc.wms.WmsCapabilities
import com.atom.wyz.worldwind.ogc.wms.WmsLayer
import com.atom.wyz.worldwind.ogc.wtms.*
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.concurrent.RejectedExecutionException

open class LayerFactory() {
    companion object {
        protected const val DEFAULT_WMS_NUM_LEVELS = 20
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

    protected var compatibleImageFormats = listOf(
        "image/png",
        "image/jpg",
        "image/jpeg",
        "image/gif",
        "image/bmp"
    )
    protected var compatibleCoordinateSystems =
        listOf(
            "urn:ogc:def:crs:OGC:1.3:CRS84",
            "urn:ogc:def:crs:EPSG::4326",
            "http://www.opengis.net/def/crs/OGC/1.3/CRS84"
        )

    open fun createFromGeoPackage(
        pathName: String,
        callback: Callback
    ): Layer? {
        // Create a layer in which to asynchronously populate with renderables for the GeoPackage contents.
        val layer = RenderableLayer()
        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.pickEnabled = (false)
        val task = GeoPackageAsyncTask(this, pathName, layer, callback)
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) { // singleton task service is full; this should never happen but we check anyway
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

    open fun createFromWms(
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
        // Create a layer in which to asynchronously populate with renderables for the GeoPackage contents.
        val layer = RenderableLayer()

        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.pickEnabled = (false)
        val task = WmsAsyncTask(this, serviceAddress, layerNames, layer, callback)
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }
        return layer
    }

    open fun createFromGeoPackageAsync(
        pathName: String,
        layer: Layer,
        callback: Callback
    ) {
        val geoPackage = GeoPackage(pathName)
        val gpkgRenderables = RenderableLayer()
        for (content in geoPackage.content) {
            if (content.dataType == null || !content.dataType.equals("tiles", true)) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage content data_type: " + content.dataType
                )
                continue
            }
            val srs =
                geoPackage.getSpatialReferenceSystem(content.srsId)
            if (srs == null || !srs.organization
                    .equals("EPSG", true) || srs.organizationCoordSysId != 4326
            ) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage spatial reference system: " + if (srs == null) "undefined" else srs.srsName
                )
                continue
            }
            val tileMatrixSet: GpkgTileMatrixSet? =
                geoPackage.getTileMatrixSet(content.tableName!!)
            if (tileMatrixSet == null || tileMatrixSet.srsId != content.srsId) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage tile matrix set"
                )
                continue
            }
            val tileMetrics: GpkgTileUserMetrics? =
                geoPackage.getTileUserMetrics(content.tableName!!)
            if (tileMetrics == null) {
                Logger.logMessage(
                    Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage tiles content"
                )
                continue
            }
            val config = LevelSetConfig()
            config.sector[content.minY, content.minX, content.maxY - content.minY] =
                content.maxX - content.minX
            config.firstLevelDelta = 180.0
            config.numLevels =
                tileMetrics.getMaxZoomLevel() + 1 // zero when there are no zoom levels, (0 = -1 + 1)
            config.tileWidth = 256
            config.tileHeight = 256
            val surfaceImage = TiledSurfaceImage()
            surfaceImage.levelSet = (LevelSet(config))
            surfaceImage.tileFactory = (GpkgTileFactory(content))
            gpkgRenderables.addRenderable(surfaceImage)
        }
        if (gpkgRenderables.count() == 0) {
            throw RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createFromGeoPackageAsync",
                    "Unsupported GeoPackage contents"
                )
            )
        }
        val finalLayer = layer as RenderableLayer
        val finalCallback = callback

        // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
        // that the image displays on all WorldWindows the layer may be attached to.
        mainLoopHandler.post {
            finalLayer.addAllRenderables(gpkgRenderables)
            finalCallback.creationSucceeded(this@LayerFactory, finalLayer)
            WorldWind.requestRedraw()
        }
    }

    open fun createFromWmts(
        serviceAddress: String,
        layerIdentifier: String,
        callback: Callback
    ): Layer {
        require(!(layerIdentifier.isEmpty())) {
            Logger.logMessage(
                Logger.ERROR,
                "LayerFactory",
                "createFromWms",
                "missingLayerNames"
            )
        }

        // Create a layer in which to asynchronously populate with renderables for the GeoPackage contents.
        val layer = RenderableLayer()
        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.pickEnabled = (false)
        val task = WmtsAsyncTask(
            this,
            serviceAddress,
            layerIdentifier,
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

    @Throws(java.lang.Exception::class)
    protected open fun createFromWmtsAsync(
        serviceAddress: String,
        layerIdentifier: String,
        layer: Layer,
        callback: Callback
    ) {
        // Parse and read the WMTS Capabilities document at the provided service address
        val wmtsCapabilities: WmtsCapabilities = this.retrieveWmtsCapabilities(serviceAddress)
        val wmtsLayer: WmtsLayer = wmtsCapabilities.getLayer(layerIdentifier)
            ?: throw java.lang.RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createFromWmtsAsync",
                    "The layer identifier specified was not found"
                )
            )
        this.createWmtsLayer(wmtsLayer, layer, callback)
    }

    protected open fun determineCoordSysCompatibleTileMatrixSets(layer: WmtsLayer): List<String> {
        val compatibleTileMatrixSets: MutableList<String> = ArrayList()
        // Look for compatible coordinate system types
        layer.getLayerSupportedTileMatrixSets()?.forEach {
            if (compatibleCoordinateSystems.contains(it.supportedCrs)) {
                it.identifier?.let {
                    compatibleTileMatrixSets.add(it)
                }
            }
        }
        return compatibleTileMatrixSets
    }

    protected open fun determineTileSchemeCompatibleTileMatrixSet(
        capabilities: WmtsCapabilities,
        tileMatrixSetIds: List<String?>
    ): CompatibleTileMatrixSet? {
        val compatibleSet = CompatibleTileMatrixSet()
        // Iterate through each provided tile matrix set
        for (tileMatrixSetId in tileMatrixSetIds) {
            compatibleSet.tileMatrixSetId = tileMatrixSetId
            compatibleSet.tileMatrices.clear()
            var previousHeight = 0
            capabilities.getTileMatrixSet(tileMatrixSetId)?.let {
                // Walk through the associated tile matrices and check for compatibility with WWA tiling scheme
                for (tileMatrix in it.tileMatrices) {
                    // Aspect and symmetry check of current matrix
                    if (2 * tileMatrix.matrixHeight != tileMatrix.matrixWidth) {
                        continue
                        // Quad division check
                    } else if (tileMatrix.matrixWidth % 2 != 0 || tileMatrix.matrixHeight % 2 != 0) {
                        continue
                        // Square image check
                    } else if (tileMatrix.matrixHeight != tileMatrix.matrixWidth) {
                        continue
                        // Minimum row check
                    } else if (tileMatrix.matrixHeight < 2) {
                        continue
                    }

                    // Parse top left corner values
                    val topLeftCornerValue = tileMatrix.topLeftCorner ?.split("\\s+") ?: continue
                    if (topLeftCornerValue.size != 2) {
                        continue
                    }
                    // Convert Values
                    var topLeftCorner: DoubleArray
                    topLeftCorner = try {
                        doubleArrayOf(
                            topLeftCornerValue[0].toDouble(),
                            topLeftCornerValue[1].toDouble()
                        )
                    } catch (e: java.lang.Exception) {
                        Logger.logMessage(
                            Logger.WARN, "LayerFactory", "determineTileSchemeCompatibleTileMatrixSet",
                            "Unable to parse TopLeftCorner values"
                        )
                        continue
                    }

                    // Check top left corner values
                    if (it.supportedCrs.equals("urn:ogc:def:crs:OGC:1.3:CRS84")
                        || it.supportedCrs.equals("http://www.opengis.net/def/crs/OGC/1.3/CRS84")
                    ) {
                        if (Math.abs(topLeftCorner[0] + 180) > 1e-9) {
                            continue
                        } else if (Math.abs(topLeftCorner[1] - 90) > 1e-9) {
                            continue
                        }
                    } else if (it.supportedCrs.equals("urn:ogc:def:crs:EPSG::4326")) {
                        if (Math.abs(topLeftCorner[1] + 180) > 1e-9) {
                            continue
                        } else if (Math.abs(topLeftCorner[0] - 90) > 1e-9) {
                            continue
                        }
                    } else {
                        // The provided list of tile matrix set ids should adhere to either EPGS:4326 or CRS84
                        continue
                    }

                    // Ensure quad division behavior from previous tile matrix and add compatible tile matrix
                    if (previousHeight == 0) {
                        previousHeight = tileMatrix.matrixHeight
                        compatibleSet.tileMatrices.add(tileMatrix.identifier ?: continue)
                    } else if (2 * previousHeight == tileMatrix.matrixHeight) {
                        previousHeight = tileMatrix.matrixHeight
                        compatibleSet.tileMatrices.add(tileMatrix.identifier ?: continue)
                    }
                }
            }
            // Return the first compatible tile matrix set
            if (compatibleSet.tileMatrices.size > 2) {
                return compatibleSet
            }
        }
        return null
    }


    protected open fun createWmtsLayer(
        wmtsLayer: WmtsLayer,
        layer: Layer,
        callback: Callback
    ) {
        val finalLayer = layer as RenderableLayer
        try {
            // Determine if there is a TileMatrixSet which matches our Coordinate System compatibility and tiling scheme
            val compatibleTileMatrixSets: List<String> =
                this.determineCoordSysCompatibleTileMatrixSets(wmtsLayer)
            if (compatibleTileMatrixSets.isEmpty()) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "createWmtsLayer",
                        "Coordinate Systems Not Compatible"
                    )
                )
            }

            // Search the list of coordinate system compatible tile matrix sets for compatible tiling schemes
            val compatibleTileMatrixSet =
                this.determineTileSchemeCompatibleTileMatrixSet(
                    wmtsLayer.getCapabilities() ?: let {
                        throw java.lang.RuntimeException(
                            Logger.makeMessage(
                                "LayerFactory",
                                "createWmtsLayer",
                                "Tile Schemes Not Compatible 2"
                            )
                        )
                    },
                    compatibleTileMatrixSets
                ) ?: throw java.lang.RuntimeException(
                        Logger.makeMessage(
                            "LayerFactory",
                            "createWmtsLayer",
                            "Tile Schemes Not Compatible"
                        )
                    )
            val tileFactory: TileFactory =
                this.createWmtsTileFactory(wmtsLayer, compatibleTileMatrixSet)
                    ?: throw java.lang.RuntimeException(
                        Logger.makeMessage(
                            "LayerFactory",
                            "createWmtsLayer",
                            "Unable to create TileFactory"
                        )
                    )
            val levelSet: LevelSet = this.createWmtsLevelSet(wmtsLayer, compatibleTileMatrixSet)
            val surfaceImage = TiledSurfaceImage()
            surfaceImage.tileFactory = (tileFactory)
            surfaceImage.levelSet = (levelSet)

            // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
            // that the image displays on all WorldWindows the layer may be attached to.
            mainLoopHandler.post {
                finalLayer.addRenderable(surfaceImage)
                callback.creationSucceeded(this@LayerFactory, finalLayer)
                WorldWind.requestRedraw()
            }
        } catch (ex: Throwable) {
            mainLoopHandler.post { callback.creationFailed(this@LayerFactory, finalLayer, ex) }
        }
    }
    protected open fun createWmtsLevelSet(
        wmtsLayer: WmtsLayer,
        compatibleTileMatrixSet: CompatibleTileMatrixSet
    ): LevelSet{
        var boundingBox: Sector? = null
        val wgs84BoundingBox= wmtsLayer.wgs84BoundingBox
        if (wgs84BoundingBox == null) {
            Logger.logMessage(
                Logger.WARN,
                "LayerFactory",
                "createWmtsLevelSet",
                "WGS84BoundingBox not defined for layer: " + wmtsLayer.identifier
            )
        } else {
            boundingBox = wgs84BoundingBox.getSector()
        }
        val tileMatrixSet =
            wmtsLayer.getCapabilities()!!.getTileMatrixSet(compatibleTileMatrixSet.tileMatrixSetId)
                ?: throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "createWmtsLevelSet",
                        "Compatible TileMatrixSet not found for: $compatibleTileMatrixSet"
                    )
                )
        val imageSize: Int = tileMatrixSet.tileMatrices.get(0).tileHeight
        return LevelSet(
            boundingBox!!,
            90.0,
            compatibleTileMatrixSet.tileMatrices.size,
            imageSize,
            imageSize
        )
    }

    protected open fun determineKvpUrl(layer: WmtsLayer): String? {
        val capabilities = layer.getCapabilities()
        val operationsMetadata: OwsOperationsMetadata =
            capabilities?.operationsMetadata ?: return null
        val getTileOperation: OwsOperation = operationsMetadata.getGetTile() ?: return null
        val dcp: List<OwsDcp> = getTileOperation.dcps
        if (dcp.isEmpty()) {
            return null
        }
        val getMethods: List<OwsHttpMethod> = dcp[0].getMethod
        if (getMethods.isEmpty()) {
            return null
        }
        val constraints: List<OwsConstraint> = getMethods[0].constraints
        if (constraints.isEmpty()) {
            return null
        }
        val allowedValues: List<String> =
            constraints[0].allowedValues
        return if (allowedValues.contains("KVP")) {
            getMethods[0].url
        } else {
            null
        }
    }

    protected open fun createWmtsTileFactory(
        wmtsLayer: WmtsLayer,
        compatibleTileMatrixSet: CompatibleTileMatrixSet
    ): TileFactory? {
        // First choice is a ResourceURL
        val resourceUrls = wmtsLayer.resourceUrls
        // Attempt to find a supported image format
        for (resourceUrl in resourceUrls) {
            if (compatibleImageFormats.contains(resourceUrl.format)) {
                val template = resourceUrl.template?.replace(
                    "{TileMatrixSet}",
                    compatibleTileMatrixSet.tileMatrixSetId!!
                ) ?: continue
                return WmtsTileFactory(template, compatibleTileMatrixSet.tileMatrices)
            }
        }

        // Second choice is if the server supports KVP
        val baseUrl= this.determineKvpUrl(wmtsLayer)
        return if (baseUrl != null) {
            var imageFormat: String? = null
            for (compatibleImageFormat in compatibleImageFormats) {
                if (wmtsLayer.formats.contains(compatibleImageFormat)) {
                    imageFormat = compatibleImageFormat
                    break
                }
            }
            if (imageFormat == null) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "getWmtsTileFactory",
                        "Image Formats Not Compatible"
                    )
                )
            }
            val styleIdentifier: String = wmtsLayer.styles.get(0).identifier
                ?: throw java.lang.RuntimeException(
                    Logger.makeMessage("LayerFactory", "getWmtsTileFactory", "No Style Identifier")
                )
            val template: String = this.buildWmtsKvpTemplate(
                baseUrl,
                wmtsLayer.identifier,
                imageFormat,
                styleIdentifier,
                compatibleTileMatrixSet.tileMatrixSetId
            )
            WmtsTileFactory(template, compatibleTileMatrixSet.tileMatrices)
        } else {
            throw java.lang.RuntimeException(
                Logger.makeMessage("LayerFactory", "getWmtsTileFactory", "No KVP Get Support")
            )
        }
    }

    protected open fun buildWmtsKvpTemplate(
        kvpServiceAddress: String,
        layer: String?,
        format: String,
        styleIdentifier: String,
        tileMatrixSet: String?
    ): String {
        val urlTemplate = StringBuilder(kvpServiceAddress)
        var index = urlTemplate.indexOf("?")
        if (index < 0) { // if service address contains no query delimiter
            urlTemplate.append("?") // add one
        } else if (index != urlTemplate.length - 1) { // else if query delimiter not at end of string
            index = urlTemplate.lastIndexOf("&")
            if (index != urlTemplate.length - 1) {
                urlTemplate.append("&") // add a parameter delimiter
            }
        }
        urlTemplate.append("SERVICE=WMTS&")
        urlTemplate.append("REQUEST=GetTile&")
        urlTemplate.append("VERSION=1.0.0&")
        urlTemplate.append("LAYER=").append(layer).append("&")
        urlTemplate.append("STYLE=").append(styleIdentifier).append("&")
        urlTemplate.append("FORMAT=").append(format).append("&")
        urlTemplate.append("TILEMATRIXSET=").append(tileMatrixSet).append("&")
        urlTemplate.append("TILEMATRIX=").append(WmtsTileFactory.TILEMATRIX_TEMPLATE).append("&")
        urlTemplate.append("TILEROW=").append(WmtsTileFactory.TILEROW_TEMPLATE).append("&")
        urlTemplate.append("TILECOL=").append(WmtsTileFactory.TILECOL_TEMPLATE)
        return urlTemplate.toString()
    }


    @Throws(java.lang.Exception::class)
    protected open fun retrieveWmtsCapabilities(serviceAddress: String?): WmtsCapabilities {
        var inputStream: InputStream? = null
        try {
            // Build the appropriate request Uri given the provided service address
            val serviceUri = Uri.parse(serviceAddress).buildUpon()
                .appendQueryParameter("VERSION", "1.0.0")
                .appendQueryParameter("SERVICE", "WMTS")
                .appendQueryParameter("REQUEST", "GetCapabilities")
                .build()

            // Open the connection as an input stream
            val conn = URL(serviceUri.toString()).openConnection()
            conn.connectTimeout = 3000
            conn.readTimeout = 30000
            inputStream = BufferedInputStream(conn.getInputStream())

            // Parse and read the input stream
            return WmtsCapabilities.getCapabilities(inputStream)
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "retrieveWmsCapabilities",
                    "Unable to open connection and read from service address $e"
                )
            )
        } finally {
            WWUtil.closeSilently(inputStream)
        }
    }


    @Throws(Exception::class)
    open fun createFromWmsAsync(
        serviceAddress: String,
        layerNames: List<String>,
        layer: Layer,
        callback: Callback
    ) {
        // Parse and read the WMS Capabilities document at the provided service address
        val wmsCapabilities: WmsCapabilities = this.retrieveWmsCapabilities(serviceAddress)
        val layerCapabilities: MutableList<WmsLayer> =
            ArrayList<WmsLayer>()
        for (layerName in layerNames) {
            val layerCaps: WmsLayer? = wmsCapabilities.getNamedLayer(layerName)
            if (layerCaps != null) {
                layerCapabilities.add(layerCaps)
            }
        }
        if (layerCapabilities.size == 0) {
            throw java.lang.RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createFromWmsAsync",
                    "Provided layers did not match available layers"
                )
            )
        }
        this.createWmsLayer(layerCapabilities, layer, callback)
    }

    open fun createWmsLayer(
        layerCapabilities: List<WmsLayer>,
        layer: Layer,
        callback: Callback
    ) {
        val finalLayer = layer as RenderableLayer
        try {
            val wmsCapabilities = layerCapabilities[0].getCapability()?.getCapabilities()
            // Check if the server supports multiple layer request
            val layerLimit = wmsCapabilities?.service?.layerLimit
            if (layerLimit != null && layerLimit < layerCapabilities.size) {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "createFromWmsAsync",
                        "The number of layers specified exceeds the services limit"
                    )
                )
            }
            val wmsLayerConfig: WmsLayerConfig =
                getLayerConfigFromWmsCapabilities(layerCapabilities)
            val levelSetConfig: LevelSetConfig =
                getLevelSetConfigFromWmsCapabilities(layerCapabilities)

            // Collect WMS Layer Titles to set the Layer Display Name
            var sb: StringBuilder? = null
            for (layerCapability in layerCapabilities) {
                if (sb == null) {
                    sb = StringBuilder()
                    layerCapability.title?.let {
                        sb.append(it)
                    }
                } else {
                    layerCapability.title?.let {
                        sb.append(",").append(it)
                    }
                }
            }
            layer.displayName = (sb.toString())
            val surfaceImage = TiledSurfaceImage()
            surfaceImage.tileFactory = (WmsTileFactory(wmsLayerConfig))
            surfaceImage.levelSet = (LevelSet(levelSetConfig))

            // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
            // that the image displays on all WorldWindows the layer may be attached to.
            mainLoopHandler.post {
                finalLayer.addRenderable(surfaceImage)
                callback.creationSucceeded(this@LayerFactory, finalLayer)
                WorldWind.requestRedraw()
            }
        } catch (ex: Throwable) {
            mainLoopHandler.post { callback.creationFailed(this@LayerFactory, finalLayer, ex) }
        }
    }

    open fun getLayerConfigFromWmsCapabilities(
        wmsLayers: List<WmsLayer>
    ): WmsLayerConfig {
        // Construct the WmsTiledImage renderable from the WMS Capabilities properties
        val wmsLayerConfig = WmsLayerConfig()
        val wmsCapabilities = wmsLayers[0].getCapability()?.getCapabilities()
        val version = wmsCapabilities?.version
        if (version == "1.3.0") {
            wmsLayerConfig.wmsVersion = version
        } else if (version == "1.1.1") {
            wmsLayerConfig.wmsVersion = version
        } else {
            throw java.lang.RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "getLayerConfigFromWmsCapabilities",
                    "Version not compatible"
                )
            )
        }
        val requestUrl = wmsCapabilities.capability?.request?.getMap?.getUrl
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
        var matchingCoordinateSystems: MutableSet<String>? = null
        for (wmsLayer in wmsLayers) {
            val layerName = wmsLayer.name
            if (sb == null) {
                sb = StringBuilder()
                layerName?.let {
                    sb.append(it)
                }
            } else {
                sb.append(",").append(layerName)
            }
            val wmsLayerCoordinateSystems = wmsLayer.getReferenceSystems()
            if (matchingCoordinateSystems == null) {
                matchingCoordinateSystems = HashSet()
                wmsLayerCoordinateSystems?.let {
                    matchingCoordinateSystems.addAll(it)
                }
            } else {
                wmsLayerCoordinateSystems?.let {
                    matchingCoordinateSystems.retainAll(it)
                }
            }
        }
        wmsLayerConfig.layerNames = sb.toString()
        if (matchingCoordinateSystems != null) {
            if (matchingCoordinateSystems.contains("EPSG:4326")) {
                wmsLayerConfig.coordinateSystem = "EPSG:4326"
            } else if (matchingCoordinateSystems.contains("CRS:84")) {
                wmsLayerConfig.coordinateSystem = "CRS:84"
            } else {
                throw java.lang.RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "getLayerConfigFromWmsCapabilities",
                        "Coordinate systems not compatible"
                    )
                )
            }
        }

        // Negotiate Image Formats
        wmsCapabilities.capability?.request?.getMap?.formats?.let {
            for (compatibleImageFormat in compatibleImageFormats) {
                if (it.contains(compatibleImageFormat)) {
                    wmsLayerConfig.imageFormat = compatibleImageFormat
                    break
                }
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

    open fun getLevelSetConfigFromWmsCapabilities(
        layerCapabilities: List<WmsLayer>
    ): LevelSetConfig {
        val levelSetConfig = LevelSetConfig()
        var minScaleDenominator = Double.MAX_VALUE
        var minScaleHint = Double.MAX_VALUE
        val sector = Sector()
        for (layerCapability in layerCapabilities) {
            val layerMinScaleDenominator = layerCapability.minScaleDenominator
            if (layerMinScaleDenominator != null) {
                minScaleDenominator = Math.min(minScaleDenominator, layerMinScaleDenominator)
            }
            val layerMinScaleHint: Double? = layerCapability.scaleHint?.min
            if (layerMinScaleHint != null) {
                minScaleHint = Math.min(minScaleHint, layerMinScaleHint)
            }
            val layerSector: Sector? = layerCapability.getGeographicBoundingBox()
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
        if (minScaleDenominator != Double.MAX_VALUE) {
            // WMS 1.3.0 scale configuration. Based on the WMS 1.3.0 spec page 28. The hard coded value 0.00028 is
            // detailed in the spec as the common pixel size of 0.28mm x 0.28mm. Configures the maximum level not to
            // exceed the specified min scale denominator.
            val minMetersPerPixel = minScaleDenominator * 0.00028
            val minRadiansPerPixel =
                minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
            levelSetConfig.numLevels = levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel)
        } else if (minScaleHint != Double.MAX_VALUE) {
            // WMS 1.1.1 scale configuration, where ScaleHint indicates approximate resolution in ground distance
            // meters. Configures the maximum level not to exceed the specified min scale denominator.
            val minMetersPerPixel = minScaleHint
            val minRadiansPerPixel =
                minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
            levelSetConfig.numLevels = levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel)
        } else {
            // Default scale configuration when no minimum scale denominator or scale hint is provided.
            levelSetConfig.numLevels = DEFAULT_WMS_NUM_LEVELS
        }
        return levelSetConfig
    }

    @Throws(java.lang.Exception::class)
    open fun retrieveWmsCapabilities(
        serviceAddress: String
    ): WmsCapabilities {
        var inputStream: InputStream? = null
        var wmsCapabilities: WmsCapabilities? = null
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
            return WmsCapabilities.getCapabilities(inputStream)
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "retrieveWmsCapabilities",
                    "Unable to open connection and read from service address"
                )
            )
        } finally {
            WWUtil.closeSilently(inputStream)
        }
    }

    class GeoPackageAsyncTask(
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

    class WmsAsyncTask(
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

    class WmtsAsyncTask(
        private var factory: LayerFactory,
        private var serviceAddress: String,
        private var layerName: String,
        private var layer: Layer,
        private var callback: Callback
    ) :
        Runnable {
        override fun run() {
            try {
                factory.createFromWmtsAsync(
                    serviceAddress,
                    layerName,
                    layer,
                    callback
                )
            } catch (ex: Throwable) {
                factory.mainLoopHandler.post { callback.creationFailed(factory, layer, ex) }
            }
        }
    }

    class CompatibleTileMatrixSet {
        var tileMatrixSetId: String? = null
        var tileMatrices: MutableList<String> = ArrayList()
    }
}