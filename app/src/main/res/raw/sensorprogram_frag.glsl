
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform float range;
uniform vec4 color[2];
uniform sampler2D depthSampler;

varying vec4 sensorPosition;
varying float sensorDistance;

const vec3 minusOne = vec3(-1.0, -1.0, -1.0);
const vec3 plusOne = vec3(1.0, 1.0, 1.0);

void main() {
    // 透视除法
    vec3 clipCoord = sensorPosition.xyz / sensorPosition.w;
    // 获取一个数
    vec3 clipCoordMask = step(minusOne, clipCoord) * step(clipCoord, plusOne);
    // 获取
    float clipMask = clipCoordMask.x * clipCoordMask.y * clipCoordMask.z;

    /* 计算当位置在传感器范围内时打开的遮罩，否则关闭。*/
    float rangeMask = step(sensorDistance, range);

    /* Compute a mask that's on when the object's depth is less than the sensor's depth. The depth texture contains the
       scene's minimum depth at each position, from the sensor's point of view. */
    vec3 sensorCoord = clipCoord * 0.5 + 0.5;
    float sensorDepth = texture2D(depthSampler, sensorCoord.xy).z;
    float occludeMask = step(sensorDepth, sensorCoord.z);

    /* Modulate the RGBA color with the computed masks to display fragments according to the sensor's configuration. */
    gl_FragColor = mix(color[0], color[1], occludeMask) * clipMask * rangeMask;
}