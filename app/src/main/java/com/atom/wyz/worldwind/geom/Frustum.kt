package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Frustum {
    val left: Plane = Plane(1.0, 0.0, 0.0, 1.0)

    val right: Plane = Plane(-1.0, 0.0, 0.0, 1.0)

    val bottom: Plane = Plane(0.0, 1.0, 0.0, 1.0)

    val top: Plane = Plane(0.0, -1.0, 0.0, 1.0)

    val near: Plane = Plane(0.0, 0.0, -1.0, 1.0)

    val far: Plane = Plane(0.0, 0.0, 1.0, 1.0)

    private var planes = arrayOf(this.left, this.right, this.top, this.bottom, this.near, this.far)

    protected val viewport = Viewport(0, 0, 1, 1)

    private val scratchMatrix = Matrix4()

    constructor()
    constructor(left: Plane?, right: Plane?, bottom: Plane?, top: Plane?, near: Plane?, far: Plane?, viewport: Viewport ) {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingPlane"))
        }
        // Internal. Intentionally not documented. See property accessors below for public interface.
        requireNotNull(viewport) { Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingViewport") }
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
    fun transformByMatrix(matrix: Matrix4?): Frustum {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "transformByMatrix", "missingMatrix"))
        }
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
        left.normalize()
        right.normalize()
        bottom.normalize()
        top.normalize()
        near.normalize()
        far.normalize()
        return this
    }

    /**
     * 来自透视矩阵 变为视口
     */
    fun setToModelviewProjection(projection: Matrix4?, modelview: Matrix4? , viewport: Viewport ): Frustum {
        require(!(projection == null || modelview == null)) {
            Logger.logMessage(
                Logger.ERROR,
                "Frustum",
                "setToModelviewProjection",
                "missingMatrix"
            )
        }

        requireNotNull(viewport) {
            Logger.logMessage(
                Logger.ERROR,
                "Frustum",
                "setToModelviewProjection",
                "missingViewport"
            )
        }
        // Compute the transpose of the modelview matrix.
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
        var d: Double
        // Left Plane = row 4 + row 1:
        x = m[12] + m[0]
        y = m[13] + m[1]
        z = m[14] + m[2]
        w = m[15] + m[3]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        left.set(x / d, y / d, z / d, w / d)
        left.transformByMatrix(scratchMatrix).normalize()

        // Right Plane = row 4 - row 1:
        x = m[12] - m[0]
        y = m[13] - m[1]
        z = m[14] - m[2]
        w = m[15] - m[3]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        right.set(x / d, y / d, z / d, w / d)
        right.transformByMatrix(scratchMatrix).normalize()
        // Bottom Plane = row 4 + row 2:
        x = m[12] + m[4]
        y = m[13] + m[5]
        z = m[14] + m[6]
        w = m[15] + m[7]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        bottom.set(x / d, y / d, z / d, w / d)
        bottom.transformByMatrix(scratchMatrix).normalize()
        // Top Plane = row 4 - row 2:
        x = m[12] - m[4]
        y = m[13] - m[5]
        z = m[14] - m[6]
        w = m[15] - m[7]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        top.set(x / d, y / d, z / d, w / d)
        top.transformByMatrix(scratchMatrix).normalize()
        // Near Plane = row 4 + row 3:
        x = m[12] + m[8]
        y = m[13] + m[9]
        z = m[14] + m[10]
        w = m[15] + m[11]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        near.set(x / d, y / d, z / d, w / d)
        near.transformByMatrix(scratchMatrix).normalize()
        // Far Plane = row 4 - row 3:
        x = m[12] - m[8]
        y = m[13] - m[9]
        z = m[14] - m[10]
        w = m[15] - m[11]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        far.set(x / d, y / d, z / d, w / d)
        far.transformByMatrix(scratchMatrix).normalize()
        // Copy the specified viewport.
        this.viewport.set(viewport)

        return this
    }

    fun setToModelviewProjection(
        projection: Matrix4?,
        modelview: Matrix4?,
        viewport: Viewport?,
        subViewport: Viewport?
    ): Frustum? {
        require(!(projection == null || modelview == null)) {
            Logger.logMessage(
                Logger.ERROR,
                "Frustum",
                "setToModelviewProjection",
                "missingMatrix"
            )
        }
        require(!(viewport == null || subViewport == null)) {
            Logger.logMessage(
                Logger.ERROR,
                "Frustum",
                "setToModelviewProjection",
                "missingViewport"
            )
        }
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
        this.left.set(nl!!.x, nl.y, nl.z, -nl.dot(bln))
        this.left.normalize()
        va[trn.x - brf.x, trn.y - brf.y] = trn.z - brf.z
        vb[trf.x - brn.x, trf.y - brn.y] = trf.z - brn.z
        val nr = va.cross(vb)
        this.right.set(nr!!.x, nr.y, nr.z, -nr.dot(brn))
        this.right.normalize()
        va[brf.x - bln.x, brf.y - bln.y] = brf.z - bln.z
        vb[blf.x - brn.x, blf.y - brn.y] = blf.z - brn.z
        val nb = va.cross(vb)
        this.bottom.set(nb!!.x, nb.y, nb.z, -nb.dot(brn))
        this.bottom.normalize()
        va[tlf.x - trn.x, tlf.y - trn.y] = tlf.z - trn.z
        vb[trf.x - tln.x, trf.y - tln.y] = trf.z - tln.z
        val nt = va.cross(vb)
        this.top.set(nt!!.x, nt.y, nt.z, -nt.dot(tln))
        this.top.normalize()
        va[tln.x - brn.x, tln.y - brn.y] = tln.z - brn.z
        vb[trn.x - bln.x, trn.y - bln.y] = trn.z - bln.z
        val nn = va.cross(vb)
        near.set(nn!!.x, nn.y, nn.z, -nn.dot(bln))
        near.normalize()
        va[trf.x - blf.x, trf.y - blf.y] = trf.z - blf.z
        vb[tlf.x - brf.x, tlf.y - brf.y] = tlf.z - brf.z
        val nf = va.cross(vb)
        far.set(nf!!.x, nf.y, nf.z, -nf.dot(blf))
        far.normalize()
        // Copy the specified sub-viewport.
        this.viewport.set(subViewport)
        return this
    }
    /**
     * 判断视锥是否包含点 TODO 有问题
     */
    fun containsPoint(point: Vec3?): Boolean {
        if (point == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"))
        }
        // See if the point is entirely within the frustum. The dot product of the point with each plane's vector
        // provides a distance to each plane. If this distance is less than 0, the point is clipped by that plane and
        // neither intersects nor is contained by the space enclosed by this Frustum.

        // 查看该点是否完全在视锥范围内。 点与每个平面向量的点积提供了到每个平面的距离。
        // 如果此距离小于0，则该点将被该平面修剪，并且该截锥既不相交也不包含在该视锥体中。

        if (far.dot(point) <= 0) return false
        if (left.dot(point) <= 0) return false
        if (right.dot(point) <= 0) return false
        if (top.dot(point) <= 0) return false
        if (bottom.dot(point) <= 0) return false
        return if (near.dot(point) <= 0) false else true
    }

    /**
     * 确定线段是否与该视锥相交。
     */
    fun intersectsSegment(pointA: Vec3?, pointB: Vec3?): Boolean {
        if (pointA == null || pointB == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"))
        }
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
    fun intersectsViewport(viewport: Viewport?): Boolean {
        requireNotNull(viewport) { Logger.logMessage(Logger.ERROR, "Frustum", "intersectsViewport", "missingViewport") }
        return this.viewport.intersects(viewport)
    }
    override fun toString(): String {
        return "left={$left}, right={$right}, bottom={$bottom}, top={$top}, near={$near}, far={$far}"
    }
}