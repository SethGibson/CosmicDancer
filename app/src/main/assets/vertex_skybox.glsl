uniform mat4    uModelMatrix;
uniform mat4    uViewMatrix;
uniform mat4    uProjMatrix;

attribute vec4  vPosition;

varying vec3    LookupVector;

void main()
{
    LookupVector = vec3(uModelMatrix*vPosition);

    mat4 skyboxMatrix = mat4(mat3(uViewMatrix));
    gl_Position = uProjMatrix * skyboxMatrix * vec4(vPosition.xyz,1.0);
    gl_Position = gl_Position.xyww;
}