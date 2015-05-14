uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 vPosition;
attribute vec3 vNormal;

attribute vec4 iColor;
attribute vec3 iPosition;
attribute float iSize;

varying vec4 Color;
varying vec3 Normal;

void main()
{
    Color = iColor;
    Normal = vNormal;
    gl_Position = uProjMatrix*uViewMatrix*uModelMatrix*(vec4(vPosition.xyz*vec3(iSize),1.0)+vec4(iPosition,1.0));
}