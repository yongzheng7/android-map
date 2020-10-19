package com.atom.map.ogc.gpkg

class GpkgSpatialReferenceSystem : GpkgEntry() {
     var srsName: String? = null

     var srsId = 0

     var organization: String? = null

     var organizationCoordSysId = 0

     var definition: String? = null

     var description: String? = null
}