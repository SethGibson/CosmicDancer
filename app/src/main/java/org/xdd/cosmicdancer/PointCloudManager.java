package org.xdd.cosmicdancer;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PointCloudManager
{
    private Context         mContext;
    private ShaderManager   mShaderMgr;

    private final String    mUniformModelM = "uModelMatrix";
    private int             mLocModelM;
    private final String    mUniformViewM = "uViewMatrix";
    private int             mLocViewM;
    private final String    mUniformProjM = "uProjMatrix";
    private int             mLocProjM;
    private final String    mUniformNormM = "uNormalMatrix";
    private int             mLocNormM;

    private final String    mUniformLightPos = "uLightPos";
    private int             mLocLightPos;

    private final String    mAttribPos = "vPosition";
    private int             mLocPos;
    private final String    mAttribNorm = "vNormal";
    private int             mLocNorm;

    private final String    mAttribIPos = "iPosition";
    private int             mLocIPos;
    private final String    mAttribIColor = "iColor";
    private int             mLocIColor;
    private final String    mAttribISize = "iSize";
    private int             mLocISize;

    private FloatBuffer     mInstanceData;
    private FloatBuffer     mMeshData;
    private static int      S_SIZE_POS = 3;
    private static int      S_SIZE_COLOR = 3;
    private static int      S_SIZE_NORM = 3;
    private static int      S_SIZE_SCALE = 1;
    private static int      S_SIZE_FLOAT = 4;
    private static int      S_VERTEX_STRIDE;
    private static int      S_INSTANCE_STRIDE;

    private float[]         mModelMatrix = new float[16];
    private float[]         mViewMatrix = new float[16];
    private float[]         mProjMatrix = new float[16];
    private float[]         mNormMatrix = new float[16];

    private int             mNumInstances;
    private int             mInstanceSize;
    private int             mVertexSize;

    public PointCloudManager(Context pContext, ShaderManager pShaderMgr)
    {
        mContext = pContext;
        mShaderMgr = pShaderMgr;

        mInstanceSize = S_SIZE_POS+S_SIZE_COLOR+S_SIZE_SCALE;
        S_INSTANCE_STRIDE = mInstanceSize*S_SIZE_FLOAT;

        mVertexSize = S_SIZE_POS+S_SIZE_NORM;
        S_VERTEX_STRIDE = mVertexSize*S_SIZE_FLOAT;
    }

    public int CreateProgram()
    {
        int vertID = mShaderMgr.CreateShader("vertex_instance.glsl", GL_VERTEX_SHADER);
        int fragID = mShaderMgr.CreateShader("frag_instance.glsl", GL_FRAGMENT_SHADER);
        int programID = mShaderMgr.CreateProgram(vertID, fragID);

        glUseProgram(programID);

        //get uniforms
        mLocModelM = glGetUniformLocation(programID, mUniformModelM);
        mLocViewM = glGetUniformLocation(programID, mUniformViewM);
        mLocProjM = glGetUniformLocation(programID, mUniformProjM);
        mLocNormM = glGetUniformLocation(programID, mUniformNormM);
        mLocLightPos = glGetUniformLocation(programID, mUniformLightPos);

        //get attributes
        mLocPos = glGetAttribLocation(programID, mAttribPos);
        mLocNorm  = glGetAttribLocation(programID, mAttribNorm);
        mLocIColor = glGetAttribLocation(programID, mAttribIColor);
        mLocIPos = glGetAttribLocation(programID, mAttribIPos);
        mLocISize = glGetAttribLocation(programID,mAttribISize);

        return programID;
    }

    public int CreateInstanceBuffer(float[] pPointData)
    {
        mInstanceData = ByteBuffer.allocateDirect(pPointData.length * S_SIZE_FLOAT)
                                    .order(ByteOrder.nativeOrder())
                                    .asFloatBuffer();

        mInstanceData.put(pPointData);
        mInstanceData.position(0);

        //setup as a single VBO for drawing test
        int[] cVoIDs = new int[1];
        glGenBuffers(1, cVoIDs, 0);
        glBindBuffer(GL_ARRAY_BUFFER, cVoIDs[0]);
        glBufferData(GL_ARRAY_BUFFER, pPointData.length * S_SIZE_FLOAT, mInstanceData, GL_STATIC_DRAW /*GL_DYNAMIC_DRAW*/);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        mNumInstances = pPointData.length/mInstanceSize;
        return cVoIDs[0];
    }

    public int CreateMeshBuffer(float pPointData[], int pVboID)
    {
        mMeshData = ByteBuffer.allocateDirect(pPointData.length * S_SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mMeshData.put(pPointData);
        mMeshData.position(0);

        int[] cVoIDs = new int[2];

        //Mesh Buffer
        glGenVertexArrays(1, cVoIDs, 0);
        glGenBuffers(1, cVoIDs, 1);
        glBindVertexArray(cVoIDs[0]);
        glBindBuffer(GL_ARRAY_BUFFER, cVoIDs[1]);
        glBufferData(GL_ARRAY_BUFFER, pPointData.length * S_SIZE_FLOAT, mMeshData, GL_STATIC_DRAW /*GL_DYNAMIC_DRAW*/);

        //Enable Position and Normals per-vertex
        glEnableVertexAttribArray(mLocPos);
        glVertexAttribPointer(mLocPos, 3, GL_FLOAT, false, S_VERTEX_STRIDE, 0);
        glEnableVertexAttribArray(mLocNorm);
        glVertexAttribPointer(mLocNorm, 3, GL_FLOAT, false, S_VERTEX_STRIDE, S_SIZE_POS * S_SIZE_FLOAT);
        //

        //Enable Position, Color, and Size per-instance
        glBindBuffer(GL_ARRAY_BUFFER, pVboID);
        glEnableVertexAttribArray(mLocIPos);
        glVertexAttribPointer(mLocIPos, 3, GL_FLOAT, false, S_INSTANCE_STRIDE, 0);
        glVertexAttribDivisor(mLocIPos, 1);
        glEnableVertexAttribArray(mLocIColor);
        glVertexAttribPointer(mLocIColor, 3, GL_FLOAT, false, S_INSTANCE_STRIDE, S_SIZE_POS*S_SIZE_FLOAT);
        glVertexAttribDivisor(mLocIColor, 1);
        glEnableVertexAttribArray(mLocISize);
        glVertexAttribPointer(mLocISize, 1, GL_FLOAT, false, S_INSTANCE_STRIDE, (S_SIZE_POS + S_SIZE_COLOR) * S_SIZE_FLOAT);
        glVertexAttribDivisor(mLocISize, 1);

        //Cleanup
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return cVoIDs[0];
    }

    public void SetMatrix(SceneManager.Camera pCamera, float pAspect, float[] pTrans, float[] pRot)
    {
        setIdentityM(mModelMatrix, 0);
        translateM(mModelMatrix, 0, pTrans[0], pTrans[1], pTrans[2]);
        rotateM(mModelMatrix, 0, pRot[0], 1.0f, 0.0f, 0.0f);
        rotateM(mModelMatrix, 0, pRot[1], 0.0f, 1.0f, 0.0f);
        rotateM(mModelMatrix, 0, pRot[2], 0.0f, 0.0f, 1.0f);

        setIdentityM(mViewMatrix, 0);
        setLookAtM(mViewMatrix, 0, pCamera.Position[0], pCamera.Position[1], pCamera.Position[2],
                pCamera.LookAt[0], pCamera.LookAt[1], pCamera.LookAt[2],
                pCamera.Up[0], pCamera.Up[1], pCamera.Up[2]);

        setIdentityM(mProjMatrix, 0);
        perspectiveM(mProjMatrix, 0, pCamera.FOV, pAspect, pCamera.NearClip, pCamera.FarClip);

        setIdentityM(mNormMatrix, 0);
        invertM(mNormMatrix,0,mModelMatrix,0);
    }

    public void DrawPointCloud(float[] pLightPos, int pInstanceVAO, int pProgID)
    {
        glUseProgram(pProgID);
        glUniformMatrix4fv(mLocModelM, 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(mLocViewM, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocProjM, 1, false, mProjMatrix, 0);
        glUniformMatrix4fv(mLocNormM, 1, true, mNormMatrix, 0);
        glUniform3fv(mLocLightPos,1,pLightPos,0);
        glBindVertexArray(pInstanceVAO);
        glDrawArraysInstanced(GL_TRIANGLES,0,36,mNumInstances);
        glBindVertexArray(0);
    }
}
