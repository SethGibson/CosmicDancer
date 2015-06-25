package org.xdd.cosmicdancer;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image.Plane;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.intel.camera2.extensions.depthcamera.*;
import android.opengl.GLSurfaceView;

public class CloudSurfaceView extends GLSurfaceView
{
    private CloudRenderer           mRenderer;
    private Context                 mContext;
    private static final String TAG = "CosmicDancer";

    private CameraDevice mCamera;
    private CameraCharacteristics mCameraChar;
    private final Handler mHandler = new Handler();
    private static final int MAX_NUM_FRAMES = 5;
    private int mColorWidth;
    private int mColorHeight;
    private int mDepthWidth;
    private int mDepthHeight;
    private CameraCaptureSession mPreviewSession = null;
    private ArrayList<Surface> mTargets;
    private boolean mRepeating = false;
    private float[] mDepthData;
    private DepthCameraImageReader depthReader;
    private ImageReader colorReader;
    private DepthCameraCalibrationDataMap.IntrinsicParams mDepthIntrinsics;
    public CloudSurfaceView(Context context)
    {
        super(context);
        mContext = context;
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause");
        closeCamera();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        openCamera();
    }

    public void initView()
    {
        setEGLContextClientVersion(3);
        mRenderer = new CloudRenderer(mContext);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public static String streamIdToText(int streamId){
        switch(streamId) {
            case DepthCameraStreamConfigurationMap.DEPTH_STREAM_SOURCE_ID:
                return "DEPTH_STREAM_SOURCE_ID";

            case DepthCameraStreamConfigurationMap.LEFT_STREAM_SOURCE_ID:
                return "LEFT_STREAM_SOURCE_ID";

            case DepthCameraStreamConfigurationMap.RIGHT_STREAM_SOURCE_ID:
                return "RIGHT_STREAM_SOURCE_ID";
        }

        return "<unknown streamId>: " + Integer.toHexString(streamId);
    }

    public static String formatToText(int format) {
        switch (format) {
            case ImageFormat.YUY2:
                return "YUY2";

            case ImageFormat.JPEG:
                return "JPEG";

            case ImageFormat.NV21:
                return "NV21";

            case ImageFormat.YV12:
                return "YV12";

            case PixelFormat.A_8:
                return "A_8";

            case PixelFormat.RGB_332:
                return "RGB_332";

            case PixelFormat.LA_88:
                return "LA_88";

            case PixelFormat.RGBA_4444:
                return "RGBA_4444";

            case PixelFormat.RGBA_5551:
                return "RGBA_5551";

            case PixelFormat.RGBA_8888:
                return "RGBA_8888";

            case DepthImageFormat.Z16:
                return "DepthImageFormat.Z16";

            case DepthImageFormat.UVMAP:
                return "DepthImageFormat.UVMAP";
        }

        return "<unknown format>: " + Integer.toHexString(format);
    }

    private class DepthImageAvailableListener implements DepthCameraImageReader.OnDepthCameraImageAvailableListener
    {
        @Override
        public void onDepthCameraImageAvailable(DepthCameraImageReader reader)
        {
            DepthImage image = (DepthImage)reader.acquireNextImage();
            int id=0;
            int pointCounter = 0;

            if (image != null)
            {
                mRenderer.SetStreaming(true);
                int maxX = mDepthWidth/2;
                int maxY = mDepthHeight/2;

                for(int dy=0;dy<maxY;dy++)
                {
                    for(int dx=0;dx<maxX;dx++)
                    {
                        int xCoord = dx*2;
                        int yCoord = dy*2;
                        float dz = image.getZ(xCoord, yCoord);
                        if(dz>100.0f && dz<1500.0f) {
                            mDepthData[id++] = (float)xCoord-240.0f;
                            mDepthData[id++] = (float)yCoord-180.0f;
                            mDepthData[id++] = dz;
                            pointCounter++;
                        }
                    }
                }
                mRenderer.SetBuffer(mDepthData,pointCounter);
                requestRender();
            }
            else
                mRenderer.SetStreaming(false);
            image.close();
        }
    }


    private class ColorImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader)
        {
            Image image = reader.acquireNextImage();
            image.close();
        }
    }


    public class SimpleCameraCaptureSession extends CameraCaptureSession.StateCallback {
        public SimpleCameraCaptureSession() {
        }

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "(cameraCaptureSession.StateCallback) onConfigured");
            mPreviewSession = cameraCaptureSession;
            createCameraPreviewRequest();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "(cameraCaptureSession.StateCallback) onConfigureFailed");
        }
    }


    public class SimpleDeviceListener extends CameraDevice.StateCallback {

        public SimpleDeviceListener() {
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d( TAG,"(CameraDevice.StateCallback) onDisconnected" );
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d( TAG,"(CameraDevice.StateCallback) onError" );
        }

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d( TAG,"(CameraDevice.StateCallback) onOpened" );
            mCamera = camera;
            createCameraSession();
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d( TAG,"(CameraDevice.StateCallback) onClosed" );
            mCamera = null;
        }
    }

    private void createCameraPreviewRequest()
    {
        Log.d( TAG, "createCameraPreviewRequest" );

        try {
            CaptureRequest.Builder reqBldr = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            for (Surface s : mTargets)
                reqBldr.addTarget(s);

            mPreviewSession.setRepeatingRequest(reqBldr.build(), null, mHandler);
            mRepeating = true;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createCameraSession()
    {
        Log.d( TAG, "createCameraSession" );

        try {
            mDepthWidth = 480;
            mDepthHeight = 360;

            depthReader = DepthCameraImageReader.newInstance(mDepthWidth, mDepthHeight, DepthImageFormat.Z16, MAX_NUM_FRAMES);
            depthReader.setOnImageAvailableListener(new DepthImageAvailableListener(), null);
            mDepthData = new float[mDepthWidth*mDepthHeight*3];
            mColorWidth = 640;
            mColorHeight = 480;

            colorReader = ImageReader.newInstance(mColorWidth, mColorHeight, PixelFormat.RGBA_8888, MAX_NUM_FRAMES);
            colorReader.setOnImageAvailableListener(new ColorImageAvailableListener(), null);

            mTargets = new ArrayList<Surface>();
            mTargets.add(depthReader.getSurface());
            mTargets.add(colorReader.getSurface());

            mCamera.createCaptureSession(mTargets, new SimpleCameraCaptureSession(), null );
        }
        catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void openCamera()
    {
        Log.d(TAG, "openCamera");

        CameraManager camManager = (CameraManager)mContext.getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        int cameraIdx = -1;
        try {
            String[] cameraIds = camManager.getCameraIdList();
            if (cameraIds.length == 0)
                throw new Exception(TAG + ": camera ids list= 0");

            Log.w( TAG, "Number of cameras: " + cameraIds.length );

            for (int i = 0; i < cameraIds.length; i++) {
                Log.w( TAG, "Evaluating camera " + cameraIds[i] );

                mCameraChar = camManager.getCameraCharacteristics(cameraIds[i]);
                try {
                    if (DepthCameraCharacteristics.isDepthCamera(mCameraChar))
                    {
                        cameraId = cameraIds[i];
                        cameraIdx = i;
                        break;
                    }
                }
                catch (Exception e) {
                    Log.w(TAG,"Camera " +cameraId + ": failed on isDepthCamera");
                }
            }

            if (cameraIds.length > 0 && cameraId == null && mCameraChar != null)
                throw new Exception(TAG + "No Depth Camera Found");

            if (cameraId != null) {
                // Color
                Log.d( TAG, "Camera " + cameraId + " color characteristics");
                StreamConfigurationMap configMap = mCameraChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int[] colorFormats = configMap.getOutputFormats();
                for (int format : colorFormats)
                {
                    Log.d( TAG,"Camera " + cameraId + ": Supports color format " + formatToText(format));
                    Size[] mColorSizes = configMap.getOutputSizes(format);
                    for( Size s : mColorSizes )
                        Log.d( TAG,"Camera " + cameraId + ":     color size " + s.getWidth() + ", " + s.getHeight() );
                }

                // Depth
                int streamIds[] = {
                        DepthCameraStreamConfigurationMap.DEPTH_STREAM_SOURCE_ID,
                        DepthCameraStreamConfigurationMap.LEFT_STREAM_SOURCE_ID,
                        DepthCameraStreamConfigurationMap.RIGHT_STREAM_SOURCE_ID
                };
                for( int streamId : streamIds )
                {
                    Log.d( TAG, "Camera " + cameraId + " DepthCameraStreamConfigurationMap for " + streamIdToText(streamId));
                    DepthCameraStreamConfigurationMap depthConfigMap = new DepthCameraStreamConfigurationMap(mCameraChar);

                    int[] depthFormats = depthConfigMap.getOutputFormats(streamId);
                    for (int format : depthFormats)
                    {
                        Log.d( TAG,"Camera " + cameraId + ": Supports depth format " + formatToText(format));

                        Size[] sizes  = depthConfigMap.getOutputSizes(streamId, format);
                        for( Size s : sizes )
                            Log.d( TAG,"Camera " + cameraId + ":     color size " + s.getWidth() + ", " + s.getHeight() );
                    }
                }
            }
            camManager.openCamera(cameraId, new SimpleDeviceListener(), mHandler);
            /*
            DepthCameraCalibrationDataMap dataMap = new DepthCameraCalibrationDataMap(mCameraChar);
            DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibData =
                    dataMap.getCalibrationData(new Size(mColorWidth, mColorHeight),
                            new Size(mDepthWidth, mDepthHeight),
                            false,cameraIdx);

            mDepthIntrinsics = calibData.getDepthCameraIntrinsics();*/

        }
        catch (CameraAccessException e) {
            Log.e( TAG, "CameraAccessException:" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("crash crash");
        }
        catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("crash crash");
        }
    }


    private void closeCamera(){
        Log.d( TAG,"closeCamera" );

        try {
            if (mCamera != null) {
                if (mRepeating) {
                    mPreviewSession.stopRepeating();
                    mRepeating = false;
                }

                mCamera.close();
            }
        }
        catch (Exception e) {
            Log.e( TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
