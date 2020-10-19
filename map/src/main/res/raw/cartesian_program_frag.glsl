/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
precision mediump float;

varying vec3 color;

void main() {
    gl_FragColor = vec4(color, 1.0);
}