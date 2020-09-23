precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTextureCo;
void main() {
    vec4 textureColor = texture2D(uTexture, vTextureCo);
    gl_FragColor = vec4((1.0 - textureColor.rgb), textureColor.w);
}