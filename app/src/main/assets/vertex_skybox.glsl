uniform mat4    mViewProjection;
attribute vec4  vPosition;
varying vec3    oPosition;

void main()
{
    oPosition = vPosition.xyz;
    oPosition.z = -oPosition.z;
    gl_Position = mViewProjection * vec4(vPosition.xyz,1.0);
    gl_Position = gl_Position.xyww;
}