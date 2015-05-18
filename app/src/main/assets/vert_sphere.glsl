uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec3 vPosition;
attribute vec3 vNormal;

varying vec3 FragPos;
varying vec3 Normal;

void main()
{
    mat4 normalMatrix = mat4(mat3(uViewMatrix*uModelMatrix));
    Normal = vec3(normalMatrix*vec4(vNormal,0.0));
    FragPos = vec3(uViewMatrix*uModelMatrix*vec4(vPosition,1.0));
    gl_Position = uProjMatrix*uViewMatrix*uModelMatrix*vec4(vPosition,1.0);
}