precision mediump float;
uniform samplerCube uCubemapInSampler;
uniform vec3 uLightPos;
uniform vec3 uEyePos;

varying vec4 Color;
varying vec3 FragPos;
varying vec3 Normal;

void main()
{
    vec3 normal = normalize(Normal);
    vec3 lightDir = normalize(uLightPos-FragPos.xyz);
    vec3 viewDir = normalize(uEyePos-FragPos.xyz);
    vec3 lightReflect = normalize(reflect(-lightDir,normal));

    float refrIndex = 1.0/2.42;
    vec3 I = normalize(FragPos.xyz-uEyePos);
    vec3 viewRefract = normalize(refract(I,normal,refrIndex));

    vec4 reflSample = textureCube(uCubemapInSampler, viewRefract);
    float specTerm = max( pow( dot(viewDir, lightReflect), 8.0), 0.0);

    gl_FragColor = reflSample+specTerm;
}