precision mediump float;
uniform vec3 uLightPos;

varying vec4 Color;
varying vec3 Normal;
varying vec3 WorldPos;

void main()
{
    vec3 lightDir = normalize(uLightPos-WorldPos);
    float diffContrib = max(dot(Normal,lightDir),0.0);
    gl_FragColor = max(Color*diffContrib,(Color*0.25));
}