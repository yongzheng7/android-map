const int SAMPLE_COUNT = 2;
const float SAMPLES = 2.0;

uniform int fragMode;
uniform mat4 mvpMatrix;
uniform mat3 texCoordMatrix;
uniform vec3 vertexOrigin;
uniform vec3 eyePoint;
uniform float eyeMagnitude;
uniform float eyeMagnitude2;
uniform mediump vec3 lightDirection;
uniform vec3 invWavelength;
uniform float atmosphereRadius;
uniform float atmosphereRadius2;
uniform float globeRadius;
uniform float KrESun;
uniform float KmESun;
uniform float Kr4PI;
uniform float Km4PI;
uniform float scale;
uniform float scaleDepth;
uniform float scaleOverScaleDepth;

attribute vec4 vertexPoint;
attribute vec2 vertexTexCoord;

varying vec3 primaryColor;
varying vec3 secondaryColor;
varying vec3 direction;

float scaleFunc(float cos) {
	float x = 1.0 - cos;
	return scaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

void main() {
    vec3 point = vertexPoint.xyz + vertexOrigin;
    vec3 ray = point - eyePoint;
    float far = length(ray);
    ray /= far;

    vec3 start;
    float startOffset;

    if (eyeMagnitude < atmosphereRadius) {
        start = eyePoint;
        float height = length(start);
        float depth = exp(scaleOverScaleDepth * (globeRadius - eyeMagnitude));
        float startAngle = dot(ray, start) / height;
        startOffset = depth*scaleFunc(startAngle);
    } else {
        float B = 2.0 * dot(eyePoint, ray);
        float C = eyeMagnitude2 - atmosphereRadius2;
        float det = max(0.0, B*B - 4.0 * C);
        float near = 0.5 * (-B - sqrt(det));

        start = eyePoint + ray * near;
        far -= near;
        float startAngle = dot(ray, start) / atmosphereRadius;
        float startDepth = exp(-1.0 / scaleDepth);
        startOffset = startDepth*scaleFunc(startAngle);
    }

    float sampleLength = far / SAMPLES;
    float scaledLength = sampleLength * scale;
    vec3 sampleRay = ray * sampleLength;
    vec3 samplePoint = start + sampleRay * 0.5;

    /* Now loop through the sample rays */
    vec3 frontColor = vec3(0.0, 0.0, 0.0);
    for(int i=0; i<SAMPLE_COUNT; i++)
    {
        float height = length(samplePoint);
        float depth = exp(scaleOverScaleDepth * (globeRadius - height));
        float lightAngle = dot(lightDirection,samplePoint) / height;
        float cameraAngle = dot(ray, samplePoint) / height;
        float scatter = (startOffset + depth*(scaleFunc(lightAngle) - scaleFunc(cameraAngle)));
        vec3 attenuate = exp(-scatter * (invWavelength * Kr4PI + Km4PI));
        frontColor += attenuate * (depth * scaledLength);
        samplePoint += sampleRay;
    }

    primaryColor = frontColor * (invWavelength * KrESun);
    secondaryColor = frontColor * KmESun;
    direction = eyePoint - point;

    gl_Position = mvpMatrix * vertexPoint;
}
