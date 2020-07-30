/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package com.atom.wyz.worldwind.shape;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.List;

import com.atom.wyz.worldwind.WorldWind;
import com.atom.wyz.worldwind.attribute.ShapeAttributes;
import com.atom.wyz.worldwind.context.RenderContext;
import com.atom.wyz.worldwind.draw.DrawShapeState;
import com.atom.wyz.worldwind.draw.Drawable;
import com.atom.wyz.worldwind.draw.DrawableShape;
import com.atom.wyz.worldwind.draw.DrawableSurfaceShapeJava;
import com.atom.wyz.worldwind.geom.Location;
import com.atom.wyz.worldwind.geom.Matrix3;
import com.atom.wyz.worldwind.geom.Position;
import com.atom.wyz.worldwind.geom.Vec3;


import com.atom.wyz.worldwind.render.ImageOptions;


import com.atom.wyz.worldwind.shader.BasicProgram;
import com.atom.wyz.worldwind.shader.BufferObject;
import com.atom.wyz.worldwind.shader.GpuTexture;
import com.atom.wyz.worldwind.util.Logger;
import com.atom.wyz.worldwind.util.SimpleFloatArray;
import com.atom.wyz.worldwind.util.SimpleShortArray;
import com.atom.wyz.worldwind.util.pool.Pool;

public class Path2 extends AbstractShape {

    protected static final int VERTEX_STRIDE = 4;

    protected static final ImageOptions defaultOutlineImageOptions = new ImageOptions(WorldWind.RGBA_8888);

    protected List<Position> positions = Collections.emptyList();

    protected boolean extrude;

    protected boolean followTerrain;

    protected SimpleFloatArray vertexArray = new SimpleFloatArray(0);

    protected SimpleShortArray interiorElements = new SimpleShortArray(0);

    protected SimpleShortArray outlineElements = new SimpleShortArray(0);

    protected SimpleShortArray verticalElements = new SimpleShortArray(0);

    protected Object vertexBufferKey = nextCacheKey();

    protected Object elementBufferKey = nextCacheKey();

    protected Vec3 vertexOrigin = new Vec3();

    protected boolean isSurfaceShape;

    protected double texCoord1d;

    private Vec3 point = new Vec3();

    private Vec3 prevPoint = new Vec3();

    private Matrix3 texCoordMatrix = new Matrix3();

    private Location intermediateLocation = new Location();

    protected static Object nextCacheKey() {
        return new Object();
    }

    static {
        defaultOutlineImageOptions.setResamplingMode(WorldWind.NEAREST_NEIGHBOR);
        defaultOutlineImageOptions.setWrapMode( WorldWind.REPEAT);
    }

    public Path2() {
    }

    public Path2(ShapeAttributes attributes) {
        super(attributes);
    }

    public Path2(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.Companion.logMessage(Logger.ERROR, "Path", "constructor", "missingList"));
        }

        this.positions = positions;
    }

    public Path2(List<Position> positions, ShapeAttributes attributes) {
        super(attributes);

        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.Companion.logMessage(Logger.ERROR, "Path", "constructor", "missingList"));
        }

        this.positions = positions;
    }

    public List<Position> getPositions() {
        return this.positions;
    }

    public void setPositions(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.Companion.logMessage(Logger.ERROR, "Path", "setPositions", "missingList"));
        }

        this.positions = positions;
        this.reset();
    }

    public boolean isExtrude() {
        return this.extrude;
    }

    public void setExtrude(boolean extrude) {
        this.extrude = extrude;
        this.reset();
    }

    public boolean isFollowTerrain() {
        return this.followTerrain;
    }

    public void setFollowTerrain(boolean followTerrain) {
        this.followTerrain = followTerrain;
        this.reset();
    }

    protected void reset() {
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.positions.isEmpty()) {
            return; // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc);
            this.vertexBufferKey = nextCacheKey();
            this.elementBufferKey = nextCacheKey();
        }

        // Obtain a drawable form the render context pool, and compute distance to the render camera.
        Drawable drawable;
        DrawShapeState drawState;
        double cameraDistance;
        if (this.isSurfaceShape) {
            Pool<DrawableSurfaceShapeJava> pool = rc.getDrawablePool(DrawableSurfaceShapeJava.class);
            drawable = DrawableSurfaceShapeJava.obtain(pool);
            drawState = ((DrawableSurfaceShapeJava) drawable).drawState;
            cameraDistance = this.cameraDistanceGeographic(rc, this.getBoundingSector());
            ((DrawableSurfaceShapeJava) drawable).sector.set(this.getBoundingSector());
        } else {
            Pool<DrawableShape> pool = rc.getDrawablePool(DrawableShape.class);
            drawable = DrawableShape.Companion.obtain(pool);
            drawState = ((DrawableShape) drawable).getDrawState();
            cameraDistance = this.cameraDistanceCartesian(rc, this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE, this.vertexOrigin);
        }

        // Use the basic GLSL program to draw the shape.
        drawState.setProgram((BasicProgram)  rc.getProgram(BasicProgram.Companion.getKEY()));
        if (drawState.getProgram() == null) {
            drawState.setProgram((BasicProgram)  rc.putProgram(BasicProgram.Companion.getKEY(), new BasicProgram(rc.resources))); }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.setVertexBuffer(rc.getBufferObject(this.vertexBufferKey));
        if (drawState.getVertexBuffer() == null) {
            int size = this.vertexArray.size() * 4;
            FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(this.vertexArray.array(), 0, this.vertexArray.size());
            drawState.setVertexBuffer(new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind()));
            rc.putBufferObject(this.vertexBufferKey, drawState.getVertexBuffer());
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawState.setElementBuffer(rc.getBufferObject(this.elementBufferKey));
        if (drawState.getElementBuffer() == null) {
            int size = (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2) + (this.verticalElements.size() * 2);
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.interiorElements.array(), 0, this.interiorElements.size());
            buffer.put(this.outlineElements.array(), 0, this.outlineElements.size());
            buffer.put(this.verticalElements.array(), 0, this.verticalElements.size());
            drawState.setElementBuffer(new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind()));
            rc.putBufferObject(this.elementBufferKey, drawState.getElementBuffer());
        }

        // Configure the drawable's vertex texture coordinate attribute.
        drawState.getTexCoordAttrib().setSize(1 /*size*/);
        drawState.getTexCoordAttrib().setOffset( 12 /*stride in bytes*/);

        // Configure the drawable to use the outline texture when drawing the outline.
        if (this.getActiveAttributes().getDrawOutline() && this.getActiveAttributes().getOutlineImageSource() != null) {
            GpuTexture texture = rc.getTexture(this.getActiveAttributes().getOutlineImageSource());
            if (texture == null) {
                texture = rc.retrieveTexture(this.getActiveAttributes().getOutlineImageSource(), defaultOutlineImageOptions);
            }
            if (texture != null) {
                double metersPerPixel = rc.pixelSizeAtDistance(cameraDistance);
                this.computeRepeatingTexCoordTransform(texture, metersPerPixel, this.texCoordMatrix);
                drawState.setTexture(texture);
                drawState.setTexCoordMatrix(texCoordMatrix);
            }
        }

        // Configure the drawable to display the shape's outline. Increase surface shape line widths by 1/2 pixel. Lines
        // drawn indirectly offscreen framebuffer appear thinner when sampled as a texture.
        if (this.getActiveAttributes().getDrawOutline()) {
            drawState.color(rc.getPickMode() ? this.getPickColor() : this.getActiveAttributes().getOutlineColor());
            drawState.lineWidth(this.isSurfaceShape ? this.getActiveAttributes().getOutlineWidth() + 0.5f : this.getActiveAttributes().getOutlineWidth());
            drawState.drawElements(GLES20.GL_LINE_STRIP, this.outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2);
        }

        // Disable texturing for the remaining drawable primitives.
        drawState.setTexture(null);

        // Configure the drawable to display the shape's extruded verticals.
        if (this.getActiveAttributes().getDrawOutline() && this.getActiveAttributes().getDrawVerticals() && this.extrude) {
            drawState.color(rc.getPickMode() ? this.getPickColor() : this.getActiveAttributes().getOutlineColor());
            drawState.lineWidth(this.getActiveAttributes().getOutlineWidth());
            drawState.drawElements(GLES20.GL_LINES, this.verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2));
        }

        // Configure the drawable to display the shape's extruded interior.
        if (this.getActiveAttributes().getDrawInterior() && this.extrude) {
            drawState.color(rc.getPickMode() ? this.getPickColor() : this.getActiveAttributes().getInteriorColor());
            drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, this.interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0);
        }

        // Configure the drawable according to the shape's attributes.
        drawState.getVertexOrigin().set(this.vertexOrigin);
        drawState.setVertexStride(VERTEX_STRIDE * 4);  // stride in bytes
        drawState.setEnableCullFace(false);
        drawState.setEnableDepthTest(this.getActiveAttributes().getDepthTest());

        // Enqueue the drawable for processing on the OpenGL thread.
        if (this.isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0 /*zOrder*/);
        } else {
            rc.offerShapeDrawable(drawable, cameraDistance);
        }
    }

    protected boolean mustAssembleGeometry(RenderContext rc) {
        return this.vertexArray.size() == 0;
    }

    protected void assembleGeometry(RenderContext rc) {
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        this.isSurfaceShape = (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND) && this.followTerrain;

        // Clear the shape's vertex array and element arrays. These arrays will accumulate values as the shapes's
        // geometry is assembled.
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();

        // Add the first vertex.
        Position begin = this.positions.get(0);
        this.addVertex(rc, begin.getLatitude(), begin.getLongitude(), begin.getAltitude(), false /*intermediate*/);

        // Add the remaining vertices, inserting vertices along each edge as indicated by the path's properties.
        for (int idx = 1, len = this.positions.size(); idx < len; idx++) {
            Position end = this.positions.get(idx);
            this.addIntermediateVertices(rc, begin, end);
            this.addVertex(rc, end.getLatitude(), end.getLongitude(), end.getAltitude(), false /*intermediate*/);
            begin = end;
        }

        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
        if (this.isSurfaceShape) {
            this.getBoundingSector().setEmpty();
            this.getBoundingSector().union(this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE);
            this.getBoundingSector().translate(this.vertexOrigin.getY() /*latitude*/, this.vertexOrigin.getX() /*longitude*/);
            this.getBoundingBox().setToUnitBox(); // Surface/geographic shape bounding box is unused
        } else {
            this.getBoundingBox().setToPoints(this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE);
            this.getBoundingBox().translate(this.vertexOrigin.getX(), this.vertexOrigin.getY(), this.vertexOrigin.getZ());
            this.getBoundingSector().setEmpty(); // Cartesian shape bounding sector is unused
        }
    }

    protected void addIntermediateVertices(RenderContext rc, Position begin, Position end) {
        if (this.getPathType() == WorldWind.LINEAR) {
            return; // suppress intermediate vertices when the path type is linear
        }

        if (this.getMaximumIntermediatePoints() <= 0) {
            return; // suppress intermediate vertices when configured to do so
        }

        double azimuth = 0;
        double length = 0;
        if (this.getPathType()  == WorldWind.GREAT_CIRCLE) {
            azimuth = begin.greatCircleAzimuth(end);
            length = begin.greatCircleDistance(end);
        } else if (this.getPathType()  == WorldWind.RHUMB_LINE) {
            azimuth = begin.rhumbAzimuth(end);
            length = begin.rhumbDistance(end);
        }

        if (length < NEAR_ZERO_THRESHOLD) {
            return; // suppress intermediate vertices when the edge length less than a millimeter (on Earth)
        }

        int numSubsegments = this.getMaximumIntermediatePoints() + 1;
        double deltaDist = length / numSubsegments;
        double deltaAlt = (end.getAltitude() - begin.getAltitude()) / numSubsegments;
        double dist = deltaDist;
        double alt = begin.getAltitude() + deltaAlt;

        for (int idx = 1; idx < numSubsegments; idx++) {
            Location loc = this.intermediateLocation;

            if (this.getPathType() == WorldWind.GREAT_CIRCLE) {
                begin.greatCircleLocation(azimuth, dist, loc);
            } else if (this.getPathType() == WorldWind.RHUMB_LINE) {
                begin.rhumbLocation(azimuth, dist, loc);
            }

            this.addVertex(rc, loc.getLatitude(), loc.getLongitude(), alt, true /*intermediate*/);
            dist += deltaDist;
            alt += deltaAlt;
        }
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude, boolean intermediate) {
        int vertex = this.vertexArray.size() / VERTEX_STRIDE;
        Vec3 point = rc.geographicToCartesian(latitude, longitude, altitude, this.getAltitudeMode(), this.point);

        if (vertex == 0) {
            if (this.isSurfaceShape) {
                this.vertexOrigin.set(longitude, latitude, altitude);
            } else {
                this.vertexOrigin.set(point);
            }
            this.texCoord1d = 0;
            this.prevPoint.set(point);
        } else {
            this.texCoord1d += point.distanceTo(this.prevPoint);
            this.prevPoint.set(point);
        }

        if (this.isSurfaceShape) {
            this.vertexArray.add((float) (longitude - this.vertexOrigin.getX()));
            this.vertexArray.add((float) (latitude - this.vertexOrigin.getY()));
            this.vertexArray.add((float) (altitude - this.vertexOrigin.getZ()));
            this.vertexArray.add((float) this.texCoord1d);
            this.outlineElements.add((short) vertex);
        } else {
            this.vertexArray.add((float) (point.getX() - this.vertexOrigin.getX()));
            this.vertexArray.add((float) (point.getY() - this.vertexOrigin.getY()));
            this.vertexArray.add((float) (point.getZ() - this.vertexOrigin.getZ()));
            this.vertexArray.add((float) this.texCoord1d);
            this.outlineElements.add((short) vertex);

            if (this.extrude) {
                point = rc.geographicToCartesian(latitude, longitude, 0, this.getAltitudeMode(), this.point);
                this.vertexArray.add((float) (point.getX() - this.vertexOrigin.getX()));
                this.vertexArray.add((float) (point.getY() - this.vertexOrigin.getY()));
                this.vertexArray.add((float) (point.getZ() - this.vertexOrigin.getZ()));
                this.vertexArray.add((float) 0 /*unused*/);
                this.interiorElements.add((short) vertex);
                this.interiorElements.add((short) (vertex + 1));
            }

            if (this.extrude && !intermediate) {
                this.verticalElements.add((short) vertex);
                this.verticalElements.add((short) (vertex + 1));
            }
        }
    }
}
