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

    private float[]         mMatVP = new float[16];
    private final String    mUniformVP = "mViewProjection";
    private int             mLocVP;
    private final String    mUniformCube = "mSamplerCube";
    private int             mLocCube;
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

    public int CreateProgram()
    {
        int skyVertID = mShaderMgr.CreateShader("vertex_skybox.glsl", GL_VERTEX_SHADER);
        int skyFragID = mShaderMgr.CreateShader("frag_skybox.glsl", GL_FRAGMENT_SHADER);
        int programID = mShaderMgr.CreateProgram(skyVertID, skyFragID);
        if(programID==0){
            return 0;
        }
        glUseProgram(programID);

        mLocVP = glGetUniformLocation(programID, mUniformVP);
        mLocCube = glGetUniformLocation(programID, mUniformCube);
        mLocPos = glGetAttribLocation(programID, mAttribPos);

        if(mLocCube<0||mLocVP<0)
        {
            Log.e("CreateProgram", "Error querying attribute locations");
            if(mLocCube<0)
                Log.e("CreateProgram", "Can't get location for "+mUniformCube);
            if(mLocVP<0)
                Log.e("CreateProgram", "Can't get location for "+mUniformVP);
            return 0;
        }

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


    public void SetMatrices(float pAspect, float pFOV, float pNear, float pFar, float[] pViewRot)
    {
        float[] cView = new float[16];
        float[] cProj = new float[16];

        //sky matrix
        setIdentityM(cView, 0);
        rotateM(cView, 0, pViewRot[0], 1f, 0f, 0f);
        rotateM(cView, 0, pViewRot[1], 0f, 1f, 0f);
        rotateM(cView, 0, pViewRot[2], 0f, 0f, 1f);

        perspectiveM(cProj, 0, pFOV, pAspect, pNear, pFar);
        multiplyMM(mMatVP, 0, cProj, 0, cView, 0);
    }

    public void DrawSkybox(int pVaoID, int pProgID, int pCubemapID)
    {
        glDisable(GL_DEPTH_TEST);

        glUseProgram(pProgID);
        glUniformMatrix4fv(mLocVP, 1, false, mMatVP, 0);
        glActiveTexture(GL_TEXTURE0);
        glUniform1i(mLocCube, 0);

        glBindVertexArray(pVaoID);
        glBindTexture(GL_TEXTURE_CUBE_MAP, pCubemapID);
        glDrawArrays(GL_TRIANGLES,0,36);
        glBindVertexArray(0);

        glEnable(GL_DEPTH_TEST);
    }
}
