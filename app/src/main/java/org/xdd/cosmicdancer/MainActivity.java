package org.xdd.cosmicdancer;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends Activity
{

    private GLSurfaceView mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mMainView = new GLSurfaceView(this);
        mMainView.setEGLContextClientVersion(3);
        mMainView.setRenderer(new CloudRenderer(this));

        setContentView(mMainView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mMainView!=null) {
            mMainView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mMainView!=null) {
            mMainView.onResume();
        }
    }
}
