/*******************************************************************************

INTEL CORPORATION PROPRIETARY INFORMATION
This software is supplied under the terms of a license agreement or nondisclosure
agreement with Intel Corporation and may not be copied or disclosed except in
accordance with the terms of that agreement
Copyright(c) 2015 Intel Corporation. All Rights Reserved.

*******************************************************************************/
package org.xdd.cosmicdancer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.intel.camera2.extensions.depthcamera.DepthCameraCalibrationDataMap;
import com.intel.camera2.extensions.depthcamera.DepthCameraCharacteristics;
import com.intel.camera2.extensions.depthcamera.DepthCameraImageReader;
import com.intel.camera2.extensions.depthcamera.DepthCameraStreamConfigurationMap;
import com.intel.camera2.extensions.depthcamera.DepthCaptureRequest;
import com.intel.camera2.extensions.depthcamera.DepthImageFormat;

/**
 * Created by Daniel Piro <daniel.piro@intel.com> on 3/2/2015.
 * This Class wraps the Realsense R200 interface to provide easy to use
 * setup.
 */
public class RealsenseCamera {
    private static final String TAG = "RealsenseCamera";
    private static final double NANOS_IN_SECOND = TimeUnit.SECONDS.toNanos(1);
    private Context mContext;
    private CameraDevice mCamera;
    private CameraCharacteristics mCameraChar;
    private final Handler mHandler = new Handler();
    private static final int MAX_NUM_FRAMES = 5;
    private CameraCaptureSession mPreviewSession = null;
    private ArrayList<Surface> mTargets;
    private ArrayList<FrameAvailableListener> mSubscribers = new ArrayList<>();
    private byte[] mDepthBuffer; //Char is 16bit unsigned in Java
    private Bitmap mColorBitmap; //RGBA 4 channels bitmap
    private ByteBuffer mRGBByteBuffer;
    private boolean mRepeating = false;
    private Size mDepthSize;
    private Size mColorSize;
    private DepthCameraImageReader mDepthReader;
    private ImageReader mColorReader;
    private DepthCameraCalibrationDataMap.DepthCameraCalibrationData mCameraCalibrationData;

    private Set<CameraConfig> mDepthConfigurations = new HashSet<>();
    private Set<CameraConfig> mColorConfigurations = new HashSet<>();
    private CameraConfig mColorConfig = new CameraConfig(new Size(640,480),33333333, PixelFormat.RGBA_8888);
    private CameraConfig mDepthConfig = new CameraConfig(new Size(480,360),33333333,DepthImageFormat.Z16);

    public static String getTag() {
        return TAG;
    }

    public CameraConfig[] getDepthConfigurations() {
        CameraConfig[] arr= new CameraConfig[mDepthConfigurations.size()];
        mDepthConfigurations.toArray(arr);
        return arr;
    }

    public CameraConfig[] getColorConfigurations() {
        CameraConfig[] arr= new CameraConfig[mColorConfigurations.size()];
        mColorConfigurations.toArray(arr);
        return arr;
    }


    public Size getColorSize() {
        return mColorSize;
    }

    public Size getDepthSize() {
        return mDepthSize;
    }
    public void set_max_fps(long frame_duration){
        mDepthConfig.frame_duration= (long) frame_duration;
        mColorConfig.frame_duration= (long) frame_duration;
        stop();
        start();
    }
    public void set_depth_configratation(int item) {
        change_configuration(mColorConfig,getDepthConfigurations()[item]);
    }
    public void set_color_configratation(int item) {
        change_configuration(getColorConfigurations()[item],mDepthConfig);
    }

    public CameraConfig getColorConfig() {
        return mColorConfig;
    }

    public CameraConfig getDepthConfig() {
        return mDepthConfig;
    }

    //interface to listeners
    public interface FrameAvailableListener {
        public void onDepthAvailable(byte[] depth_buffer);

        public void onColorAvailable(Bitmap color_buffer);

        public void onCameraReady(RealsenseCamera camera);

        void onCameraClosed(RealsenseCamera camera);
    }

    public boolean subscribe(FrameAvailableListener listener) {
        return mSubscribers.add(listener);
    }

    public boolean unsubscribe(FrameAvailableListener listener) {
        return mSubscribers.remove(listener);
    }

    public RealsenseCamera(Context context) {
        this.mContext = context;
    }

    //Camera control functions
    public class SimpleCameraCaptureSession extends CameraCaptureSession.StateCallback {
        public SimpleCameraCaptureSession() {
        }

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "(cameraCaptureSession.StateCallback) onConfigured");
            mPreviewSession = cameraCaptureSession;
            createCameraPreviewRequest();
            for (FrameAvailableListener l : mSubscribers)
                l.onCameraReady(RealsenseCamera.this);
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
            Log.d(TAG, "(CameraDevice.StateCallback) onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "(CameraDevice.StateCallback) onError");
        }

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "(CameraDevice.StateCallback) onOpened");
            mCamera = camera;
            createCameraSession();
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "(CameraDevice.StateCallback) onClosed");
            for (FrameAvailableListener l : mSubscribers)
                l.onCameraClosed(RealsenseCamera.this);
            mCamera = null;
        }
    }


    private void createCameraPreviewRequest() {
        Log.d(TAG, "createCameraPreviewRequest");
        CaptureRequest request;
        try {
            CaptureRequest.Builder reqBldr = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            reqBldr.set(DepthCaptureRequest.R200_COLOR_RECTIFICATION_MODE, true);
            reqBldr.set(CaptureRequest.SENSOR_FRAME_DURATION, Math.max(mColorConfig.frame_duration, mDepthConfig.frame_duration));
            for (Surface s : mTargets)
                reqBldr.addTarget(s);
            request = reqBldr.build();
            mPreviewSession.setRepeatingRequest(request, null, mHandler);
            mRepeating = true;
            DepthCameraCalibrationDataMap cameraCalibrationMap = new DepthCameraCalibrationDataMap(mCameraChar);
            mCameraCalibrationData = cameraCalibrationMap.getCalibrationData(mColorSize, mDepthSize, true, 0);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createCameraSession() {
        Log.d(TAG, "createCameraSession");

        try {
            mDepthSize=mDepthConfig.size;
            int mDepthWidth = mDepthSize.getWidth();
            int mDepthHeight = mDepthSize.getHeight();
            mDepthReader = DepthCameraImageReader.newInstance(mDepthWidth, mDepthHeight, mDepthConfig.format, MAX_NUM_FRAMES);

            mDepthReader.setOnImageAvailableListener(new DepthImageAvailableListener(), null);
            mDepthBuffer = new byte[mDepthSize.getHeight() * mDepthSize.getWidth() * 2];

            mColorSize=mColorConfig.size;
            int mColorWidth = mColorSize.getWidth();
            int mColorHeight = mColorSize.getHeight();
            mColorReader = ImageReader.newInstance(mColorWidth, mColorHeight, ImageFormat.YUV_420_888, MAX_NUM_FRAMES);
            mColorReader.setOnImageAvailableListener(new ColorImageAvailableListener(), null);
            mColorBitmap = Bitmap.createBitmap(mColorWidth, mColorHeight, Bitmap.Config.ARGB_8888);
            mRGBByteBuffer = ByteBuffer.allocateDirect(mColorWidth * mColorHeight * 4);


            mTargets = new ArrayList<>();
            mTargets.add(mDepthReader.getSurface());
            mTargets.add(mColorReader.getSurface());

            mCamera.createCaptureSession(mTargets, new SimpleCameraCaptureSession(), null);
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void openCamera(CameraConfig color, CameraConfig depth) {
        Log.d(TAG, "openCamera");

        CameraManager camManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        String cameraId = null;

        try {
            String[] cameraIds = camManager.getCameraIdList();
            if (cameraIds.length == 0)
                throw new Exception(TAG + ": camera ids list= 0");

            Log.w(TAG, "Number of cameras: " + cameraIds.length);

            for (String id : cameraIds) {
                Log.w(TAG, "Evaluating camera " + id);

                mCameraChar = camManager.getCameraCharacteristics(id);
                mCameraChar.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
                try {
                    if (DepthCameraCharacteristics.isDepthCamera(mCameraChar)) {
                        cameraId = id;
                        break;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Camera " + id + ": failed on isDepthCamera");
                }
            }

            if (cameraIds.length > 0 && cameraId == null && mCameraChar != null)
                throw new Exception(TAG + "No Depth Camera Found");

            if (cameraId != null) {
                // Color
                Log.d(TAG, "DepthCamera ID: " + cameraId + " - Supported configurations:");
                StreamConfigurationMap configMap = mCameraChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mColorConfigurations.clear();
                int[] colorFormats = configMap.getOutputFormats();
                for (int format : colorFormats) {
                    if (format != ImageFormat.YUV_420_888)
                        continue;
                    Size[] mColorSizes = configMap.getOutputSizes(format);
                    for (Size s : mColorSizes) {
                        long frame_duration = configMap.getOutputMinFrameDuration(format, s); //get max color framerate
                        mColorConfigurations.add(new CameraConfig(s, frame_duration, format));
                    }
                }

                // Depth
                int streamIds[] = {
                        DepthCameraStreamConfigurationMap.DEPTH_STREAM_SOURCE_ID,
                };
                mDepthConfigurations.clear();
                for (int streamId : streamIds) {
                    DepthCameraStreamConfigurationMap depthConfigMap = new DepthCameraStreamConfigurationMap(mCameraChar);
                    int[] depthFormats = depthConfigMap.getOutputFormats(streamId);
                    for (int format : depthFormats) {
                        if (format != DepthImageFormat.Z16)
                            continue;
                        Size[] sizes = depthConfigMap.getOutputSizes(streamId, format);
                        for (Size s : sizes) {
                            long frame_duration = depthConfigMap.getOutputMinFrameDuration(streamId, format, s);
                            mDepthConfigurations.add(new CameraConfig(s, frame_duration, format));
                         }
                    }
                }
                Log.d(TAG, "Camera " + cameraId + ": Supported color formats: ");
                for(CameraConfig c: mColorConfigurations){
                    Log.d(TAG, c.toString());
                }
                Log.d(TAG, "Camera " + cameraId + ": Supported depth formats: ");
                for(CameraConfig c: mDepthConfigurations){
                    Log.d(TAG, c.toString());
                }
            }
            if (depth != null && mDepthConfigurations.contains(depth))
                mDepthConfig = depth;
            if (color != null && mColorConfigurations.contains(color))
                mColorConfig = color;
            camManager.openCamera(cameraId, new SimpleDeviceListener(), mHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void closeCamera() {
        Log.d(TAG, "closeCamera");

        try {
            if (mCamera != null) {
                if (mRepeating) {
                    mPreviewSession.stopRepeating();
                    mRepeating = false;
                }

                mCamera.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        Log.d(TAG, "Stop");
        closeCamera();
    }


    public void start() {
        Log.d(TAG, "Start");
        if (mContext == null)
            throw new NullPointerException("Context isn't set for Realsense camera");
        openCamera(mColorConfig,mDepthConfig);
    }
    public void change_configuration(CameraConfig color,CameraConfig depth){
        closeCamera();
        openCamera(color,depth);
    }


    //listners
    private class DepthImageAvailableListener implements DepthCameraImageReader.OnDepthCameraImageAvailableListener {
        @Override
        public void onDepthCameraImageAvailable(DepthCameraImageReader reader) {
            Image image = reader.acquireNextImage();
            if (image != null) {

                Image.Plane[] planes = image.getPlanes();
                if (BuildConfig.DEBUG && (!(planes != null && planes.length > 0)))
                    throw new AssertionError();
                planes[0].getBuffer().get(mDepthBuffer);
                for (FrameAvailableListener l : mSubscribers)
                    l.onDepthAvailable(mDepthBuffer);
                image.close();
            }
        }
    }

    private class ColorImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
    			assert(planes != null && planes.length > 0);

    	 		mRGBByteBuffer.rewind();
    	 		YUV420ToRGB( planes[0].getBuffer(), planes[1].getBuffer(), mRGBByteBuffer, image.getWidth(), image.getHeight(), planes[0].getRowStride(), planes[0].getPixelStride(), planes[1].getRowStride(), planes[1].getPixelStride() );
    			mColorBitmap.copyPixelsFromBuffer(mRGBByteBuffer);

    			for (FrameAvailableListener l : mSubscribers)
                    l.onColorAvailable(mColorBitmap);
                image.close();
            }
        }
    }

    public DepthCameraCalibrationDataMap.DepthCameraCalibrationData getCalibrationData() {
        return mCameraCalibrationData;
    }

    public class CameraConfig {
        public Size size;
        public long frame_duration;
        public int format;

        private CameraConfig(Size size, long frame_duration, int format) {
            this.size = size;
            this.frame_duration = frame_duration;
            this.format = format;
        }

        @Override
        public int hashCode() {
            return size.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return size.equals(((CameraConfig)o).size);
        }

        @Override
        public String toString() {
            double fps= (NANOS_IN_SECOND/(frame_duration-1));
            return size+ " (Max "+(int)fps+" FPS)";
        }
    }
    
    private native boolean depthToGrayscale( ByteBuffer depthBuf, ByteBuffer grayscaleBuf, int bufSize);
    private native boolean uvMapToRGB( ByteBuffer depthBufSrc, ByteBuffer uvMapSrc, ByteBuffer colorPixelsSrc, ByteBuffer dst, int depthWidth, int depthHeight, int colorWidth, int colorHeight);
    private native boolean YUV420ToRGB( ByteBuffer YBuf, ByteBuffer UVBuf, ByteBuffer RGBBuf, int width, int height, int y_pitch, int y_stride, int uv_pitch, int uv_stride );

     static {
     	System.loadLibrary("jni_Camera2BasicViewer");
     }
    
}
