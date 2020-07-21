uniform mat4 mvpMatrix;
uniform mat4 svpMatrix[2];

attribute vec4 vertexPoint;

varying vec4 sensorPosition;
varying float sensorDistance;

void main() {
    /* 进行顶点变换 */
    gl_Position = mvpMatrix * vertexPoint;

    /* 矩阵2顶点变换 */
    vec4 sensorEyePosition = svpMatrix[1] * vertexPoint;
    // 在此进行顶点变换
    sensorPosition = svpMatrix[0] * sensorEyePosition;
    // 获取长度
    sensorDistance = length(sensorEyePosition);
}