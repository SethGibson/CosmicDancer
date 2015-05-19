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

    vec3 I = normalize(FragPos.xyz-uEyePos);
    vec3 viewReflect = normalize(reflect(I,normal));

    vec4 reflSample = textureCube(uCubemapInSampler, viewReflect);

    float diffContrib = max(dot(normal, lightDir), 0.0);
    float specTerm = max( pow( dot(viewDir, lightReflect), 8.0), 0.0);
    vec3 specContrib = reflSample.rgb*specTerm;

    gl_FragColor = vec4(Color.rgb*diffContrib+specContrib,1.0);
}