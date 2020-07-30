/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package com.atom.wyz.worldwind.app;

import com.atom.wyz.worldwind.WorldWind;
import com.atom.wyz.worldwind.WorldWindow;
import com.atom.wyz.worldwind.geom.Position;
import com.atom.wyz.worldwind.layer.RenderableLayer;
import com.atom.wyz.worldwind.shape.Path;
import com.atom.wyz.worldwind.shape.Path2;

import java.util.Arrays;
import java.util.List;

public class Paths_2_Activity extends BasicGlobeActivity {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a set of Path shapes
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();
        // Create a layer to display the tutorial paths.
        RenderableLayer layer = new RenderableLayer();
        wwd.getLayers().addLayer(layer);

        // Create a basic path with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        List<Position> positions = Arrays.asList(
            Position.Companion.fromDegrees(50, -180, 1e5),
            Position.Companion.fromDegrees(30, -100, 1e6),
            Position.Companion.fromDegrees(50, -40, 1e5)
        );
        Path2 path = new Path2(positions);
        layer.addRenderable(path);

        // Create a terrain following path with the default attributes, and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.Companion.fromDegrees(40, -180, 0),
            Position.Companion.fromDegrees(20, -100, 0),
            Position.Companion.fromDegrees(40, -40, 0)
        );
        path = new Path2(positions);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the path vertices to the ground
        path.setFollowTerrain(true); // follow the ground between path vertices
        layer.addRenderable(path);


        return wwd;
    }
}
