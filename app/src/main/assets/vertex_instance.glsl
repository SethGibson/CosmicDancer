uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 vPosition;
attribute vec3 vNormal;

attribute vec4 iColor;
attribute vec3 iPosition;
attribute float iSize;

varying vec4 Color;
varying vec3 FragPos;
varying vec3 Normal;

void main()
{
    vec3 finalPosition = vPosition.xyz*vec3(iSize)+iPosition;
    mat4 normalMatrix = mat4(mat3(uViewMatrix*uModelMatrix));

    Color = iColor;
    Normal = vec3(normalMatrix*vec4(vNormal,0.0));
    FragPos = vec3(uViewMatrix*uModelMatrix*vPosition);

    gl_Position = uProjMatrix*uViewMatrix*uModelMatrix*vec4(finalPosition,1.0);
}