package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.WorldWind

class GlobeWgs84 : BasicGlobe(WorldWind.WGS84_SEMI_MAJOR_AXIS, WorldWind.WGS84_INVERSE_FLATTENING,  ProjectionWgs84()) {
    
}