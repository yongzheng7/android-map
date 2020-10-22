package com.atom.wyz.worldwind

import com.atom.map.util.Logger
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.vividsolutions.jts.geom.*
import com.vividsolutions.jts.io.geojson.GeoJsonReader
import com.vividsolutions.jts.io.geojson.GeoJsonWriter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


@RunWith(PowerMockRunner::class)
@PrepareForTest(Logger::class)
class PointJson {
    @Before
    @Throws(Exception::class)
    fun setUp() {
        PowerMockito.mockStatic(Logger::class.java)
    }

    @Test
    fun createPoint_test() {
        val latitude: Double = 110.123
        val longitude: Double = 30.123
        val altitude: Double = 10000.123
        val factory = GeometryFactory(PrecisionModel(), 4326)
        val createPoint = factory.createPoint(Coordinate(longitude, latitude, altitude))
        println(createPoint)
        val objectMapper = ObjectMapper()
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        objectMapper.dateFormat = format
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        val module = SimpleModule()
        val geoJsonWriter = GeoJsonWriter(10)
        val geoJsonReader = GeoJsonReader(factory)
        module.addSerializer(Geometry::class.java, JtsGeometrySerializer<Geometry>(geoJsonWriter))
        module.addDeserializer(Geometry::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(Point::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(MultiPoint::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(LineString::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(MultiLineString::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(Polygon::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(MultiPolygon::class.java, JtsGeometryDeserializer(geoJsonReader))
        module.addDeserializer(
            GeometryCollection::class.java,
            JtsGeometryDeserializer(geoJsonReader)
        )
        objectMapper.registerModule(module)
        val serialized: String = objectMapper.writeValueAsString(createPoint)
        println(serialized)
        val readValue = objectMapper.readValue(serialized, Point::class.java)
        println(readValue)
    }


    class JtsGeometrySerializer<T : Geometry> : JsonSerializer<T> {
        private val geometryJSON: GeoJsonWriter

        constructor() : this(GeoJsonWriter(10))
        constructor(geometryJSON: GeoJsonWriter) {
            this.geometryJSON = geometryJSON
        }

        @Throws(IOException::class)
        override fun serialize(geometry: T, gen: JsonGenerator, serializers: SerializerProvider) {
            val writer = StringWriter()
            geometryJSON.write(geometry, writer)
            val jsonValue = writer.toString()
            gen.writeString(jsonValue)
        }
    }

    class JtsGeometryDeserializer<T : Geometry> : JsonDeserializer<T> {
        private var geometryJSON: GeoJsonReader

        constructor() : this(GeoJsonReader())
        constructor(geometryJSON: GeoJsonReader) {
            this.geometryJSON = geometryJSON
        }

        @Throws(IOException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
            val text: String?
            text = if (p.currentToken == JsonToken.START_OBJECT) {
                val mapper = p.codec as ObjectMapper
                val jsonNode = mapper.readTree<JsonNode>(p)
                jsonNode.toString()
            } else {
                p.text
            }
            if (text == null || text.isEmpty()) return null
            val reader: Reader = StringReader(text)
            val geometry: Geometry = geometryJSON.read(reader)
            return geometry as T
        }
    }
}