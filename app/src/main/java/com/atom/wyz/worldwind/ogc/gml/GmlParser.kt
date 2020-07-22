package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.xml.XmlModelParser

class GmlParser : XmlModelParser() {

    protected var gml32Namespace = "http://www.opengis.net/gml/3.2"

    init {
        registerGmlModels(gml32Namespace)
    }

    protected fun registerGmlModels(namespace: String?) {
        registerXmlModel(namespace, "AbstractFeature", GmlAbstractFeature::class.java)
        registerXmlModel(namespace, "AbstractGeometry", GmlAbstractGeometry::class.java)
        registerXmlModel(namespace, "AbstractGML", GmlAbstractGml::class.java)
        registerTxtModel(namespace, "axisLabels")
        registerTxtModel(namespace, "axisName")
        registerXmlModel(namespace, "boundedBy", GmlBoundingShape::class.java)
        registerTxtModel(namespace, "dimension")
        registerXmlModel(namespace, "domainSet", GmlDomainSet::class.java)
        registerXmlModel(namespace, "Envelope", GmlEnvelope::class.java)
        registerXmlModel(namespace, "_GeometricPrimitive", GmlAbstractGeometricPrimitive::class.java)
        registerTxtModel(namespace, "gid")
        registerXmlModel(namespace, "Grid", GmlGrid::class.java)
        registerXmlModel(namespace, "GridEnvelope", GmlGridEnvelope::class.java)
        registerXmlModel(namespace, "high", GmlIntegerList::class.java)
        registerXmlModel(namespace, "limits", GmlGridLimits::class.java)
        registerXmlModel(namespace, "low", GmlIntegerList::class.java)
        registerXmlModel(namespace, "lowerCorner", GmlDirectPosition::class.java)
        registerTxtModel(namespace, "nilReason")
        registerXmlModel(namespace, "offsetVector", GmlVector::class.java)
        registerXmlModel(namespace, "origin", GmlPointProperty::class.java)
        registerXmlModel(namespace, "Point", GmlPoint::class.java)
        registerXmlModel(namespace, "pos", GmlDirectPosition::class.java)
        registerXmlModel(namespace, "RectifiedGrid", GmlRectifiedGrid::class.java)
        registerTxtModel(namespace, "srsName")
        registerTxtModel(namespace, "srsDimension")
        registerTxtModel(namespace, "uomLabels")
        registerXmlModel(namespace, "upperCorner", GmlDirectPosition::class.java)
    }
}