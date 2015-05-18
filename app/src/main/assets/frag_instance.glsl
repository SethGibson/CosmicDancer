precision mediump float;
uniform vec3 uLightPos;

varying vec4 Color;
varying vec3 Normal;
varying vec3 FragPos;
varying vec3 LookupVector;

void main()
{
    vec3 normal = normalize(Normal);
    vec3 lightDir = normalize(uLightPos-FragPos);
    float diffContrib = max(dot(normal,lightDir),0.0);
    gl_FragColor = Color*diffContrib+Color*0.2;
}