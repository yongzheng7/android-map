/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package com.atom.wyz.worldwind.geom;

import android.graphics.Color;

import com.atom.wyz.worldwind.util.Logger;

/**
 * Color with red, green, blue and alpha components. Each RGB component is a number between 0.0 and 1.0 indicating the
 * component's intensity. The alpha component is a number between 0.0 (fully transparent) and 1.0 (fully opaque)
 * indicating the color's opacity.
 */
public class SimpleColor {

    public float red = 1;

    public float green = 1;

    public float blue = 1;

    public float alpha = 1;

    public SimpleColor() {
    }

    public SimpleColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }


    public SimpleColor(int colorInt) {
        this.red = android.graphics.Color.red(colorInt) / (float) 0xFF;
        this.green = android.graphics.Color.green(colorInt) / (float) 0xFF;
        this.blue = android.graphics.Color.blue(colorInt) / (float) 0xFF;
        this.alpha = android.graphics.Color.alpha(colorInt) / (float) 0xFF;
    }


    public SimpleColor(SimpleColor color) {
        if (color == null) {
            throw new IllegalArgumentException(
                    Logger.Companion.logMessage(Logger.ERROR, "Color", "constructor", "missingColor"));
        }

        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
        this.alpha = color.alpha;
    }


    public SimpleColor set(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }


    public SimpleColor set(int colorInt) {
        this.red = Color.red(colorInt) / (float) 0xFF;
        this.green = Color.green(colorInt) / (float) 0xFF;
        this.blue = Color.blue(colorInt) / (float) 0xFF;
        this.alpha = Color.alpha(colorInt) / (float) 0xFF;
        return this;
    }


    public SimpleColor set(SimpleColor color) {
        if (color == null) {
            throw new IllegalArgumentException(
                    Logger.Companion.logMessage(Logger.ERROR, "Color", "set", "missingColor"));
        }

        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
        this.alpha = color.alpha;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        SimpleColor that = (SimpleColor) o;
        return this.red == that.red
                && this.green == that.green
                && this.blue == that.blue
                && this.alpha == that.alpha;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(red);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(green);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(blue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(alpha);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "(r=" + this.red + ", g=" + this.green + ", b=" + this.blue + ", a=" + this.alpha + ")";
    }

    public float[] toArray(float[] result, int offset) {
        if (result == null || result.length - offset < 4) {
            throw new IllegalArgumentException(
                    Logger.Companion.logMessage(Logger.ERROR, "Color", "toArray", "missingResult"));
        }

        result[offset++] = this.red;
        result[offset++] = this.green;
        result[offset++] = this.blue;
        result[offset] = this.alpha;

        return result;
    }

    public float[] premultiplyToArray(float[] result, int offset) {
        if (result == null || result.length - offset < 4) {
            throw new IllegalArgumentException(
                    Logger.Companion.logMessage(Logger.ERROR, "Color", "premultiplyToArray", "missingResult"));
        }

        result[offset++] = this.red * this.alpha;
        result[offset++] = this.green * this.alpha;
        result[offset++] = this.blue * this.alpha;
        result[offset] = this.alpha;

        return result;
    }

    /**
     * Returns this color's components as a color int. Color ints are stored as packed ints as follows: <code>(alpha <<
     * 24) | (red << 16) | (green << 8) | (blue)</code>. Each component is an 8 bit number between 0 and 255 with 0
     * indicating the component's intensity.
     *
     * @return this color converted to a color int
     */
    public int toColorInt() {
        int r8 = Math.round(this.red * 0xFF);
        int g8 = Math.round(this.green * 0xFF);
        int b8 = Math.round(this.blue * 0xFF);
        int a8 = Math.round(this.alpha * 0xFF);

        return android.graphics.Color.argb(a8, r8, g8, b8);
    }
}
