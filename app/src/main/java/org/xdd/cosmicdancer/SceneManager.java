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

    public static float[] GetPlaneVerts() {
        float[] vertices = new float[]
                {
                        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                        -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,

                        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                        0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                };

        return vertices;
    }

    public static float[] GetCubeVerts() {
        float[] meshData = new float[]
                {
                        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                        0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                        0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                        0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                        -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

                        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

                        -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
                        -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                        -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                        -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                        -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
                        -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,

                        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
                        0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
                        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,

                        -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
                        0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
                        0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
                        0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
                        -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
                        -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,

                        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                        0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                        0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                        0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f
                };
        return meshData;
    }

    public static float[] GetHedronVerts() {
        final int S_NUM_VERTS = 60;

        float[] verts = new float[S_NUM_VERTS * 3];
        float[] normals = new float[S_NUM_VERTS * 3];
        float[] vertices = new float[S_NUM_VERTS * 6];

        int[] indices = new int[]
                {
                        0, 11, 5, 0, 5, 1, 0, 1, 7, 0, 7, 10, 0, 10, 11, 1, 5, 9, 5, 11,
                        4, 11, 10, 2, 10, 7, 6, 7, 1, 8, 3, 9, 4, 3, 4, 2, 3, 2, 6, 3,
                        6, 8, 3, 8, 9, 4, 9, 5, 2, 4, 11, 6, 2, 10, 8, 6, 7, 9, 8, 1
                };

        final float t = (float) ((1.0f + Math.sqrt(5.0f)) / 2.0f);
        int id = 0;

        float[] rawverts = new float[]
                {
                        -1.0f, t, 0.0f, 1.0f, t, 0.0f, -1.0f, -t, 0.0f, 1.0f, -t, 0.0f,
                        0.0f, -1.0f, t, 0.0f, 1.0f, t, 0.0f, -1.0f, -t, 0.0f, 1.0f, -t,
                        t, 0.0f, -1.0f, t, 0.0f, 1.0f, -t, 0.0f, -1.0f, -t, 0.0f, 1.0f
                };

        //build unindexed buffer for per-vert normals
        id = 0;
        for (int i = 0; i < indices.length; i++) {
            int vID = indices[i] * 3;
            float[] vert = {rawverts[vID], rawverts[vID + 1], rawverts[vID + 2]};
            vert = normalize3f(vert);
            verts[id++] = vert[0];
            verts[id++] = vert[1];
            verts[id++] = vert[2];
        }

        //calculate normal
        id = 0;
        int jMax = (S_NUM_VERTS * 3) - 9;
        for (int j = 0; j <= jMax; j += 9)
        {
            int id0 = j;
            int id1 = j+3;
            int id2 = j+6;

            float[] vA = {verts[id0],verts[id0+1],verts[id0+2]};
            float[] vB = {verts[id1],verts[id1+1],verts[id1+2]};
            float[] vC = {verts[id2],verts[id2+1],verts[id2+2]};

            float[] magA = {vB[0]-vA[0], vB[1]-vA[1], vB[2]-vA[2]};
            float[] magB = {vC[0]-vB[0], vC[1]-vB[1], vC[2]-vB[2]};

            float[] normal = normalize3f(cross3f(magA,magB));
            normals[id++] = normal[0];
            normals[id++] = normal[1];
            normals[id++] = normal[2];
            normals[id++] = normal[0];
            normals[id++] = normal[1];
            normals[id++] = normal[2];
            normals[id++] = normal[0];
            normals[id++] = normal[1];
            normals[id++] = normal[2];
        }

        //interleave verts and normals
        for (int m = 0; m < S_NUM_VERTS; ++m) {
            vertices[m * 6] = verts[m * 3];
            vertices[m * 6 + 1] = verts[m * 3 + 1];
            vertices[m * 6 + 2] = verts[m * 3 + 2];
            vertices[m * 6 + 3] = normals[m * 3];
            vertices[m * 6 + 4] = normals[m * 3 + 1];
            vertices[m * 6 + 5] = normals[m * 3 + 2];
        }

        return vertices;
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

    public static void GetCircleVerts(int pNumPoints, float[] pOutVerts, byte[] pOutIndices) {
        float[] vertices = new float[(pNumPoints + 1) * 6];
        float interval = 360.0f / pNumPoints;

        vertices[0] = 0.0f;
        vertices[1] = 0.0f;
        vertices[2] = 0.0f;
        vertices[3] = 0.0f;
        vertices[4] = 0.0f;
        vertices[5] = 1.0f;
        for (int i = 1; i <= pNumPoints; ++i) {
            float angle = (float) i * interval;
            vertices[i * 6] = (float) Math.cos(Math.toRadians(angle));
            vertices[i * 6 + 1] = (float) Math.sin(Math.toRadians(angle));
            vertices[i * 6 + 2] = 0.0f;
            vertices[i * 6 + 3] = 0.0f;
            vertices[i * 6 + 4] = 0.0f;
            vertices[i * 6 + 5] = 1.0f;
        }

        byte[] indices = new byte[pNumPoints * 3];
        for (int j = 0; j < pNumPoints; j++) {
            byte id0 = 0;
            byte id1 = (byte) (j + 1);
            byte id2 = (byte) (j + 2);
            if ((j + 2) > pNumPoints)
                id2 = (byte) ((j + 2) % pNumPoints);
            indices[j * 3] = id0;
            indices[j * 3 + 1] = id1;
            indices[j * 3 + 2] = id2;
        }

        System.arraycopy(vertices, 0, pOutVerts, 0, vertices.length);
        System.arraycopy(indices, 0, pOutIndices, 0, indices.length);
    }

    private static float[] normalize3f(float[] pVector)
    {
        final float L = (float) (Math.abs(Math.sqrt((pVector[0] * pVector[0]) + (pVector[1] * pVector[1]) + (pVector[2] * pVector[2]))));
        final float normX = pVector[0] / L;
        final float normY = pVector[1] / L;
        final float normZ = pVector[2] / L;

        return new float[]{normX, normY, normZ};
    }

    private static float[] cross3f(float[] pVectorA, float[] pVectorB)
    {
        float[] cross = {0.0f,0.0f,0.0f};
        cross[0] = pVectorA[1]*pVectorB[2]-pVectorA[2]*pVectorB[1];
        cross[1] = pVectorA[2]*pVectorB[0]-pVectorA[0]*pVectorB[2];
        cross[2] = pVectorA[0]*pVectorB[1]-pVectorA[1]*pVectorB[0];

        return cross;
    }
}
