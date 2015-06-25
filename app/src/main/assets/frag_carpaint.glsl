precision mediump float;
uniform samplerCube uCubemapInSampler;
uniform vec3 uLightPos;

varying vec3 Normal;
varying vec3 FragPos;

void main()
{
    vec3 normal = normalize(Normal);
    vec4 r = textureCube(uCubemapInSampler,FragPos);
    gl_FragColor = vec4(vec3(r),1.0);
}