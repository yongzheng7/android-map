/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package com.atom.wyz.worldwind.draw;

import android.opengl.GLES20;
import android.util.Log;

import com.atom.wyz.worldwind.context.DrawContext;
import com.atom.wyz.worldwind.frame.Framebuffer;
import com.atom.wyz.worldwind.geom.Color;
import com.atom.wyz.worldwind.geom.Matrix3;
import com.atom.wyz.worldwind.geom.Matrix4;
import com.atom.wyz.worldwind.geom.Sector;
import com.atom.wyz.worldwind.geom.Vec3;
import com.atom.wyz.worldwind.shader.GpuTexture;
import com.atom.wyz.worldwind.util.pool.Pool;

import java.util.ArrayList;


public class DrawableSurfaceShapeJava implements Drawable {

    public DrawShapeState drawState = new DrawShapeState();

    public Sector sector = new Sector();

    private Matrix4 mvpMatrix = new Matrix4();

    private Matrix4 textureMvpMatrix = new Matrix4();

    private Matrix3 identityMatrix3 = new Matrix3();

    private Color color = new Color();

    private Pool<DrawableSurfaceShapeJava> pool;

    public DrawableSurfaceShapeJava() {
    }

    public static DrawableSurfaceShapeJava obtain(Pool<DrawableSurfaceShapeJava> pool) {
        DrawableSurfaceShapeJava instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableSurfaceShapeJava().setPool(pool);
    }

    private DrawableSurfaceShapeJava setPool(Pool<DrawableSurfaceShapeJava> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.drawState.reset();

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    public void draw(DrawContext dc) {
        // 判断program是否存在且可用
        if (this.drawState.getProgram() == null || !this.drawState.getProgram().useProgram(dc)) {
            return;
        }

        // 激活多纹理单元0。
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        // 设置为使用纹理顶点坐标属性。
        GLES20.glEnableVertexAttribArray(1 /*vertexTexCoord*/); // only vertexPoint is enabled by default

        // 获取list
        ArrayList<Object> scratchList = dc.scratchList();

        try {
            // 将自身加到gailist中
            scratchList.add(this);

            //获取draw 获取就近的所有drawable
            Drawable next;
            while ((next = dc.peekDrawable()) != null && next.getClass() == this.getClass()) { // check if the drawable at the front of the queue can be batched
                scratchList.add(dc.pollDrawable()); // take it off the queue
            }
            Log.e("draw" , "terrainSector > 1 ");
            // 在每个可绘制地形上绘制累积的形状。
            for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) { // 获取绘制的图块
                // 获取与绘图上下文关联的可绘制地形。
                DrawableTerrain terrain = dc.getDrawableTerrain(idx);
                // 将累积的表面形状绘制为代表地形扇区的纹理。 在这一步该瓦片的所有可绘制的图像绘制到帧缓存上
                if (this.drawShapesToTexture(dc, terrain) > 0) {
                    // Draw the texture containing the rasterized shapes onto the terrain geometry.
                    this.drawTextureToTerrain(dc, terrain);
                }
            }
        } finally {
            // Clear the accumulated shapes.
            scratchList.clear();
            // Restore the default WorldWind OpenGL state.
            GLES20.glDisableVertexAttribArray(1 /*vertexTexCoord*/); // only vertexPoint is enabled by default
        }
    }

    protected int drawShapesToTexture(DrawContext dc, DrawableTerrain terrain) {
        // 形状已累积在绘制上下文的暂存列表中。
        ArrayList<Object> scratchList = dc.scratchList();

        // 地形的扇区定义了要绘制的地理区域。 确定要绘制的瓦片的区域
        Sector terrainSector = terrain.getSector();

        // 跟踪绘制到纹理中的形状的数量。
        int shapeCount = 0;

        try {
            // 获取一个帧缓存
            Framebuffer framebuffer = dc.scratchFramebuffer();
            if (!framebuffer.bindFramebuffer(dc)) {
                return 0; // framebuffer failed to bind
            }

            // 清除帧缓冲区并禁用深度测试。
            GpuTexture colorAttachment = framebuffer.getAttachedTexture(GLES20.GL_COLOR_ATTACHMENT0);
            GLES20.glViewport(0, 0, colorAttachment.getTextureWidth(), colorAttachment.getTextureHeight());
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            // 使用绘制上下文的选取模式。 只能使用方式二和方式三
            this.drawState.getProgram().enablePickMode(dc.getPickMode());
            // 计算将地理坐标转换为适合地形扇区的纹理片段的图块公共矩阵。
            // TODO capture this in a method on Matrix4
            Log.e("draw" , "terrainSector >  "+terrainSector.toString());
            this.textureMvpMatrix.setToIdentity(); // 设置为单位矩阵
            this.textureMvpMatrix.multiplyByTranslation(-1, -1, 0); // 进行x y 的位移操作
            this.textureMvpMatrix.multiplyByScale(
                    2 / terrainSector.deltaLongitude(),  // 进行缩放操作
                    2 / terrainSector.deltaLatitude(),
                    0);
            this.textureMvpMatrix.multiplyByTranslation(
                    -terrainSector.getMinLongitude(),
                    -terrainSector.getMinLatitude(),
                    0);

            for (int idx = 0, len = scratchList.size(); idx < len; idx++) {
                // Get the shape.
                DrawableSurfaceShapeJava shape = (DrawableSurfaceShapeJava) scratchList.get(idx);
                if (!shape.sector.intersectsOrNextTo(terrainSector)) {
                    continue;
                }
                if (shape.drawState.getVertexBuffer() == null || !shape.drawState.getVertexBuffer().bindBuffer(dc)) {
                    continue; // vertex buffer unspecified or failed to bind
                }

                if (shape.drawState.getElementBuffer() == null || !shape.drawState.getElementBuffer().bindBuffer(dc)) {
                    continue; // element buffer unspecified or failed to bind
                }

                // Transform local shape coordinates to texture fragments appropriate for the terrain sector.
                this.mvpMatrix.set(this.textureMvpMatrix);
                this.mvpMatrix.multiplyByTranslation(shape.drawState.getVertexOrigin().getX(), shape.drawState.getVertexOrigin().getY(), shape.drawState.getVertexOrigin().getZ());
                this.drawState.getProgram().loadModelviewProjection(this.mvpMatrix);

                // Use the shape's vertex point attribute.
                GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, shape.drawState.getVertexStride(), 0);

                // Draw the specified primitives to the framebuffer texture.
                for (int primIdx = 0; primIdx < shape.drawState.getPrimCount(); primIdx++) {
                    DrawShapeState.DrawElements prim = shape.drawState.getPrims()[primIdx];
                    this.drawState.getProgram().loadColor(prim.getColor());

                    if (prim.getTexture() != null && prim.getTexture().bindTexture(dc)) {
                        this.drawState.getProgram().loadTexCoordMatrix(prim.getTexCoordMatrix());
                        this.drawState.getProgram().enableTexture(true);
                    } else {
                        this.drawState.getProgram().enableTexture(false);
                    }

                    GLES20.glVertexAttribPointer(1 /*vertexTexCoord*/, prim.getTexCoordAttrib().getSize(), GLES20.GL_FLOAT, false, shape.drawState.getVertexStride(), prim.getTexCoordAttrib().getOffset());
                    GLES20.glLineWidth(prim.getLineWidth());
                    GLES20.glDrawElements(prim.getMode(), prim.getCount(), prim.getType(), prim.getOffset());
                }

                // Accumulate the number of shapes drawn into the texture.
                shapeCount++;
            }
        } finally {
            // Restore the default WorldWind OpenGL state.
            dc.bindFramebuffer(0);
            GLES20.glViewport(dc.getViewport().getX(), dc.getViewport().getY(), dc.getViewport().getWidth(), dc.getViewport().getHeight());
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glLineWidth(1);
        }

        return shapeCount;
    }

    protected void drawTextureToTerrain(DrawContext dc, DrawableTerrain terrain) {
        if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
            return; // terrain vertex attribute failed to bind
        }

        if (!terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)) {
            return; // terrain vertex attribute failed to bind
        }

        GpuTexture colorAttachment = dc.scratchFramebuffer().getAttachedTexture(GLES20.GL_COLOR_ATTACHMENT0);
        if (!colorAttachment.bindTexture(dc)) {
            return; // framebuffer texture failed to bind
        }

        // Configure the program to draw texture fragments unmodified and aligned with the terrain.
        // TODO consolidate pickMode and enableTexture into a single textureMode
        // TODO it's confusing that pickMode must be disabled during surface shape render-to-texture
        this.drawState.getProgram().enablePickMode(false);
        this.drawState.getProgram().enableTexture(true);
        this.drawState.getProgram().loadTexCoordMatrix(this.identityMatrix3);
        this.drawState.getProgram().loadColor(this.color);

        // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
        Vec3 terrainOrigin = terrain.getVertexOrigin();
        this.mvpMatrix.set(dc.getModelviewProjection());
        this.mvpMatrix.multiplyByTranslation(terrainOrigin.getX(), terrainOrigin.getY(), terrainOrigin.getZ());
        this.drawState.getProgram().loadModelviewProjection(this.mvpMatrix);

        // Draw the terrain as triangles.
        terrain.drawTriangles(dc);
    }
}
