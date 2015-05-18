precision mediump float;
uniform samplerCube uCubemapSampler;
uniform vec3 uLightPos;
uniform vec3 uEyePos;

varying vec3 FragPos;
varying vec3 Normal;

void main()
{
    vec3 uColor = vec3(0.1,0.1,0.1);
    vec3 normal = normalize(Normal);
    vec3 lightDir = normalize(uLightPos-FragPos.xyz);
    vec3 viewDir = normalize(uEyePos-FragPos.xyz);
    vec3 lightReflect = normalize(reflect(-lightDir,normal));

    vec3 I = normalize(FragPos.xyz-uEyePos);
    vec3 viewReflect = normalize(reflect(I,normal));

    float diffContrib = max(dot(normal, lightDir), 0.0);
    float specContrib = max( pow( dot(viewDir, lightReflect), 8.0), 0.0);
    float fresContrib = 1.0-pow( max(dot(viewDir, normal), 0.0), 1.0);
    vec4 reflSample = textureCube(uCubemapSampler, viewReflect);
    vec3 reflContrib = reflSample.rgb*0.1;
    vec3 hiLite = mix(reflContrib, vec3(0.55), fresContrib);

    gl_FragColor = vec4(uColor*diffContrib+specContrib+hiLite,1.0);
}