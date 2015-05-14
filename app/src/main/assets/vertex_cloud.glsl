uniform mat4    mModelViewProjection;
attribute vec4  vPosition;
attribute vec4  vColor;

varying vec4    Color;

 void main()
 {
    Color = vColor;
    gl_Position = mModelViewProjection*vPosition;
    gl_PointSize = 10.0;
 }