precision mediump float;
uniform samplerCube uCubemapInSampler;
uniform samplerCube uCubemapOutSampler;
uniform float uBlendFactor;
varying vec3 LookupVector;

void main()
{
    gl_FragColor = textureCube(mSamplerCube, oPosition);
}