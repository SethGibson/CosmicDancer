precision mediump float;
uniform samplerCube uCubemapSampler;
uniform vec3 uLightPos;
uniform vec3 uEyePos;

varying vec3 FragPos;
varying vec3 Normal;

void main()
{
    vec3 uColor = vec3(0.5,0.5,0.5);
    vec3 normal = normalize(Normal);
    vec3 lightDir = normalize(uLightPos-FragPos.xyz);
    vec3 viewDir = normalize(uEyePos-FragPos.xyz);
    vec3 lightReflect = normalize(reflect(-lightDir,normal));

    vec3 I = normalize(FragPos.xyz-uEyePos);
    vec3 viewReflect = normalize(reflect(I,normal));

    vec4 reflSample = textureCube(uCubemapSampler, viewReflect);
    vec3 reflContrib = reflSample.rgb*0.5;
    float diffContrib = max(dot(normal, lightDir), 0.0);
    float specContrib = max( pow( dot(viewDir, lightReflect), 8.0), 0.0);

    gl_FragColor = vec4(uColor*diffContrib+specContrib+reflContrib,1.0);
}