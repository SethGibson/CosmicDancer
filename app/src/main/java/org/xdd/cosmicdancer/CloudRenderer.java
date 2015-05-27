package org.xdd.cosmicdancer;

import static android.opengl.GLES30.*;

import android.content.Context;
import android.graphics.Point;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.view.Display;
import android.view.WindowManager;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CloudRenderer implements GLSurfaceView.Renderer
{
    private Context             mContext;
    public ShaderManager        mShaderMgr;

    //Skybox
    private int                 S_NUM_CUBEMAPS = 3;
    private int                 S_TRANS_TIME_MILLIS = 5000;
    public SkyboxManager        mSkyboxMgr;
    private int                 mSkyboxVaoID;
    private int                 mSkyboxProgID;
    private int[]               mSkyboxTexIDs = new int[S_NUM_CUBEMAPS];
    private int                 mSkyboxCurrentID = 0;

    //Pointcloud
    public PointCloudManager                mCloudMgr;
    private PointCloudManager.CloudData     mPointCloud;
    private float[]                         mDataBuffer;
    int                                     mNumPoints;
    private boolean                         mIsStreaming;

    //Misc world stuff
    private float               mAspect;
    private SceneManager.Camera mCamera;
    private float[]             mLightPosition = new float[3];
    private long                mStartTime;

    public CloudRenderer(Context pContext)
    {
        mContext = pContext;

        WindowManager cwm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Display cDisp = cwm.getDefaultDisplay();
        Point cSize = new Point();
        cDisp.getSize(cSize);
        mAspect = (float)cSize.x/(float)cSize.y;
        mStartTime = System.currentTimeMillis();
        mNumPoints = 0;
        mIsStreaming = false;
    }
    /*
    Pass buffer to cloud manager
     */

    public void SetStreaming(boolean pStreaming)
    {
        mIsStreaming = pStreaming;
    }

    public void SetCloudBuffer(float[] pBuffer)
    {
        mDataBuffer = pBuffer;
    }

    public void SetNumPoints(int pNumPoints)
    {
        mPointCloud.InstanceCount = pNumPoints;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        glClearColor(0.25f, 0.25f, 0.25f, 1f);
        mCamera = new SceneManager.Camera(  0, 0, 0,
                                            0, 0, 1,
                                            0, -1, 0,
                                            100.0f,4000.0f,
                                            45.0f);
        mLightPosition[0] = 0;mLightPosition[1] = 500.0f;mLightPosition[2] = 0;
        mShaderMgr = new ShaderManager(mContext);
        mSkyboxMgr = new SkyboxManager(mContext, mShaderMgr);
        mCloudMgr = new PointCloudManager(mContext,mShaderMgr);
        mCloudMgr.GetCloudBuffer(mDataBuffer);
        SetupSkyboxes();
        SetupPointClouds();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        glViewport(0, 0, width, height);
        mAspect = (float)width/(float)height;
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        long sysTime = System.nanoTime();
        long elapsedTime = System.currentTimeMillis() - mStartTime;
        if(elapsedTime>S_TRANS_TIME_MILLIS)
        {
            mStartTime = System.currentTimeMillis();
            mSkyboxCurrentID = (mSkyboxCurrentID+1)%S_NUM_CUBEMAPS;
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        mSkyboxMgr.SetMatrices(mCamera, mAspect);
        mSkyboxMgr.DrawSkybox(mSkyboxVaoID, mSkyboxProgID, mSkyboxTexIDs[mSkyboxCurrentID]);

        mCloudMgr.SetMatrix(mCamera, mAspect);
        mCloudMgr.DrawCloud(mPointCloud, mLightPosition, mCamera.Position, mSkyboxTexIDs[mSkyboxCurrentID]);
    }

    public void SetupSkyboxes()
    {
        //load textures
        //morning
        int[] skyboxIds = new int[]
                {
                        R.drawable.px01,R.drawable.nx01,
                        R.drawable.py01,R.drawable.ny01,
                        R.drawable.pz01,R.drawable.nz01
                };
        mSkyboxTexIDs[0] = mSkyboxMgr.CreateCubemap(skyboxIds);

        //midday

        skyboxIds = new int[]
                {
                        R.drawable.px03,R.drawable.nx03,
                        R.drawable.py03,R.drawable.ny03,
                        R.drawable.pz03,R.drawable.nz03
                };
        mSkyboxTexIDs[1] = mSkyboxMgr.CreateCubemap(skyboxIds);

        //evening
        skyboxIds = new int[]
                {
                        R.drawable.px04,R.drawable.nx04,
                        R.drawable.py04,R.drawable.ny04,
                        R.drawable.pz04,R.drawable.nz04
                };
        mSkyboxTexIDs[2] = mSkyboxMgr.CreateCubemap(skyboxIds);

        //setup program
        mSkyboxProgID = mSkyboxMgr.CreateProgram("vertex_skybox.glsl", "frag_skybox.glsl");
        mSkyboxVaoID = mSkyboxMgr.CreateSkyboxMesh();
    }

    void SetupPointClouds()
    {
        int numElements = 1000;
        float minPos = 500.0f;
        float maxPos = 1500.0f;
        float step = 100.0f;
        //setup instance data
        float[] instanceData = new float[numElements*3];
        int id = 0;
        for(int px=0;px<10;++px)
        {
            float xPos = -500.0f+(px*step);
            for(int py=0;py<10;++py)
            {
                float yPos = -500.0f+(py*step);
                for(int pz=0;pz<10;++pz)
                {
                    float zPos = minPos+(pz*step);
                    instanceData[id++] = xPos;
                    instanceData[id++] = yPos;
                    instanceData[id++] = zPos;
                }
            }
        }
        int instanceVBO = mCloudMgr.CreateArrayBuffer(instanceData, GL_DYNAMIC_DRAW);
        int instanceCount = instanceData.length/3;
        //setup materials
        int spheresProgID = mCloudMgr.CreateProgram("vertex_instance.glsl", "frag_carpaint.glsl");

        //create spheres cloud
        int subdAxis = 8;
        int subdHeight = 8;
        float radius = 10.0f;
        float[] sphereMeshData = new float[subdAxis*subdHeight*6];
        int[] sphereIndexData = new int[(subdAxis*subdHeight+subdAxis)*6];
        SceneManager.GetSphereVerts(sphereMeshData,sphereIndexData,radius,subdAxis,subdHeight);
        int sphereMeshVBO = mCloudMgr.CreateArrayBuffer(sphereMeshData, GL_STATIC_DRAW);
        int sphereIndexVBO = mCloudMgr.CreateElementIntBuffer(sphereIndexData, GL_STATIC_DRAW);
        int sphereElementCount = sphereMeshData.length;

        mPointCloud = mCloudMgr.CreatePointCloud(sphereMeshVBO,
                                                        sphereElementCount,
                                                        instanceVBO,
                                                        instanceCount,
                                                        sphereIndexVBO,
                                                        spheresProgID,
                                                        GL_UNSIGNED_INT);
    }
}
