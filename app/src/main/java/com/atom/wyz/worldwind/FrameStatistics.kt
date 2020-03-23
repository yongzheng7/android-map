package com.atom.wyz.worldwind

/**
 * 帧统计
 */
class FrameStatistics {

    private val lock = Any()

    private var frameTime: Long = 0

    private var frameTimeSum: Long = 0

    private var frameTimeSumOfSquares: Long = 0

    private var frameCount: Long = 0

    private var frameBegin: Long = 0

    fun getFrameTime(): Long {
        synchronized(lock) { return frameTime }
    }

    fun getFrameTimeAverage(): Double {
        synchronized(lock) { return frameTimeSum / frameCount.toDouble() }
    }

    fun getFrameTimeStdDev(): Double {
        synchronized(lock) {
            val avg = frameTimeSum.toDouble() / frameCount.toDouble()
            val `var` = frameTimeSumOfSquares.toDouble() / frameCount.toDouble() - avg * avg
            return Math.sqrt(`var`)
        }
    }

    fun getFrameTimeTotal(): Long {
        synchronized(lock) { return frameTimeSum }
    }

    override fun toString(): String {
        synchronized(lock) {
            return String.format(
                    "FrameStatistics{frameTime=%d, frameTimeAverage=%.1f, frameTimeStdDev=%.1f, frameTimeTotal=%d, frameCount=%d",
                    frameTime,
                    getFrameTimeAverage(),
                    getFrameTimeStdDev(),
                    frameTimeSum,
                    frameCount)
        }
    }

    fun beginFrame() {
        synchronized(lock) { frameBegin = System.currentTimeMillis() }
    }

    fun getFrameCount(): Long {
        synchronized(lock) { return frameCount }
    }

    fun endFrame() {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            frameTime = now - frameBegin
            frameTimeSum += frameTime
            frameTimeSumOfSquares += frameTime * frameTime
            frameCount++
        }
    }

    fun reset() {
        synchronized(lock) {
            frameTime = 0
            frameTimeSum = 0
            frameTimeSumOfSquares = 0
            frameCount = 0
            frameBegin = 0
        }
    }
}