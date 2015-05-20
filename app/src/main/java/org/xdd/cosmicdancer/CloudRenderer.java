package org.xdd.cosmicdancer;

import static android.opengl.GLES30.*;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.transition.Scene;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.Math;
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
    private PointCloudManager.CloudData[]   mPointClouds = new PointCloudManager.CloudData[S_NUM_CUBEMAPS];
    private int                             mPointCloudID = 0;

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
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        glClearColor(0.25f, 0.25f, 0.25f, 1f);
        mCamera = new SceneManager.Camera(  0, 0, 40,
                                            0, 0, 0,
                                            0, 1, 0,
                                            0.1f,500.0f,
                                            45.0f);
        mLightPosition[0] = 0;mLightPosition[1] = 0;mLightPosition[2] = 40;
        mShaderMgr = new ShaderManager(mContext);
        mSkyboxMgr = new SkyboxManager(mContext, mShaderMgr);
        mCloudMgr = new PointCloudManager(mContext,mShaderMgr);

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
            mPointCloudID = (mPointCloudID+1)%S_NUM_CUBEMAPS;
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        mSkyboxMgr.SetMatrices(mCamera, mAspect, new float[]{0, 0, 0});
        mSkyboxMgr.DrawSkybox(mSkyboxVaoID, mSkyboxProgID, mSkyboxTexIDs[mSkyboxCurrentID]);

        float xAngle = sysTime*0.00000002f;
        float yAngle = sysTime*0.00000004f;
        float zAngle = sysTime*0.00000001f;

        mCloudMgr.SetMatrix(mCamera, mAspect, new float[]{0, 0, 0}, new float[]{xAngle, yAngle, zAngle});
        mCloudMgr.DrawCloud(mPointClouds[2], mLightPosition, mCamera.Position, mSkyboxTexIDs[mSkyboxCurrentID]);
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
        Random rgbGen = new Random();
        int numElements = 10;
        float bound = 10.0f;
        float step = (bound*2.0f)/(float)numElements;

        //setup instance data
        float[] instanceData = new float[numElements*numElements*numElements*7];
        int id = 0;
        for(int px=0;px<numElements;++px)
        {
            float xPos = -bound + ((float)px*step)+(step*0.5f);
            for(int py=0;py<numElements;++py)
            {
                float yPos = -bound + ((float)py*step)+(step*0.5f);
                for(int pz=0;pz<numElements;++pz)
                {
                    float zPos = -bound + ((float)pz*step)+(step*0.5f);
                    instanceData[id++] = xPos;
                    instanceData[id++] = yPos;
                    instanceData[id++] = zPos;
                    instanceData[id++] = 0.0f;
                    instanceData[id++] = rgbGen.nextFloat();
                    instanceData[id++] = rgbGen.nextFloat();
                    instanceData[id++] = Math.max(rgbGen.nextFloat(), 1.0f/(float)numElements);
                }
            }
        }
        int instanceVBO = mCloudMgr.CreateArrayBuffer(instanceData, GL_STATIC_DRAW);
        int instanceCount = instanceData.length/7;
        //setup materials
        int cubesProgID = mCloudMgr.CreateProgram("vertex_instance.glsl", "frag_refract.glsl");
        int hedronsProgID = mCloudMgr.CreateProgram("vertex_instance.glsl", "frag_reflect.glsl");
        int spheresProgID = mCloudMgr.CreateProgram("vertex_instance.glsl", "frag_carpaint.glsl");

        //create cube cloud
        float[] cubeMeshData = SceneManager.GetCubeVerts();
        int cubeMeshVBO = mCloudMgr.CreateArrayBuffer(cubeMeshData,GL_STATIC_DRAW);
        int cubeElementCount = cubeMeshData.length/6;
        mPointClouds[0] = mCloudMgr.CreatePointCloud(cubeMeshVBO,
                                                        cubeElementCount,
                                                        instanceVBO,
                                                        instanceCount,
                                                        -1,cubesProgID,-1);

        //create hedrons cloud
        float[] hedronMeshData = SceneManager.GetHedronVerts();
        int hedronMeshVBO = mCloudMgr.CreateArrayBuffer(hedronMeshData, GL_STATIC_DRAW);
        int hedronMeshElementCount = hedronMeshData.length/6;
        mPointClouds[1] = mCloudMgr.CreatePointCloud(hedronMeshVBO,
                                                        hedronMeshElementCount,
                                                        instanceVBO,
                                                        instanceCount,
                                                        -1,hedronsProgID,-1);

        //create spheres cloud
        int subdAxis = 8;
        int subdHeight = 8;
        float radius = 1.0f;
        float[] sphereMeshData = new float[subdAxis*subdHeight*6];
        int[] sphereIndexData = new int[(subdAxis*subdHeight+subdAxis)*6];
        SceneManager.GetSphereVerts(sphereMeshData,sphereIndexData,radius,subdAxis,subdHeight);
        int sphereMeshVBO = mCloudMgr.CreateArrayBuffer(sphereMeshData, GL_STATIC_DRAW);
        int sphereIndexVBO = mCloudMgr.CreateElementIntBuffer(sphereIndexData, GL_STATIC_DRAW);
        int sphereElementCount = sphereMeshData.length;

        mPointClouds[2] = mCloudMgr.CreatePointCloud(sphereMeshVBO,
                                                        sphereElementCount,
                                                        instanceVBO,
                                                        instanceCount,
                                                        sphereIndexVBO,
                                                        spheresProgID,
                                                        GL_UNSIGNED_INT);
    }
}
