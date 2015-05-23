package org.xdd.cosmicdancer;

        import static android.opengl.GLES30.*;
        import static android.opengl.Matrix.*;

        import android.content.Context;

        import java.nio.ByteBuffer;
        import java.nio.ByteOrder;
        import java.nio.FloatBuffer;
        import java.nio.IntBuffer;

public class PointCloudManager
{
    public class CloudData
    {
        public int  CloudVAO;
        public int  MeshVBO;
        public int  InstanceVBO;
        public int  IndexVBO;
        public int  GlslProgram;
        public int  ElementCount;
        public int  InstanceCount;
        public int  IndexType;

        public CloudData()
        {
            CloudVAO = -1;
            MeshVBO = -1;
            InstanceVBO = -1;
            IndexVBO = -1;
            GlslProgram = -1;
            ElementCount = -1;
            IndexType = -1;
        }

        public CloudData(int pVAO, int pMeshID, int pNumElem, int pInstID, int pNumInst, int pIndexID, int pProgID, int pIndexType)
        {
            CloudVAO = pVAO;
            MeshVBO = pMeshID;
            InstanceVBO = pInstID;
            IndexVBO = pIndexID;
            GlslProgram = pProgID;
            ElementCount = pNumElem;
            InstanceCount = pNumInst;
            IndexType = pIndexType;
        }
    };

    private Context         mContext;
    private ShaderManager   mShaderMgr;

    private final String    mUniformModel = "uModelMatrix";
    private final String    mUniformView = "uViewMatrix";
    private final String    mUniformProj = "uProjMatrix";
    private final String    mUniformNorm = "uNormalMatrix";
    private final String    mUniformLight = "uLightPos";
    private final String    mUniformEye = "uEyePos";
    private final String    mUniformCubemapIn = "uCubemapInSampler";
    private int             mLocModel;
    private int             mLocView;
    private int             mLocProj;
    private int             mLocNormal;
    private int             mLocLight;
    private int             mLocEye;
    private int             mLocCube;

    private final String    mAttribPos = "vPosition";
    private final String    mAttribNorm = "vNormal";
    private final String    mAttribIPos = "iPosition";
    private int             mLocPos;
    private int             mLocNorm;
    private int             mLocIPos;

    private static int      S_SIZE_POS = 3;
    private static int      S_SIZE_NORM = 3;
    private static int      S_SIZE_FLOAT = 4;
    private static int      S_SIZE_INT = 4;
    private static int      S_VERTEX_STRIDE;
    private static int      S_INSTANCE_STRIDE;

    private float[]         mModelMatrix = new float[16];
    private float[]         mViewMatrix = new float[16];
    private float[]         mProjMatrix = new float[16];
    private float[]         mNormMatrix = new float[16];

    private int             mInstanceSize;
    private int             mVertexSize;



    public PointCloudManager(Context pContext, ShaderManager pShaderMgr) {
        mContext = pContext;
        mShaderMgr = pShaderMgr;

        mInstanceSize = S_SIZE_POS;
        S_INSTANCE_STRIDE = mInstanceSize * S_SIZE_FLOAT;

        mVertexSize = S_SIZE_POS + S_SIZE_NORM;
        S_VERTEX_STRIDE = mVertexSize * S_SIZE_FLOAT;
    }


    public int CreateProgram(String pVertexShader, String pFragmentShader)
    {
        int vertID = mShaderMgr.CreateShader(pVertexShader, GL_VERTEX_SHADER);
        int fragID = mShaderMgr.CreateShader(pFragmentShader, GL_FRAGMENT_SHADER);
        int programID = mShaderMgr.CreateProgram(vertID, fragID);

        glUseProgram(programID);

        //get uniforms
        mLocModel = glGetUniformLocation(programID, mUniformModel);
        mLocView = glGetUniformLocation(programID, mUniformView);
        mLocProj = glGetUniformLocation(programID, mUniformProj);
        mLocNormal = glGetUniformLocation(programID, mUniformNorm);
        mLocLight = glGetUniformLocation(programID, mUniformLight);
        mLocEye = glGetUniformLocation(programID, mUniformEye);
        mLocCube = glGetUniformLocation(programID, mUniformCubemapIn);

        //get attributes
        mLocPos = glGetAttribLocation(programID, mAttribPos);
        mLocNorm  = glGetAttribLocation(programID, mAttribNorm);
        mLocIPos = glGetAttribLocation(programID, mAttribIPos);

        return programID;
    }

    public int CreateArrayBuffer(float[] pData, int pUsage)
    {
        FloatBuffer dataBuffer = ByteBuffer.allocateDirect(pData.length*S_SIZE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        dataBuffer.put(pData);
        dataBuffer.position(0);

        int[] vboID = new int[1];
        glGenBuffers(1,vboID,0);
        glBindBuffer(GL_ARRAY_BUFFER, vboID[0]);
        glBufferData(GL_ARRAY_BUFFER, pData.length * S_SIZE_FLOAT, dataBuffer, pUsage);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        return vboID[0];
    }

    public int CreateElementIntBuffer(int[] pData, int pUsage)
    {
        IntBuffer dataBuffer = ByteBuffer.allocateDirect(pData.length*S_SIZE_INT)
                                            .order(ByteOrder.nativeOrder())
                                            .asIntBuffer();
        dataBuffer.put(pData);
        dataBuffer.position(0);

        int[] vboID = new int[1];
        glGenBuffers(1,vboID,0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, pData.length*S_SIZE_INT, dataBuffer, pUsage);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);

        return vboID[0];
    }

    public CloudData CreatePointCloud(int pMeshVBO, int pNumElems, int pInstanceVBO, int pNumInst, int pIndexVBO, int pProg, int pIndexType)
    {
        int elementCount = pNumElems;
        if(pIndexVBO>=0)
            elementCount*=3;
        int[] cVaoID = new int[1];

        glGenVertexArrays(1, cVaoID, 0);
        glBindVertexArray(cVaoID[0]);

        //setup mesh data
        //attribs: pos, norm
        glBindBuffer(GL_ARRAY_BUFFER, pMeshVBO);
        glEnableVertexAttribArray(mLocPos);
        glVertexAttribPointer(mLocPos, 3, GL_FLOAT, false, S_VERTEX_STRIDE, 0);
        glEnableVertexAttribArray(mLocNorm);
        glVertexAttribPointer(mLocNorm,3,GL_FLOAT,false,S_VERTEX_STRIDE,S_SIZE_POS*S_SIZE_FLOAT);

        //setup instance data
        glBindBuffer(GL_ARRAY_BUFFER, pInstanceVBO);
        glEnableVertexAttribArray(mLocIPos);
        glVertexAttribPointer(mLocIPos, 3, GL_FLOAT, false, S_INSTANCE_STRIDE, 0);
        glVertexAttribDivisor(mLocIPos, 1);

        //cleanup
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new CloudData(cVaoID[0],pMeshVBO, elementCount, pInstanceVBO, pNumInst, pIndexVBO,pProg,pIndexType);
    }

    public void SetMatrix(SceneManager.Camera pCamera, float pAspect)
    {
        setIdentityM(mModelMatrix, 0);
        translateM(mModelMatrix, 0, 0, 0, 0);

        setIdentityM(mViewMatrix, 0);
        setLookAtM(mViewMatrix, 0, pCamera.Position[0], pCamera.Position[1], pCamera.Position[2],
                pCamera.LookAt[0], pCamera.LookAt[1], pCamera.LookAt[2],
                pCamera.Up[0], pCamera.Up[1], pCamera.Up[2]);

        setIdentityM(mProjMatrix, 0);
        perspectiveM(mProjMatrix, 0, pCamera.FOV, pAspect, pCamera.NearClip, pCamera.FarClip);

        setIdentityM(mNormMatrix, 0);
        multiplyMM(mNormMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
        invertM(mNormMatrix, 0, mNormMatrix, 0);
        transposeM(mNormMatrix,0,mNormMatrix,0);
    }

    public void UpdateCloud(CloudData pCloudData, FloatBuffer pDepthBuffer, int pNumPoints)
    {
        pCloudData.InstanceCount = pNumPoints;
        glBindBuffer(GL_ARRAY_BUFFER, pCloudData.InstanceVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, pNumPoints * 3 * 4, pDepthBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void DrawCloud(CloudData pCloudData, float[] pLightPos, float[] pEyePos, int pSkyboxTexID)
    {
        glUseProgram(pCloudData.GlslProgram);
        glUniformMatrix4fv(mLocModel, 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(mLocView, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocProj, 1, false, mProjMatrix, 0);
        glUniformMatrix4fv(mLocNormal, 1, false, mNormMatrix, 0);
        glUniform3fv(mLocLight, 1, pLightPos, 0);
        glUniform3fv(mLocEye, 1, pEyePos, 0);

        glUniform1i(mLocCube, 0);
        glActiveTexture(0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, pSkyboxTexID);

        glBindVertexArray(pCloudData.CloudVAO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pCloudData.IndexVBO);
        glDrawElementsInstanced(GL_TRIANGLES, pCloudData.ElementCount, pCloudData.IndexType, 0, pCloudData.InstanceCount);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glUseProgram(0);
    }
}
