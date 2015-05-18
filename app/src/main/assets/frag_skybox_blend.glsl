precision mediump float;
uniform samplerCube uCubemapInSampler;
uniform samplerCube uCubemapOutSampler;

varying vec3 LookupVector;

void main()
{
    gl_FragColor = textureCube(mSamplerCube, oPosition);
}