uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 vPosition;
attribute vec3 vNormal;
attribute vec3 iPosition;

varying vec3 Normal;
varying vec3 FragPos;

void main()
{
    mat4 normalMatrix = mat4(mat3(uViewMatrix*uModelMatrix));
    vec3 finalPosition = vPosition.xyz+iPosition;

    Normal = vec3(normalMatrix*vec4(vNormal,0.0));
    FragPos = vPosition.xyz;
    gl_Position = uProjMatrix*uViewMatrix*uModelMatrix*vec4(finalPosition,1.0);
}