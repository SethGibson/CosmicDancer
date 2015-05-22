package org.xdd.cosmicdancer;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class CloudSurfaceView extends GLSurfaceView
{
    private CloudRenderer           mRenderer;

    private Context                 mContext;

    public CloudSurfaceView(Context context)
    {
        super(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initView()
    {
        setEGLContextClientVersion(3);

        mRenderer = new CloudRenderer(mContext);
        setRenderer(mRenderer);
    }

}
