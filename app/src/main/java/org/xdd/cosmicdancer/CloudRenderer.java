package org.xdd.cosmicdancer;

import static android.opengl.GLES30.*;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
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
    public SkyboxManager        mSkyboxMgr;
    private int                 mSkyboxProgID;
    private int[]               mSkyboxTexIDs = new int[5];
    private int                 mSkyboxVaoID;

    private float               mYAngle = 0f;
    private float               mYRate = 0.2f;

    //Pointcloud
    public PointCloudManager    mCloudMgr;
    private int                 mCloudProgID;
    private int                 mCloudVaoID;
    private int                 mInstanceVboID;

    //Misc world stuff
    private float               mAspect;
    private SceneManager.Camera mCamera;
    private float[]             mLightPosition = new float[3];
    private long                mStartTime;

    private PointCloudManager.CloudData mPointCloud;

    public CloudRenderer(Context pContext)
    {
        mContext = pContext;

        WindowManager cwm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Display cDisp = cwm.getDefaultDisplay();
        Point cSize = new Point();
        cDisp.getSize(cSize);
        mAspect = (float)cSize.x/(float)cSize.y;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        glClearColor(0.25f, 0.25f, 0.25f, 1f);
        mCamera = new SceneManager.Camera(  0, 0, 25,
                                            0, 0, 0,
                                            0, 1, 0,
                                            0.1f,500.0f,
                                            45.0f);
        mLightPosition[0] = 0;mLightPosition[1] = 10;mLightPosition[2] = 10;
        mShaderMgr = new ShaderManager(mContext);
        mSkyboxMgr = new SkyboxManager(mContext, mShaderMgr);
        mCloudMgr = new PointCloudManager(mContext,mShaderMgr);

        SetupSkyboxes();

        Random rgbGen = new Random();
        float[] instanceData = new float[1000*7];
        int id = 0;
        for(int px=0;px<10;++px)
        {
            float xPos = -10.0f + ((float)px*2.0f)+1.0f;
            for(int py=0;py<10;++py)
            {
                float yPos = -10.0f + ((float)py*2.0f)+1.0f;
                for(int pz=0;pz<10;++pz)
                {
                    float zPos = -10.0f + ((float)pz*2.0f)+1.0f;
                    instanceData[id++] = xPos;
                    instanceData[id++] = yPos;
                    instanceData[id++] = zPos;
                    instanceData[id++] = 0.0f;
                    instanceData[id++] = rgbGen.nextFloat();
                    instanceData[id++] = rgbGen.nextFloat();
                    instanceData[id++] = Math.max(rgbGen.nextFloat(), 0.2f);
                }
            }
        }

        mCloudProgID = mCloudMgr.CreateProgram();

        int numCirclePoints = 16;
        float[] meshData = new float[(numCirclePoints+1)*6];
        byte[] indexData = new byte[numCirclePoints*3];
        SceneManager.GetCircleVerts(numCirclePoints, meshData, indexData);

        int meshVBO = mCloudMgr.CreateArrayBuffer(meshData, GL_STATIC_DRAW);
        int instanceVBO = mCloudMgr.CreateArrayBuffer(instanceData, GL_STATIC_DRAW);
        int indexVBO = mCloudMgr.CreateElementBuffer(indexData, GL_STATIC_DRAW);
        int elementCount = meshData.length/6;
        int instanceCount = instanceData.length/7;
        mPointCloud = mCloudMgr.CreatePointCloud(meshVBO, elementCount, instanceVBO, instanceCount, indexVBO, mCloudProgID);
        /*
        float[] meshData = SceneManager.GetCubeVerts();
        int meshVBO = mCloudMgr.CreateArrayBuffer(meshData, GL_STATIC_DRAW);
        int instanceVBO = mCloudMgr.CreateArrayBuffer(instanceData, GL_STATIC_DRAW);
        int elementCount = meshData.length/6;
        int instanceCount = instanceData.length/7;

        mPointCloud = mCloudMgr.CreatePointCloud(meshVBO, elementCount, instanceVBO, instanceCount, -1, mCloudProgID);
        */
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        mSkyboxMgr.SetMatrices(mAspect, 45.0f, 0.1f, 10.0f, new float[]{0, 0, 0});
        mSkyboxMgr.DrawSkybox(mSkyboxVaoID, mSkyboxProgID, mSkyboxTexIDs[0]);

        float xAngle = sysTime*0.00000002f;
        float yAngle = sysTime*0.00000004f;
        float zAngle = sysTime*0.00000001f;

        mCloudMgr.SetMatrix(mCamera, mAspect, new float[]{0, 0, 0}, new float[]{xAngle, yAngle, zAngle});
        mCloudMgr.DrawCloud(mPointCloud, mLightPosition);
    }

    public void SetupSkyboxes()
    {
        //load textures
        int[] skyboxIds = new int[]
                {
                        R.drawable.01_px,
                }

        //setup program
        mSkyboxProgID = mSkyboxMgr.CreateProgram();
        mSkyboxVaoID = mSkyboxMgr.CreateSkyboxMesh();
    }
}
