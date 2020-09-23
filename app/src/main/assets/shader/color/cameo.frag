precision mediump float;
uniform sampler2D uTexture;
uniform vec2 uCameo ; //浮雕参数
//const vec2 uCameo = vec2(1920,1080); //浮雕参数
varying vec2 vTextureCo;
void main() {
    vec2 tex = vTextureCo;
    vec4 textureColor = texture2D(uTexture, vTextureCo);
    vec2 upLeftUV = vec2(tex.x - 1.0/uCameo.x, tex.y - 1.0/uCameo.y);
    vec4 upLeftColor = texture2D(uTexture,upLeftUV);
    vec4 delColor = textureColor - upLeftColor;
    float h = 0.3*delColor.x + 0.59*delColor.y + 0.11*delColor.z;
    vec4 bkColor = vec4(0.5, 0.5, 0.5, 1.0);
    gl_FragColor = vec4(h,h,h,0.0) +bkColor;
}