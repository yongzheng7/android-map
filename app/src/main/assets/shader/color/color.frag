precision mediump float;
varying vec2 vTextureCo;
uniform sampler2D uTexture;
uniform vec3 uColor ;

void main() {
    gl_FragColor=texture2D(uTexture,vTextureCo)+vec4(uColor,0.0);
}