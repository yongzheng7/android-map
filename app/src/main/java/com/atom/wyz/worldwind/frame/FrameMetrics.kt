package com.atom.wyz.worldwind.frame

/**
 * 帧统计
 */
class FrameMetrics {

    protected class Metrics {
        var begin: Long = 0
        var time: Long = 0
        var timeSum: Long = 0
        var timeSumOfSquares: Long = 0
        var count: Long = 0
    }

    protected fun markBegin(metrics: Metrics, timeMillis: Long) {
        metrics.begin = timeMillis
    }

    protected fun markEnd(metrics: Metrics, timeMillis: Long) {
        metrics.time = timeMillis - metrics.begin
        metrics.timeSum += metrics.time
        metrics.timeSumOfSquares += metrics.time * metrics.time
        metrics.count++
    }

    protected fun resetMetrics(metrics: Metrics) { // reset the metrics collected across multiple frames
        metrics.timeSum = 0
        metrics.timeSumOfSquares = 0
        metrics.count = 0
    }

    protected fun computeTimeAverage(metrics: Metrics): Double {
        return metrics.timeSum / metrics.count.toDouble()
    }

    protected fun computeTimeStdDev(metrics: Metrics): Double {
        val avg = metrics.timeSum.toDouble() / metrics.count
        val `var` =
            metrics.timeSumOfSquares.toDouble()/ metrics.count - avg * avg
        return Math.sqrt(`var`)
    }

    protected var renderMetrics: Metrics = Metrics()

    protected var drawMetrics: Metrics = Metrics()

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

    fun beginRendering() {
        val now = System.currentTimeMillis()
        markBegin(renderMetrics, now)
    }

    fun endRendering() {
        val now = System.currentTimeMillis()
        markEnd(renderMetrics, now)
    }

    fun beginDrawing() {
        val now = System.currentTimeMillis()
        markBegin(drawMetrics, now)
    }

    fun endDrawing() {
        val now = System.currentTimeMillis()
        markEnd(drawMetrics, now)
    }

    fun reset() {
        resetMetrics(renderMetrics)
        resetMetrics(drawMetrics)
    }

    override fun toString(): String {
        val sb = StringBuilder("FrameStatistics")
        sb.append("{\n")
        sb.append("renderTime=").append(renderMetrics.time)
        sb.append(", renderTimeTotal=").append(renderMetrics.timeSum)
        sb.append(", renderCount=").append(renderMetrics.count)
        sb.append(", renderTimeAvg=")
            .append(String.format("%.1f", computeTimeAverage(renderMetrics)))
        sb.append(", renderTimeStdDev=")
            .append(String.format("%.1f", computeTimeStdDev(renderMetrics)))
        sb.append("\n")
        sb.append("drawTime=").append(drawMetrics.time)
        sb.append(", drawTimeTotal=").append(drawMetrics.timeSum)
        sb.append(", drawCount=").append(drawMetrics.count)
        sb.append(", drawTimeAvg=").append(String.format("%.1f", computeTimeAverage(drawMetrics)))
        sb.append(", drawTimeStdDev=")
            .append(String.format("%.1f", computeTimeStdDev(drawMetrics)))
        sb.append("\n}")
        return sb.toString()
    }
}