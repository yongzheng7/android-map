package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.globe.Globe

/**
 * 观察者点位 角度 位置 倾斜度 等 导航器
 */
interface Navigator {

    fun getLatitude(): Double

    fun setLatitude(latitude: Double): Navigator

    fun getLongitude(): Double

    fun setLongitude(longitude: Double): Navigator

    fun getAltitude(): Double

    fun setAltitude(altitude: Double): Navigator

    fun getHeading(): Double

    fun setHeading(headingDegrees: Double): Navigator

    fun getTilt(): Double

    fun setTilt(tiltDegrees: Double): Navigator

    fun getRoll(): Double

    fun setRoll(rollDegrees: Double): Navigator

    fun getFieldOfView(): Double

    fun setFieldOfView(fovyDegrees: Double): Navigator

    fun getAsCamera(globe : Globe?, result: Camera?):  Camera

    fun setAsCamera(globe : Globe?, camera: Camera?): Navigator

    fun getAsLookAt(globe : Globe?, result: LookAt?): LookAt

    fun setAsLookAt(globe : Globe?, lookAt: LookAt?): Navigator

}