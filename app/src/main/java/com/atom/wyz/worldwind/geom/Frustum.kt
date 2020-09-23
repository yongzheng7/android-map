package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Frustum {
    val left: Plane = Plane(1.0, 0.0, 0.0, 1.0) // 该面是距离原点1个单位 且法向量指向原点方向

    val right: Plane = Plane(-1.0, 0.0, 0.0, 1.0)

    val bottom: Plane = Plane(0.0, 1.0, 0.0, 1.0)

    val top: Plane = Plane(0.0, -1.0, 0.0, 1.0)

    val near: Plane = Plane(0.0, 0.0, -1.0, 1.0)

    val far: Plane = Plane(0.0, 0.0, 1.0, 1.0)

    val planes = arrayOf(this.left, this.right, this.top, this.bottom, this.near, this.far)

    protected val viewport = Viewport(0, 0, 1, 1)

    private val scratchMatrix = Matrix4()

    constructor()
    constructor(left: Plane, right: Plane, bottom: Plane, top: Plane, near: Plane, far: Plane, viewport: Viewport ) {
        this.left.set(left)
        this.right.set(right)
        this.bottom.set(bottom)
        this.top.set(top)
        this.near.set(near)
        this.far.set(far)
        this.viewport.set(viewport)
    }

    /**
     * 将此视锥设置为单位视锥，其每个平面距中心1米
     */
    fun setToUnitFrustum(): Frustum {
        left.set(1.0, 0.0, 0.0, 1.0)
        right.set(-1.0, 0.0, 0.0, 1.0)
        bottom.set(0.0, 1.0, 0.0, 1.0)
        top.set(0.0, -1.0, 0.0, 1.0)
        near.set(0.0, 0.0, -1.0, 1.0)
        far.set(0.0, 0.0, 1.0, 1.0)
        viewport.set(0, 0, 1 , 1)
        return this
    }

    /**
     * 视口变换 通过矩阵
     */
    fun transformByMatrix(matrix: Matrix4): Frustum {
        left.transformByMatrix(matrix)
        right.transformByMatrix(matrix)
        bottom.transformByMatrix(matrix)
        top.transformByMatrix(matrix)
        near.transformByMatrix(matrix)
        far.transformByMatrix(matrix)
        return this
    }

    /**
     * 视口进行规格化
     */
    fun normalize(): Frustum {
        left.normalizeIfNeeded()
        right.normalizeIfNeeded()
        bottom.normalizeIfNeeded()
        top.normalizeIfNeeded()
        near.normalizeIfNeeded()
        far.normalizeIfNeeded()
        return this
    }

    /**
     * 来自透视矩阵 变为视口
     */
    fun setToModelviewProjection(projection: Matrix4, modelview: Matrix4 , viewport: Viewport ): Frustum {
        scratchMatrix.transposeMatrix(modelview)
        /**
         * 0  1  2  3
         * 4  5  6  7
         * 8  9  10 11
         * 12 13 14 15
         *
         * 1  0  0  0
         * 0  1  0  0
         * 0  0  1  0
         * 0  0  0  1
         */
        val m: DoubleArray = projection.m
        var x: Double
        var y: Double
        var z: Double
        var w: Double
        // Left Plane = row 4 + row 1:
        x = m[12] + m[0]
        y = m[13] + m[1]
        z = m[14] + m[2]
        w = m[15] + m[3]
        left.set(x , y , z , w )
        left.transformByMatrix(scratchMatrix)

        // Right Plane = row 4 - row 1:
        x = m[12] - m[0]
        y = m[13] - m[1]
        z = m[14] - m[2]
        w = m[15] - m[3]
        right.set(x , y , z , w )
        right.transformByMatrix(scratchMatrix)
        // Bottom Plane = row 4 + row 2:
        x = m[12] + m[4]
        y = m[13] + m[5]
        z = m[14] + m[6]
        w = m[15] + m[7]
        bottom.set(x , y , z, w )
        bottom.transformByMatrix(scratchMatrix)
        // Top Plane = row 4 - row 2:
        x = m[12] - m[4]
        y = m[13] - m[5]
        z = m[14] - m[6]
        w = m[15] - m[7]
        top.set(x , y , z, w )
        top.transformByMatrix(scratchMatrix)
        // Near Plane = row 4 + row 3:
        x = m[12] + m[8]
        y = m[13] + m[9]
        z = m[14] + m[10]
        w = m[15] + m[11]
        near.set(x, y , z , w )
        near.transformByMatrix(scratchMatrix)
        // Far Plane = row 4 - row 3:
        x = m[12] - m[8]
        y = m[13] - m[9]
        z = m[14] - m[10]
        w = m[15] - m[11]
        far.set(x , y , z , w)
        far.transformByMatrix(scratchMatrix)
        this.viewport.set(viewport)

        return this
    }

    fun setToModelviewProjection(
        projection: Matrix4,
        modelview: Matrix4,
        viewport: Viewport,
        subViewport: Viewport
    ): Frustum {
        // Compute the sub-viewport's four edges in screen coordinates.
        val left = subViewport.x.toDouble()
        val right = (subViewport.x + subViewport.width).toDouble()
        val bottom = subViewport.y.toDouble()
        val top = (subViewport.y + subViewport.height).toDouble()
        // Transform the sub-viewport's four edges from screen coordinates to Cartesian coordinates.
        var bln: Vec3
        var blf: Vec3
        var brn: Vec3
        var brf: Vec3
        var tln: Vec3
        var tlf: Vec3
        var trn: Vec3
        var trf: Vec3
        val mvpInv = scratchMatrix.setToMultiply(projection, modelview).invert()
        mvpInv.unProject(left, bottom, viewport, Vec3().also { bln = it }, Vec3().also { blf = it })
        mvpInv.unProject(right, bottom, viewport, Vec3().also { brn = it }, Vec3().also { brf = it })
        mvpInv.unProject(left, top, viewport, Vec3().also { tln = it }, Vec3().also { tlf = it })
        mvpInv.unProject(right, top, viewport, Vec3().also { trn = it }, Vec3().also { trf = it })

        val va = Vec3(tlf.x - bln.x, tlf.y - bln.y, tlf.z - bln.z)
        val vb = Vec3(tln.x - blf.x, tln.y - blf.y, tln.z - blf.z)
        val nl = va.cross(vb)
        this.left.set(nl.x, nl.y, nl.z, -nl.dot(bln))

        va.set(trn.x - brf.x, trn.y - brf.y, trn.z - brf.z)
        vb.set(trf.x - brn.x, trf.y - brn.y, trf.z - brn.z);
        val nr = va.cross(vb)
        this.right.set(nr.x, nr.y, nr.z, -nr.dot(brn))

        va.set(brf.x - bln.x, brf.y - bln.y, brf.z - bln.z);
        vb.set(blf.x - brn.x, blf.y - brn.y, blf.z - brn.z);
        val nb = va.cross(vb)
        this.bottom.set(nb.x, nb.y, nb.z, -nb.dot(brn))

        va.set(tlf.x - trn.x, tlf.y - trn.y, tlf.z - trn.z);
        vb.set(trf.x - tln.x, trf.y - tln.y, trf.z - tln.z);
        val nt = va.cross(vb)
        this.top.set(nt.x, nt.y, nt.z, -nt.dot(tln))

        va.set(tln.x - brn.x, tln.y - brn.y, tln.z - brn.z);
        vb.set(trn.x - bln.x, trn.y - bln.y, trn.z - bln.z);
        val nn = va.cross(vb)
        near.set(nn.x, nn.y, nn.z, -nn.dot(bln))

        va.set(trf.x - blf.x, trf.y - blf.y, trf.z - blf.z);
        vb.set(tlf.x - brf.x, tlf.y - brf.y, tlf.z - brf.z);
        val nf = va.cross(vb)
        far.set(nf.x, nf.y, nf.z, -nf.dot(blf))

        this.viewport.set(subViewport)
        return this
    }
    /**
     * 判断视锥是否包含点
     */
    fun containsPoint(point: Vec3): Boolean {
        if (far.dot(point) <= 0) return false
        if (left.dot(point) <= 0) return false
        if (right.dot(point) <= 0) return false
        if (top.dot(point) <= 0) return false
        if (bottom.dot(point) <= 0) return false
        return near.dot(point) > 0
    }

    /**
     * 确定线段是否与该视锥相交。
     */
    fun intersectsSegment(pointA: Vec3, pointB: Vec3): Boolean {
        // First do a trivial accept test.
        if (containsPoint(pointA) || containsPoint(pointB)) return true
        if (pointA == pointB) return false
        for (plane in planes) { // See if both points are behind the plane and therefore not in the frustum.
            //查看两个点是否都在平面后面，因此不在视锥中。
            if (plane.onSameSide(pointA, pointB) < 0) return false
            // See if the segment intersects the plane.
            // 查看线段是否与平面相交。
            if (plane.clip(pointA, pointB) != null) return true
        }
        return false // segment does not intersect frustum
    }


    fun intersectsViewport(viewport: Viewport): Boolean {
        return this.viewport.intersects(viewport)
    }


    override fun toString(): String {
        return "left={$left}, right={$right}, bottom={$bottom}, top={$top}, near={$near}, far={$far}"
    }
}