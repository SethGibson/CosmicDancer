uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 vPosition;
attribute vec3 vNormal;

attribute vec3 iPosition;

varying vec3 FragPos;
varying vec3 Normal;

void main()
{
    vec3 finalPosition = vPosition.xyz+iPosition;
    mat4 normalMatrix = mat4(mat3(uViewMatrix*uModelMatrix));

    Normal = vec3(normalMatrix*vec4(vNormal,0.0));
    FragPos = vec3(uViewMatrix*uModelMatrix*vPosition);

    gl_Position = uProjMatrix*uViewMatrix*uModelMatrix*vec4(finalPosition,1.0);
}