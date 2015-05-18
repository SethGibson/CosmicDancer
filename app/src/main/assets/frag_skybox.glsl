precision mediump float;
uniform samplerCube uCubemapInSampler;

varying vec3 LookupVector;

void main()
{
    gl_FragColor = textureCube(uCubemapInSampler, LookupVector);
}