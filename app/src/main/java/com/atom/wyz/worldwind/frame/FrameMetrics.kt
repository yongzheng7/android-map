package com.atom.wyz.worldwind.frame

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.util.LruMemoryCache
import java.util.*

/**
 * 帧统计
 */
class FrameMetrics {

    class TimeMetrics {
        var begin: Long = 0
        var time: Long = 0
        var timeSum: Long = 0
        var timeSumOfSquares: Long = 0
        var count: Long = 0
    }

    class CacheMetrics {
        var capacity = 0
        var usedCapacity = 0
        var entryCount = 0
    }

    protected var renderMetrics: TimeMetrics = TimeMetrics()

    protected var drawMetrics: TimeMetrics = TimeMetrics()

    protected var renderResourceCacheMetrics = CacheMetrics()

    private val drawLock = Any()

    protected fun markBegin(metrics: TimeMetrics, timeMillis: Long) {
        metrics.begin = timeMillis
    }

    protected fun markEnd(metrics: TimeMetrics, timeMillis: Long) {
        metrics.time = timeMillis - metrics.begin
        metrics.timeSum += metrics.time
        metrics.timeSumOfSquares += metrics.time * metrics.time
        metrics.count++
    }

    protected fun resetTimeMetrics(metrics: TimeMetrics) { // reset the metrics collected across multiple frames
        metrics.timeSum = 0
        metrics.timeSumOfSquares = 0
        metrics.count = 0
    }

    protected fun computeTimeAverage(metrics: TimeMetrics): Double {
        return if (metrics.count > 0) {
            metrics.timeSum / metrics.count.toDouble()
        } else {
            0.0
        }
    }

    protected fun computeTimeStdDev(metrics: TimeMetrics): Double {
        return if (metrics.count > 0) {
            val avg = metrics.timeSum.toDouble() / metrics.count.toDouble()
            val `var` = metrics.timeSumOfSquares.toDouble() / metrics.count.toDouble() - avg * avg
            Math.sqrt(`var`)
        } else {
            0.0
        }
    }

    protected fun assembleCacheMetrics(metrics: CacheMetrics, cache: LruMemoryCache<*,*>) {
        metrics.capacity = cache.capacity
        metrics.usedCapacity = cache.usedCapacity
        metrics.entryCount = cache.getEntryCount()
    }

    fun getRenderTime(): Long {
        return renderMetrics.time
    }

    fun getRenderTimeAverage(): Double {
        return computeTimeAverage(renderMetrics)
    }

    fun getRenderTimeStdDev(): Double {
        return computeTimeStdDev(renderMetrics)
    }

    fun getRenderTimeTotal(): Long {
        return renderMetrics.timeSum
    }

    fun getRenderCount(): Long {
        return renderMetrics.count
    }

    fun getDrawTime(): Long {
        return drawMetrics.time
    }

    fun getDrawTimeAverage(): Double {
        return computeTimeAverage(drawMetrics)
    }

    fun getDrawTimeStdDev(): Double {
        return computeTimeStdDev(drawMetrics)
    }

    fun getDrawTimeTotal(): Long {
        return drawMetrics.timeSum
    }

    fun getDrawCount(): Long {
        return drawMetrics.count
    }

    fun getRenderResourceCacheCapacity(): Int {
        return renderResourceCacheMetrics.capacity
    }

    fun getRenderResourceCacheUsedCapacity(): Int {
        return renderResourceCacheMetrics.usedCapacity
    }

    fun getRenderResourceCacheEntryCount(): Int {
        return renderResourceCacheMetrics.entryCount
    }

    fun beginRendering(rc: RenderContext) {
        val now = System.currentTimeMillis()
        markBegin(renderMetrics, now)
    }

    fun endRendering(rc: RenderContext) {
        val now = System.currentTimeMillis()
        markEnd(renderMetrics, now)
        this.assembleCacheMetrics(renderResourceCacheMetrics, rc.renderResourceCache!!)
    }

    fun beginDrawing(dc: DrawContext) {
        val now = System.currentTimeMillis()
        markBegin(drawMetrics, now)
    }

    fun endDrawing(dc: DrawContext) {
        val now = System.currentTimeMillis()
        markEnd(drawMetrics, now)
    }

    fun reset() {
        this.resetTimeMetrics(renderMetrics)
        synchronized(this.drawLock) { this.resetTimeMetrics(drawMetrics) }
    }


    protected fun printCacheMetrics(metrics: CacheMetrics, out: java.lang.StringBuilder) {
        out.append("capacity=").append(String.format(Locale.US, "%,.0f", metrics.capacity / 1024.0))
            .append("KB")
        out.append(", usedCapacity=")
            .append(String.format(Locale.US, "%,.0f", metrics.usedCapacity / 1024.0)).append("KB")
        out.append(", entryCount=").append(metrics.entryCount)
    }

    protected fun printTimeMetrics(metrics: TimeMetrics, out: java.lang.StringBuilder) {
        out.append("lastTime=").append(metrics.time).append("ms")
        out.append(", totalTime=").append(metrics.timeSum).append("ms")
        out.append(", count=").append(metrics.count)
        out.append(", avg=").append(String.format(Locale.US, "%.1f", computeTimeAverage(metrics)))
            .append("ms")
        out.append(", stdDev=")
            .append(String.format(Locale.US, "%.1f", computeTimeStdDev(metrics))).append("ms")
    }
    override fun toString(): String {
        val sb = StringBuilder("FrameMetrics")
        sb.append("{renderMetrics={")
        this.printTimeMetrics(renderMetrics, sb)
        sb.append("}, drawMetrics={")
        this.printTimeMetrics(drawMetrics, sb)
        sb.append("}, renderResourceCacheMetrics={")
        this.printCacheMetrics(renderResourceCacheMetrics, sb)
        sb.append("}")
        return sb.toString()
    }
}