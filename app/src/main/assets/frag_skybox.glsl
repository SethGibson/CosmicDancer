precision mediump float;
uniform samplerCube mSamplerCube;
varying vec3 oPosition;

void main()
{
    gl_FragColor = textureCube(mSamplerCube, oPosition);
}