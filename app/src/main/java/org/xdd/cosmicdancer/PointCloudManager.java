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
    private static int      S_SIZE_INT = 4;
    private static int      S_VERTEX_STRIDE;
    private static int      S_INSTANCE_STRIDE;

    private float[]         mModelMatrix = new float[16];
    private float[]         mViewMatrix = new float[16];
    private float[]         mProjMatrix = new float[16];
    private float[]         mNormMatrix = new float[16];

    private int             mNumInstances;
    private int             mNumElements;
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

    public int CreateElementByteBuffer(byte[] pData, int pUsage)
    {
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(pData.length)
                .put(pData);
        dataBuffer.position(0);

        int[] vboID = new int[1];
        glGenBuffers(1,vboID,0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, pData.length, dataBuffer, pUsage);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);

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
        glEnableVertexAttribArray(mLocIColor);
        glVertexAttribPointer(mLocIColor, 3, GL_FLOAT, false, S_INSTANCE_STRIDE, S_SIZE_POS * S_SIZE_FLOAT);
        glVertexAttribDivisor(mLocIColor, 1);
        glEnableVertexAttribArray(mLocISize);
        glVertexAttribPointer(mLocISize, 1, GL_FLOAT, false, S_INSTANCE_STRIDE, (S_SIZE_POS + S_SIZE_COLOR) * S_SIZE_FLOAT);
        glVertexAttribDivisor(mLocISize, 1);

        //cleanup
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new CloudData(cVaoID[0],pMeshVBO, elementCount, pInstanceVBO, pNumInst, pIndexVBO,pProg,pIndexType);
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
        multiplyMM(mNormMatrix,0,mViewMatrix,0,mModelMatrix,0);
        invertM(mNormMatrix, 0, mNormMatrix, 0);
        transposeM(mNormMatrix,0,mNormMatrix,0);
    }

    public void DrawCloud(CloudData pCloudData, float[] pLightPos)
    {
        glUseProgram(pCloudData.GlslProgram);
        glUniformMatrix4fv(mLocModelM, 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(mLocViewM, 1, false, mViewMatrix, 0);
        glUniformMatrix4fv(mLocProjM, 1, false, mProjMatrix, 0);
        glUniformMatrix4fv(mLocNormM, 1, false, mNormMatrix, 0);
        glUniform3fv(mLocLightPos,1,pLightPos,0);

        glBindVertexArray(pCloudData.CloudVAO);

        if(pCloudData.IndexVBO>=0)
        {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pCloudData.IndexVBO);
            glDrawElementsInstanced(GL_TRIANGLES,pCloudData.ElementCount,pCloudData.IndexType,0,pCloudData.InstanceCount);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        else
            glDrawArraysInstanced(GL_TRIANGLES,0,pCloudData.ElementCount,pCloudData.InstanceCount);
        glBindVertexArray(0);

    }
}
