package de.chaos_darmstadt.schlafwandler.SeCam;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

// ----------------------------------------------------------------------

public class SeCam extends Activity {
    private Preview mPreview;
    private Camera mCamera;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);
    }
    
    public void onResume()
    {
    	super.onResume();

    	mCamera = Camera.open();
    	mPreview.setCamera(mCamera);
    }
    
    public void onPause()
    {
    	super.onPause();
    	
    	mCamera.release();
    	mCamera = null;
    	mPreview.setCamera(null);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
    	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mCamera != null)
    	{
    		mCamera.takePicture(null, null, this.onPictureTakenJPEG);
    		return true;
    	}
    	else
    		return super.onKeyDown(keyCode, event);
	}
    
	PictureCallback onPictureTakenJPEG = new PictureCallback() {	
		public void onPictureTaken(byte[] data,Camera cam)
		{
			FileOutputStream f = null;
			try {
				 f = openFileOutput("test.jpg",0);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				f.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			cam.startPreview();
			return;
		}
	};    

}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Context context;
    
    Preview(Context context) {
        super(context);
        this.context = context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void setCamera(Camera mCamera)
    {
    	this.mCamera = mCamera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.

        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	if (mCamera != null)
        {
    		mCamera.stopPreview();        
    		mCamera.release();
    		mCamera = null;
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
    	parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        switch (display.getRotation())
        {
        case Surface.ROTATION_0:
        	mCamera.setDisplayOrientation(90);
        	break;
        case Surface.ROTATION_90:
        	break;
        case Surface.ROTATION_180:
        	break;
        case Surface.ROTATION_270:
        	mCamera.setDisplayOrientation(180);
        	break;
        }
        
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

}