package org.xdd.cosmicdancer;

import static android.opengl.GLES30.*;
import static android.opengl.GLUtils.*;
import static android.opengl.Matrix.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SkyboxManager
{
    private Context         mContext;
    private ShaderManager   mShaderMgr;
    private final float[]   mSkyboxVerts;
    FloatBuffer             mSkyboxBuffer;

    private static int      S_SIZE_FLOAT = 4;
    private static int      S_SIZE_POS = 3;
    private static int      S_BUFFER_SIZE;

    private float[]         mModelMatrix = new float[16];
    private float[]         mViewMatrix = new float[16];
    private float[]         mProjMatrix = new float[16];
    private final String    mUniformModel = "uModelMatrix";
    private final String    mUniformView = "uViewMatrix";
    private final String    mUniformProj="uProjMatrix";
    private final String    mUniformCubeIn = "uCubemapInSampler";
    private final String    mUniformCubeOut = "uCubemapOutSampler";
    private final String    mUniformBlendFactor = "uBlendFactor";
    private int             mLocModel;
    private int             mLocView;
    private int             mLocProj;
    private int             mLocCubeIn;
    private int             mLocCubeOut;
    private int             mLocCubeBlend;

    private final String    mAttribPos = "vPosition";
    private int             mLocPos;

    public SkyboxManager(Context pContext, ShaderManager pShaderMgr)
    {
        mContext = pContext;
        mShaderMgr = pShaderMgr;
        mSkyboxVerts = new float[]
        {
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };

        S_BUFFER_SIZE = mSkyboxVerts.length * S_SIZE_POS * S_SIZE_FLOAT;
        mSkyboxBuffer = ByteBuffer.allocateDirect(S_BUFFER_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mSkyboxBuffer.put(mSkyboxVerts);
        mSkyboxBuffer.position(0);
    }

    public int CreateProgram(String pVertShader, String pFragShader)
    {
        int skyVertID = mShaderMgr.CreateShader(pVertShader, GL_VERTEX_SHADER);
        int skyFragID = mShaderMgr.CreateShader(pFragShader, GL_FRAGMENT_SHADER);
        int programID = mShaderMgr.CreateProgram(skyVertID, skyFragID);
        if(programID==0){
            return 0;
        }
        glUseProgram(programID);

        mLocModel = glGetUniformLocation(programID, mUniformModel);
        mLocView = glGetUniformLocation(programID, mUniformView);
        mLocProj = glGetUniformLocation(programID, mUniformProj);
        mLocCubeIn = glGetUniformLocation(programID, mUniformCubeIn);
        mLocCubeOut = glGetUniformLocation(programID, mUniformCubeOut);
        mLocPos = glGetAttribLocation(programID, mAttribPos);
        return programID;
    }

    public int CreateSkyboxMesh()
    {
        final int[] cVaoID = new int[1];
        final int[] cVboID = new int[1];

        glGenVertexArrays(1, cVaoID, 0);
        glGenBuffers(1, cVboID, 0);

        glBindVertexArray(cVaoID[0]);
        glBindBuffer(GL_ARRAY_BUFFER, cVboID[0]);
        glBufferData(GL_ARRAY_BUFFER, S_BUFFER_SIZE, mSkyboxBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(mLocPos, S_SIZE_POS, GL_FLOAT, false, S_SIZE_POS * S_SIZE_FLOAT, 0);
        glEnableVertexAttribArray(mLocPos);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return cVaoID[0];
    }

    public int CreateCubemap(int[] pBitmapIDs)
    {
        final int[] cTexID = new int[1];
        glGenTextures(1, cTexID, 0);

        if(cTexID[0]==0)
        {
            Log.e("CreateCubemap", "Unable to generate cube map object");
            return 0;
        }

        final Bitmap[] cCubemaps = new Bitmap[6];
        final BitmapFactory.Options cOptions = new BitmapFactory.Options();
        cOptions.inScaled = false;

        for(int i=0;i<6;++i)
        {
            cCubemaps[i] = BitmapFactory.decodeResource(mContext.getResources(), pBitmapIDs[i],cOptions);
        }

        glBindTexture(GL_TEXTURE_CUBE_MAP,cTexID[0]);
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cCubemaps[0], 0);
        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cCubemaps[1], 0);
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cCubemaps[2], 0);
        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cCubemaps[3], 0);
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cCubemaps[4], 0);
        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cCubemaps[5], 0);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        for(Bitmap b : cCubemaps)
            b.recycle();

        return cTexID[0];
    }


    public void SetMatrices(SceneManager.Camera pCamera, float pAspect)
    {
        setIdentityM(mModelMatrix, 0);
        translateM(mModelMatrix, 0, 0, 0, 0);

        setIdentityM(mViewMatrix, 0);
        /*
        setLookAtM(mViewMatrix, 0, pCamera.Position[0], pCamera.Position[1], pCamera.Position[2],
                pCamera.LookAt[0], pCamera.LookAt[1], pCamera.LookAt[2],
                pCamera.Up[0], pCamera.Up[1], pCamera.Up[2]);
        */
        setIdentityM(mProjMatrix, 0);
        perspectiveM(mProjMatrix, 0, pCamera.FOV, pAspect, pCamera.NearClip, pCamera.FarClip);
    }

    public void DrawSkybox(int pVaoID, int pProgID, int pCubemapID)
    {
        glDisable(GL_DEPTH_TEST);

        glUseProgram(pProgID);
        glUniformMatrix4fv(mLocModel, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocView, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocProj, 1, false, mViewMatrix, 0);


        glActiveTexture(GL_TEXTURE0);
        glUniform1i(mLocCubeIn, 0);

        glBindVertexArray(pVaoID);
        glBindTexture(GL_TEXTURE_CUBE_MAP, pCubemapID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glBindVertexArray(0);

        glEnable(GL_DEPTH_TEST);
    }

    public void DrawSkyboxBlended(int pVaoID, int pProgID, int pCubemapInID, int pCubemapOutID, float pFactor)
    {
        glDisable(GL_DEPTH_TEST);

        glUseProgram(pProgID);
        glUniformMatrix4fv(mLocModel, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocView, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocProj, 1, false, mViewMatrix, 0);


        glActiveTexture(GL_TEXTURE0);
        glActiveTexture(GL_TEXTURE1);
        glUniform1i(mLocCubeIn, 0);
        glUniform1i(mLocCubeOut, 1);
        glUniform1f(mLocCubeBlend, pFactor);
        glBindVertexArray(pVaoID);
        glBindTexture(GL_TEXTURE_CUBE_MAP, pCubemapInID);
        glBindTexture(GL_TEXTURE_CUBE_MAP, pCubemapOutID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        glBindVertexArray(0);

        glEnable(GL_DEPTH_TEST);
    }
}
