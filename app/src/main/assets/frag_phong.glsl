precision mediump float;
uniform vec3 uLightPos;
uniform vec3 uEyePos;

varying vec3 FragPos;
varying vec3 Normal;


void main()
{
    vec3 uColor = vec3(0.25,0,0);
    vec3 normal = normalize(Normal);
    vec3 lightDir = normalize(uLightPos-FragPos.xyz);
    vec3 eyeDir = normalize(uEyePos-FragPos.xyz);
    vec3 reflectDir = reflect(-lightDir,normal);

    float diffContrib = max(dot(normal, lightDir), 0.0);
    float specContrib = max( pow( dot(eyeDir, reflectDir), 8.0), 0.0);

    gl_FragColor = vec4(uColor*diffContrib+specContrib,1.0);
}