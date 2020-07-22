/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */


uniform mat4 mvpMatrix;

attribute vec4 vertexPoint;

varying vec3 color;
void main() {
    gl_Position = mvpMatrix * vertexPoint;
    if(vertexPoint.x != 0.0){
     color = vec3(1.0, 0.0, 0.0) ;
    }else if(vertexPoint.y != 0.0){
    color = vec3(0.0, 1.0, 0.0) ;
    }else if(vertexPoint.z != 0.0){
     color = vec3(0.0, 0.0, 1.0) ;
    }else{
    color = vec3(1.0, 1.0, 1.0) ;
    }
}