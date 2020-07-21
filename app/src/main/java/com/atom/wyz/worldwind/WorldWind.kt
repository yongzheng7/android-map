package com.atom.wyz.worldwind

import androidx.annotation.IntDef
import com.atom.wyz.worldwind.geom.Ellipsoid
import com.atom.wyz.worldwind.util.MessageService
import com.atom.wyz.worldwind.util.TaskService

class WorldWind {
    /**
     * Altitude mode indicates how World Wind interprets a position's altitude component. Accepted values are [ ][.ABSOLUTE], [.CLAMP_TO_GROUND] and [.RELATIVE_TO_GROUND].
     */
    @IntDef(ABSOLUTE, CLAMP_TO_GROUND, RELATIVE_TO_GROUND)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    public annotation class AltitudeMode

    /**
     * Path type indicates how World Wind create a geographic path between two locations. Accepted values are [ ][.GREAT_CIRCLE], [.LINEAR] and [.RHUMB_LINE].
     */
    @IntDef(GREAT_CIRCLE, LINEAR, RHUMB_LINE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class PathType


    @IntDef(POSSIBLE, FAILED, RECOGNIZED, BEGAN, CHANGED, CANCELLED, ENDED)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class GestureState


    @IntDef(RELATIVE_TO_GLOBE, RELATIVE_TO_SCREEN)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class OrientationMode


    /**
     * Offset mode indicates how World Wind interprets an offset's x and y values. Accepted values are [ ][.OFFSET_FRACTION], [.OFFSET_INSET_PIXELS] and [.OFFSET_PIXELS].
     */
    @IntDef(OFFSET_FRACTION, OFFSET_INSET_PIXELS, OFFSET_PIXELS)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class OffsetMode

    /**
     * Drawable group provides a standard set of group IDs for organizing World Window drawing into four phases:
     * background, surface, shape, and screen. Accepted values are [.BACKGROUND_DRAWABLE], [ ][.SURFACE_DRAWABLE], [.SHAPE_DRAWABLE] and [.SCREEN_DRAWABLE].
     */
    @IntDef(BACKGROUND_DRAWABLE, SURFACE_DRAWABLE, SHAPE_DRAWABLE, SCREEN_DRAWABLE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class DrawableGroup

    /**
     * Image format indicates the in-memory representation for images displayed by World Wind components. Images are
     * typically represented in the 32-bit RGBA_8888 format, the highest quality available. Components that do not
     * require an alpha channel and want to conserve memory may use the 16-bit RGBA_565 format. Accepted values are
     * [.IMAGE_FORMAT_RGBA_8888] and [.IMAGE_FORMAT_RGB_565].
     */
    @IntDef(RGBA_8888, RGB_565)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class ImageFormat


    /**
     * Navigator event type indicates the reason a NavigatorEvent has been generated.
     *
     * Accepted values are [.NAVIGATOR_MOVED] and [.NAVIGATOR_STOPPED].
     */
    @IntDef(NAVIGATOR_MOVED, NAVIGATOR_STOPPED)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class NavigatorAction

    /**
     * Wrap mode indicates how World Wind displays the contents of an image when attempting to draw a region outside of
     * the image bounds. Accepted values are [WorldWind.CLAMP] and [WorldWind.REPEAT].
     */
    @IntDef(CLAMP, REPEAT)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class WrapMode

    /**
     * Resampling mode indicates the image sampling algorithm used by World Wind to display images that appear larger or
     * smaller on screen than their native resolution. Accepted values are [WorldWind.BILINEAR] and [ ][WorldWind.NEAREST_NEIGHBOR].
     */
    @IntDef(BILINEAR, NEAREST_NEIGHBOR)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class ResamplingMode

    companion object {
        /**
         * [DrawableGroup] constant indicating drawables displayed before everything else. This group is typically
         * used to display atmosphere and stars before all other drawables.
         */
        const val BACKGROUND_DRAWABLE = 0

        /**
         * [DrawableGroup] constant indicating drawables displayed on the globe's surface. Surface drawables are
         * displayed beneath shapes and screen drawables.
         */
        const val SURFACE_DRAWABLE = 1

        /**
         * [DrawableGroup] constant indicating shape drawables, such as placemarks, polygons and polylines. Shape
         * drawables are displayed on top of surface drawables, but beneath screen drawables.
         */
        const val SHAPE_DRAWABLE = 2

        /**
         * [DrawableGroup] constant indicating drawables displayed in the plane of the screen. Screen drawables are
         * displayed on top of everything else.
         */
        const val SCREEN_DRAWABLE = 3

        ////////////////////////////////////////////////////
        /**
         * Altitude mode constant indicating an altitude relative to the globe's ellipsoid. Ignores the elevation of the
         * terrain directly beneath the position's latitude and longitude.
         * <br/>
         * 表示相对于地球椭球的高度，与地形的海拔无关。
         */
        const val ABSOLUTE: Int = 0

        /**
         * Altitude mode constant indicating an altitude on the terrain. Ignores a position's specified altitude, and always
         * places the position on the terrain.
         *  <br/>
         * 忽略位置的指定高度，并始终将位置放置在地形上。
         */
        const val CLAMP_TO_GROUND: Int = 1

        /**
         * Altitude mode constant indicating an altitude relative to the terrain. The altitude indicates height above the
         * terrain directly beneath the position's latitude and longitude.
         *  <br/>
         * 指示相对于地形的高度。 海拔高度表示该位置的经纬度正下方的地形上方的高度。
         */
        const val RELATIVE_TO_GROUND: Int = 2


        ////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Path type constant indicating a great circle arc between two locations.
         * 指示两个位置之间的大圆弧。
         */
        const val GREAT_CIRCLE = 0

        /**
         * Path type constant indicating simple linear interpolation between two locations.
         *  表示两个位置之间的简单线性插值。
         */
        const val LINEAR = 1

        /**
         * Path type constant indicating a line of constant bearing between two locations.
         * 指示两个位置之间的恒定方位线。
         */
        const val RHUMB_LINE = 2


        ////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * The POSSIBLE gesture recognizer state. Gesture recognizers in this state are idle when there is no input event to
         * evaluate, or are evaluating input events to determine whether or not to transition into another state.
         */
        const val POSSIBLE = 0

        /**
         * The FAILED gesture recognizer state. Gesture recognizers transition to this state from the POSSIBLE state when
         * the gesture cannot be recognized given the current input.
         */
        const val FAILED = 1

        /**
         * The RECOGNIZED gesture recognizer state. Discrete gesture recognizers transition to this state from the POSSIBLE
         * state when the gesture is recognized.
         */
        const val RECOGNIZED = 2

        /**
         * The BEGAN gesture recognizer state. Continuous gesture recognizers transition to this state from the POSSIBLE
         * state when the gesture is first recognized.
         */
        const val BEGAN = 3

        /**
         * The CHANGED gesture recognizer state. Continuous gesture recognizers transition to this state from the BEGAN
         * state or the CHANGED state, whenever an input event indicates a change in the gesture.
         */
        const val CHANGED = 4

        /**
         * The CANCELLED gesture recognizer state. Continuous gesture recognizers may transition to this state from the
         * BEGAN state or the CHANGED state when the touch events are cancelled.
         */
        const val CANCELLED = 5

        /**
         * The ENDED gesture recognizer state. Continuous gesture recognizers transition to this state from either the BEGAN
         * state or the CHANGED state when the current input no longer represents the gesture.
         */
        const val ENDED = 6

        ////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Notification constant requesting that World Window instances render a frame.
         */
        const val REQUEST_REDRAW = "gov.nasa.worldwind.RequestRedraw"

        /**
         * WGS 84 reference value for the Earth ellipsoid's semi-major axis: 6378137.0.
         */
        const val WGS84_SEMI_MAJOR_AXIS = 6378137.0

        /**
         * WGS 84 reference value for the Earth ellipsoid's inverse flattening (1/f): 298.257223563.
         */
        const val WGS84_INVERSE_FLATTENING = 298.257223563

        /**
         * WGS 84 reference ellipsoid for Earth. The ellipsoid's semi-major axis and inverse flattening factor are
         * configured according to the WGS 84 reference system (aka WGS 1984, EPSG:4326). WGS 84 reference values taken from
         * [](http://earth-info.nga.mil/GandG/publications/NGA_STND_0036_1_0_0_WGS84/NGA.STND.0036_1.0.0_WGS84.pdf).
         */
        val WGS84_ELLIPSOID: Ellipsoid = Ellipsoid(WGS84_SEMI_MAJOR_AXIS, WGS84_INVERSE_FLATTENING)


        ////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * {@link OrientationMode} constant indicating that the related value is specified relative to the globe.
         */
        const val RELATIVE_TO_GLOBE = 0
        /**
         * {@link OrientationMode} constant indicating that the related value is specified relative to the plane of the
         * screen.
         */
        const val RELATIVE_TO_SCREEN = 1

        ////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * [NavigatorAction] constant indicating that the navigator has moved.
         */
        const val NAVIGATOR_MOVED = 0

        /**
         * [NavigatorAction] constant indicating that the navigator has stopped moving.
         */
        const val NAVIGATOR_STOPPED = 1

        ///////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * [ImageFormat] constant indicating 32-bit RGBA_8888 image format.
         */
        const val RGBA_8888 = 0

        /**
         * [ImageFormat] constant indicating 16-bit RGBA_565 image format.
         */
        const val RGB_565 = 1

        ///////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * [ResamplingMode] constant indicating bilinear image sampling.
         */
        const val BILINEAR = 0

        /**
         * [ResamplingMode] constant indicating nearest neighbor image sampling.
         */
        const val NEAREST_NEIGHBOR = 1

        ///////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * [WrapMode] constant indicating that the image's edge pixels should be displayed outside of the image
         * bounds.
         */
        const val CLAMP = 0

        /**
         * [WrapMode] constant indicating that the image should display as a repeating pattern outside of the image
         * bounds.
         */
        const val REPEAT = 1

        ///////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * [OffsetMode] constant indicating that the associated parameters are fractional values of the virtual
         * rectangle's width or height in the range [0, 1], where 0 indicates the rectangle's origin and 1 indicates the
         * corner opposite its origin.
         * 该模式是比例模式,offset x y 成对应的x y的比例进行偏移
         */
        const val OFFSET_FRACTION = 0

        /**
         * [OffsetMode] constant indicating that the associated parameters are in units of pixels relative to the
         * virtual rectangle's corner opposite its origin corner.
         * 该模式是相减 x y 的值减去偏移量的值
         */
        const val OFFSET_INSET_PIXELS = 1

        /**
         * [OffsetMode] constant indicating that the associated parameters are in units of pixels relative to the
         * virtual rectangle's origin.
         * 该模式是直接取 offset的值作为偏移后的值
         */
        const val OFFSET_PIXELS = 2

        val messageService = MessageService()

        val taskService = TaskService()

        /**
         * Requests that all World Window instances render a frame. Internally, this dispaches a REQUEST_RENDER message to
         * the World Wind message center.
         */
        fun requestRedraw() {
            messageService.postMessage(REQUEST_REDRAW, null, null) // specify null for no sender, no user properties
        }

    }

}