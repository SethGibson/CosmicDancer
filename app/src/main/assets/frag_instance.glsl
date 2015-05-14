precision mediump float;
varying vec4 Color;
varying vec3 Normal;
void main()
{
    float normalR = (Normal.x+1.0)*0.5;
    float normalG = (Normal.y+1.0)*0.5;
    float normalB = (Normal.z+1.0)*0.5;

    vec3 fColor = Color.xyz*vec3(0.01)+vec3(normalR,normalG,normalB);
    gl_FragColor = vec4(fColor,1.0);
}