package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.DrawableSurfaceTexture
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.shape.Movable
import com.atom.wyz.worldwind.util.Logger
import java.util.*

open class SurfaceImage : AbstractRenderable, Movable {
    var sector: Sector = Sector()
        set(value) {
            field.set(value)
        }
    var imageSource: ImageSource? = null
    var imageOptions: ImageOptions? = null

    constructor() : super("Surface Image")

    constructor(sector: Sector?, imageSource: ImageSource) : super("Surface Image") {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector")
            )
        }
        this.sector.set(sector)
        this.imageSource = imageSource
    }

    override fun doRender(rc: RenderContext) {
        if (sector.isEmpty()) {
            return
        }
        if (rc.terrain == null || !rc.terrain!!.sector.intersects(sector)) {
            return  // nothing to render on
        }
        var texture: GpuTexture? = rc.getTexture(imageSource!!)
        if (texture == null) {
            texture = rc.retrieveTexture(imageSource , imageOptions)
        }
        if (texture == null) {
            return
        }
        val program = this.getShaderProgram(rc)
        val drawable = DrawableSurfaceTexture.obtain(rc.getDrawablePool(DrawableSurfaceTexture::class.java))
            .set(program, sector, texture, texture.texCoordTransform)
        rc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)

        // Enqueue a picked object that associates the drawable surface texture with this surface image.
        if (rc.pickMode) {
            rc.pickedObjects?.let {
                val terrainObject = it.terrainPickedObject()
                terrainObject?.position?.let {
                    val pickedObject = PickedObject.fromRenderable(
                        this, terrainObject.position, rc.currentLayer, rc.nextPickedObjectId()
                    )
                    pickedObject?.let {
                        PickedObject.identifierToUniqueColor(it.identifier, drawable.color)
                    }
                    rc.offerPickedObject(pickedObject)
                }
            }
        }
    }

    protected fun getShaderProgram(dc: RenderContext): SurfaceTextureProgram? {
        var program: SurfaceTextureProgram? = dc.getProgram(SurfaceTextureProgram.KEY) as SurfaceTextureProgram?
        if (program == null) {
            program = dc.putProgram(
                SurfaceTextureProgram.KEY,
                SurfaceTextureProgram(dc.resources!!)
            ) as SurfaceTextureProgram?
        }
        return program
    }

    override fun getReferencePosition(): Position? {
        return Position(sector.centroidLatitude(), sector.centroidLongitude(), 0.0)
    }

    override fun moveTo(globe: Globe, position: Position?) {
        val oldRef = getReferencePosition() ?: return

        val swCorner = Location(sector.minLatitude, sector.minLongitude)
        val nwCorner = Location(sector.maxLatitude, sector.minLongitude)
        val seCorner = Location(sector.minLatitude, sector.maxLongitude)
        val neCorner = Location(sector.maxLatitude, sector.maxLongitude)

        val EAST = 90.0
        val WEST = -90.0
        val NORTH = 0.0
        val SOUTH = 180.0

        // Determine the delta from the reference point to sector's anchor (SW corner)
        val azimuthDegrees = oldRef.greatCircleAzimuth(swCorner)
        val distanceRadians = oldRef.greatCircleDistance(swCorner)
        // Determine the width and height of the sector
        // Determine the width and height of the sector
        val widthRadians: Double = swCorner.rhumbDistance(seCorner)
        val heightRadians: Double = swCorner.rhumbDistance(nwCorner)
        val dLat: Double = nwCorner.latitude - swCorner.latitude

        // Compute a new position for the SW corner
        position!!.greatCircleLocation(azimuthDegrees, distanceRadians, swCorner)

        // If the dragged image would span the pole then constrain to the pole
        if (swCorner.latitude + dLat > 90) {
            nwCorner.set(90.0, swCorner.longitude)
            //swCorner.set(nwCorner.latitude - dLat, swCorner.longitude);
            nwCorner.rhumbLocation(SOUTH, heightRadians, swCorner)
        } else { // Compute the NW corner with the original height
            nwCorner.set(swCorner.latitude + dLat, swCorner.longitude)
            swCorner.rhumbLocation(NORTH, heightRadians, nwCorner)
        }

        // Compute the SE corner, using the original width
        swCorner.rhumbLocation(EAST, widthRadians, seCorner)


        // If the dragged image would span the dateline then snap the image the other side
        if (Location.locationsCrossAntimeridian(
                Arrays.asList<Location>(
                    swCorner, seCorner
                )
            )
        ) { // TODO: create JIRA issue regarding Sector Anti-meridian limitation
            // There's presently no support for placing SurfaceImages crossing the Anti-meridian
            // Snap the image to the other side of the date line
            val dragAzimuth = oldRef.greatCircleAzimuth(position)
            if (dragAzimuth < 0) { // Set the East edge of the sector to the dateline
                seCorner.set(seCorner.latitude, 180.0)
                seCorner.rhumbLocation(WEST, widthRadians, swCorner)
            } else { // Set the West edge of the sector to the dateline
                swCorner.set(swCorner.latitude, -180.0)
                swCorner.rhumbLocation(EAST, widthRadians, seCorner)
            }
        }
        // Compute the delta lon values from the new SW position
        val dLon: Double = seCorner.longitude - swCorner.longitude

        // Update the image's sector to move the image
        if (dLat > 0 && dLon > 0) {
            sector[swCorner.latitude, swCorner.longitude, dLat] = dLon
        }
    }

}