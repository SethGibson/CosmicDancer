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

    public static float[] GetPlaneVerts()
    {
        float[] vertices = new float[]
        {
            0.5f,0.5f,0.0f,    0.0f,0.0f,1.0f,
            -0.5f,0.5f,0.0f,   0.0f,0.0f,1.0f,
            -0.5f,-0.5f,0.0f,     0.0f,0.0f,1.0f,

            0.5f,0.5f,0.0f,    0.0f,0.0f,1.0f,
            -0.5f,-0.5f,0.0f,     0.0f,0.0f,1.0f,
            0.5f,-0.5f,0.0f,     0.0f,0.0f,1.0f,
        };

        return vertices;
    }

    public static float[] GetCubeVerts()
    {
        float[] meshData = new float[]
        {
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,

            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
            0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,

            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,

            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
            0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
            0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,

            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
            0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,

            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f
        };
        return meshData;
    }

    /*
    public static void GetHedronVerts(float[] pOutVerts, byte[] pOutIndices)
    {
        float t = (float)((1.0f+Math.sqrt(5.0))/2.0);
        float[] vertices = new float[]
        {
            -1,  t,  0,
             1,  t,  0,
            -1, -t,  0,
            1, -t,  0,

            0, -1,  t,
            0,  1,  t,
            0, -1, -t,
            0,  1, -t,

            t,  0, -1,
            t,  0,  1,
            -t,  0, -1,
            -t,  0,  1,
         }
    }

    public static float[] GetSphereVerts()
    {

    }
    */

    public static void GetCircleVerts(int pNumPoints, float[] pOutVerts, byte[] pOutIndices)
    {
        float[] vertices = new float[(pNumPoints+1)*6];
        float interval = 360.0f/pNumPoints;

        vertices[0]=0.0f;vertices[1]=0.0f;vertices[2]=0.0f;
        for(int i=1;i<=pNumPoints;++i)
        {
            float angle = (float)i*interval;
            vertices[i*6] = (float)Math.cos(Math.toRadians(angle));
            vertices[i*6+1] = (float)Math.sin(Math.toRadians(angle));
            vertices[i*6+2] = 0.0f;
            vertices[i*6+3] = 0.0f;
            vertices[i*6+4] = 0.0f;
            vertices[i*6+5] = 1.0f;
        }

        byte[] indices = new byte[pNumPoints*3];
        for(int j=0;j<pNumPoints;j++)
        {
            byte id0 = 0;
            byte id1 = (byte)(j+1);
            byte id2 = (byte)(j+2);
            if((j+2)>pNumPoints)
                id2 = (byte)((j+2)%pNumPoints);
            indices[j*3]=id0;
            indices[j*3+1]=id1;
            indices[j*3+2]=id2;
        }

        System.arraycopy(vertices,0,pOutVerts,0,vertices.length);
        System.arraycopy(indices,0,pOutIndices,0,indices.length);
    }
}
