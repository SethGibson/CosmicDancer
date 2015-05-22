package org.xdd.cosmicdancer;

import java.lang.Math;

public class SceneManager {
    public static class Camera {
        float[] Position = new float[3];
        float[] LookAt = new float[3];
        float[] Up = new float[3];
        float NearClip;
        float FarClip;
        float FOV;

        public Camera(float pEyeX, float pEyeY, float pEyeZ,
                      float pLookX, float pLookY, float pLookZ,
                      float pUpX, float pUpY, float pUpZ,
                      float pNear, float pFar, float pFOV) {
            Position = new float[]{pEyeX, pEyeY, pEyeZ};
            LookAt = new float[]{pLookX, pLookY, pLookZ};
            Up = new float[]{pUpX, pUpY, pUpZ};
            NearClip = pNear;
            FarClip = pFar;
            FOV = pFOV;
        }
    }

    public static void GetSphereVerts(float[] pMeshData, int[] pIndexData, float pRadius, int pSubdAxis, int pSubdHeight) {
        final int numVerts = pSubdAxis * pSubdHeight;
        final int numIndices = (pSubdAxis * pSubdHeight + pSubdAxis);
        float[] vertices = new float[numVerts * 6];
        int[] indices = new int[numIndices * 6];

        int id = 0;
        for (int dy = 0; dy < pSubdHeight; ++dy) {
            float yStep = dy / (float) pSubdHeight;
            float phi = (float) (yStep * Math.PI);

            //tf
            for (int dx = 0; dx < pSubdAxis; ++dx) {
                float xStep = dx / (float) pSubdAxis;
                float theta = (float) (xStep * (2 * Math.PI));

                float vx = (float) (Math.cos(theta) * Math.sin(phi));
                float vy = (float) Math.cos(phi);
                float vz = (float) (Math.sin(theta) * Math.sin(phi));

                float[] normal = normalize3f(new float[]{vx, vy, vz});
                vertices[id++] = vx * pRadius;
                vertices[id++] = vy * pRadius;
                vertices[id++] = vz * pRadius;
                vertices[id++] = normal[0];
                vertices[id++] = normal[1];
                vertices[id++] = normal[2];
            }
        }

        id = 0;
        for (int i = 0; i < numIndices; ++i) {
            indices[id++] = i;
            indices[id++] = i + pSubdAxis + 1;
            indices[id++] = i + pSubdAxis;

            indices[id++] = i + pSubdAxis + 1;
            indices[id++] = i;
            indices[id++] = i + 1;
        }

        System.arraycopy(vertices, 0, pMeshData, 0, vertices.length);
        System.arraycopy(indices, 0, pIndexData, 0, indices.length);
    }

    private static float[] normalize3f(float[] pVector)
    {
        final float L = (float) (Math.abs(Math.sqrt((pVector[0] * pVector[0]) + (pVector[1] * pVector[1]) + (pVector[2] * pVector[2]))));
        final float normX = pVector[0] / L;
        final float normY = pVector[1] / L;
        final float normZ = pVector[2] / L;

        return new float[]{normX, normY, normZ};
    }
}
