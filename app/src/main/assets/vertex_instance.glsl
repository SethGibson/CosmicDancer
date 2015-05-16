uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;
uniform mat4 uNormalMatrix;

attribute vec4 vPosition;
attribute vec3 vNormal;

attribute vec4 iColor;
attribute vec3 iPosition;
attribute float iSize;

varying vec3 WorldPos;
varying vec4 Color;
varying vec3 Normal;

void main()
{
    Color = iColor;

    vec3 finalPosition = vPosition.xyz*vec3(iSize)+iPosition;

    Normal = vec3(uViewMatrix*uModelMatrix*vec4(vNormal,0.0));
    WorldPos = vec3(uViewMatrix*uModelMatrix*vPosition);
    gl_Position = uProjMatrix*uViewMatrix*uModelMatrix*vec4(finalPosition,1.0);
}