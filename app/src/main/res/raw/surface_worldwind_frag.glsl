precision mediump float;

uniform sampler2D texSampler;

varying vec2 texCoord[2];

void main() {

    float sMask = step(0.0, texCoord[1].s) * (1.0 - step(1.0, texCoord[1].s));
    float tMask = step(0.0, texCoord[1].t) * (1.0 - step(1.0, texCoord[1].t));
    float tileMask = sMask * tMask;


    gl_FragColor = texture2D(texSampler, texCoord[0]) * tileMask;
}