/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform bool enableTexture;
uniform vec4 color;
uniform sampler2D texSampler;
uniform bool enablePickMode;

varying vec2 texCoord;

void main() {
    if (enablePickMode && enableTexture) {
        float texMask = floor(texture2D(texSampler, texCoord).a + 0.5);
        gl_FragColor = color * texMask;
     } else if (!enablePickMode && enableTexture) {
        gl_FragColor = color * texture2D(texSampler, texCoord);
    } else {
        gl_FragColor = color;
    }
}