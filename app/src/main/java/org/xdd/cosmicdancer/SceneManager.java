package org.xdd.cosmicdancer;

public class SceneManager
{
    public static class Camera
    {
        float[] Position = new float[3];
        float[] LookAt = new float[3];
        float[] Up = new float[3];
        float   NearClip;
        float   FarClip;
        float   FOV;

        public Camera(float pEyeX, float pEyeY, float pEyeZ,
                      float pLookX, float pLookY, float pLookZ,
                      float pUpX, float pUpY, float pUpZ,
                      float pNear, float pFar, float pFOV)
        {
            Position = new float[]{pEyeX,pEyeY,pEyeZ};
            LookAt = new float[]{pLookX,pLookY,pLookZ};
            Up = new float[]{pUpX,pUpY,pUpZ};
            NearClip = pNear;
            FarClip = pFar;
            FOV = pFOV;
        }
    }
}
