precision mediump float;

uniform sampler2D uTexture;

varying vec2 vTextureCo;

uniform float uTexWidth;
uniform float uTexHeight;

const mediump mat3 convolutionMatrix = mat3(-1.0, 0.0, 1.0,-2.0, 0.0, 2.0,-1.0, 0.0, 1.0);

void main() {
    vec4 textureColor = texture2D(uTexture, vTextureCo);

             vec2 widthStep = vec2(uTexWidth, 0.0);
             vec2 heightStep = vec2(0.0, uTexHeight);
             vec2 widthHeightStep = vec2(uTexWidth, uTexHeight);
             vec2 widthNegativeHeightStep = vec2(uTexWidth, -uTexHeight);

            mediump vec4 bottomLeftColor = texture2D(uTexture, vTextureCo.xy - widthNegativeHeightStep);
           mediump vec4 topRightColor = texture2D(uTexture, vTextureCo.xy + widthNegativeHeightStep);
             mediump vec4 topLeftColor = texture2D(uTexture, vTextureCo.xy - widthHeightStep);
            mediump vec4 bottomRightColor  = texture2D(uTexture, vTextureCo.xy + widthHeightStep);
            mediump vec4 leftColor = texture2D(uTexture,  vTextureCo.xy - widthStep);
            mediump vec4 rightColor = texture2D(uTexture, vTextureCo.xy + widthStep);
            mediump vec4 bottomColor = texture2D(uTexture, vTextureCo.xy + heightStep);
            mediump vec4 topColor = texture2D(uTexture, vTextureCo.xy - heightStep);


    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];
             resultColor += leftColor * convolutionMatrix[1][0] + textureColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];
             resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];
    gl_FragColor = resultColor;
}