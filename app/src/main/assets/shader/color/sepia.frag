precision mediump float;
uniform sampler2D uTexture;

varying vec2 vTextureCo;

const lowp mat4 colorMatrix = mat4(0.3588, 0.7044, 0.1368, 0.0,0.2990, 0.5870, 0.1140, 0.0,0.2392, 0.4696, 0.0912, 0.0,0, 0, 0, 1.0);
const lowp float intensityone = 1.0;

void main() {
    vec4 textureColor = texture2D(uTexture, vTextureCo);
    lowp vec4 outputColor = textureColor * colorMatrix;
    gl_FragColor = (intensityone * outputColor) + ((1.0 - intensityone) * textureColor);
}