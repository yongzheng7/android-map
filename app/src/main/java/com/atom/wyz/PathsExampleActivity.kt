package com.atom.wyz

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.atom.map.WorldWind
import com.atom.map.geom.Position
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.render.attribute.ShapeAttributes
import com.atom.map.layer.render.shape.Path
import com.atom.map.util.Logger
import com.atom.map.util.WWUtil
import com.atom.wyz.base.BasicGlobeActivity
import com.atom.wyz.worldwind.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class PathsExampleActivity : BasicGlobeActivity()  , Handler.Callback  {
    protected class Airport {
        var pos: Position = Position()
        var nam: String? = null
        var iko: String? = null
        var na3: String? = null
        var use: String? = null
    }

    protected var airportTable = ArrayList<Airport>()

    protected var airportIkoIndex = HashMap<String, Airport>()

    protected var flightPathLayer: RenderableLayer = RenderableLayer()

    protected var handler = Handler(this)

    protected var animationAmount = 0.0

    protected var animationIncrement = 0.01 // increment 1% each iteration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch {
            Log.e("PathsExampleActivity" , " 1 ")
            readAirportTable()
            Log.e("PathsExampleActivity" , " 2 ")

            populateFlightPaths()
            Log.e("PathsExampleActivity" , " 3 ")

            handler.sendEmptyMessage(0 /*what*/)
        }
        this.wwd.layers.addLayer(flightPathLayer)
    }

    override fun onPause() {
        super.onPause()
        // Stop the animation when this activity is paused.
        handler.removeMessages(0 /*what*/)
    }

    override fun onResume() {
        super.onResume()
        // Start or resume the animation.
        handler.sendEmptyMessage(0 /*what*/)
    }

    override fun handleMessage(msg: Message): Boolean {

        if (animationAmount < 1) { // Increment the animation amount.
            Log.e("PathsExampleActivity" , " handleMessage ")

            animationAmount += animationIncrement
            val len = flightPathLayer.count()
            Log.e("PathsExampleActivity" , " handleMessage len "+len)
            for (idx in 0 until  len) {

                val path= flightPathLayer.getRenderable(idx) as Path? ?: continue
                Log.e("PathsExampleActivity" , "path $idx")

                val dept = path.getUserProperty("dept") as Airport? ?: continue
                Log.e("PathsExampleActivity" , "  dept ")

                val dest = path.getUserProperty("dest") as Airport? ?: continue
                Log.e("PathsExampleActivity" , "  dest ")

                Log.e("PathsExampleActivity" , " handleMessage idx "+idx)
                val nextPos = dept.pos.interpolateAlongPath( WorldWind.GREAT_CIRCLE, animationAmount,dest.pos, Position())
                val dist: Double = dept.pos.greatCircleDistance(dest.pos) * this.wwd.globe.getEquatorialRadius()
                val altCurve = (1 - animationAmount) * animationAmount * 4
                nextPos!!.altitude = altCurve * dist * 0.1
                val positions= path.positions
                positions.add(nextPos)
                path.positions = positions
            }
            // Redraw the World Window to display the changes.
            this.wwd.requestRedraw()
            // Continue the animation after a delay.
            Log.e("PathsExampleActivity" , " handleMessage 2")
            handler.sendEmptyMessageDelayed(0 /*what*/, 1000)
        }

        return false
    }

    protected fun readAirportTable() {
        var reader: BufferedReader? = null
        try {
            val `in` = this.resources.openRawResource(R.raw.world_apts)
            reader = BufferedReader(InputStreamReader(`in`))
            // The first line is the CSV header:
            //  LAT,LON,ALT,NAM,IKO,NA3,USE,USEdesc
            var line = reader.readLine()
            val headers =
                Arrays.asList(*line.split(",").toTypedArray())
            val LAT = headers.indexOf("LAT")
            val LON = headers.indexOf("LON")
            val ALT = headers.indexOf("ALT")
            val NAM = headers.indexOf("NAM")
            val IKO = headers.indexOf("IKO")
            val NA3 = headers.indexOf("NA3")
            val USE = headers.indexOf("USE")
            // Read the remaining lines
            while (reader.readLine().also { line = it } != null) {
                val fields = line.split(",").toTypedArray()
                val apt = Airport()
                apt.pos.latitude = fields[LAT].toDouble()
                apt.pos.longitude = fields[LON].toDouble()
                apt.pos.altitude = fields[ALT].toDouble()
                apt.nam = fields[NAM]
                apt.iko = fields[IKO]
                apt.na3 = fields[NA3]
                apt.use = fields[USE]
                airportTable.add(apt)
                airportIkoIndex.put(apt.iko!! , apt)
            }
        } catch (e: IOException) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database")
        } finally {
            WWUtil.closeSilently(reader)
        }
    }

    protected fun populateFlightPaths() {
        val attrs =
            ShapeAttributes.defaults()
        attrs.interiorColor.set(0.8f, 0.8f, 1.0f, 0.8f)
        attrs.outlineColor.set(0.0f, 0.0f,  0.0f, 1.0f)
        val dept = airportIkoIndex["KSEA"]
        for (dest in airportTable) {
            if (dest == dept) {
                continue  // the destination and departure must be different
            }
            if (dest.iko!!.length != 4) {
                continue  // the destination must be a major airfield
            }
            if (!dest.na3!!.startsWith("US")) {
                continue  // the destination must be in the United States
            }
            if (dest.use != "49") {
                continue  // the destination must a Civilian/Public airport
            }
            val positions: MutableList<Position> = ArrayList()
            positions.add(dept!!.pos)
            val path =
                Path(positions, attrs)
            path.putUserProperty("dept", dept)
            path.putUserProperty("dest", dest)
            flightPathLayer.addRenderable(path)
        }
    }

}