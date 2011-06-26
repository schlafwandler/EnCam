package de.chaos_darmstadt.schlafwandler.EnCam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import de.chaos_darmstadt.schlafwandler.EnCam.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

// ----------------------------------------------------------------------

public class SeCam extends Activity {
    private Preview mPreview;
//    private Camera mCamera;
    
    private long mEncryptionKeyIds[] = null;
    private String mEncryptedData = null;
    
    private Random mPRNG = null;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
    	loadSelectedKeys();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);
        
        // new random number generator (not secure, for file names only)
        mPRNG = new Random();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	
    	if (mEncryptionKeyIds == null)
    	{
    		Toast noKeys = Toast.makeText(getApplicationContext(), getString(R.string.warning_noKeySelected), Toast.LENGTH_LONG);
    		noKeys.show();
    	}
    	
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	    	
    	saveSelectedKeys();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.selectKeys:
    		Intent select = new Intent(Apg.Intent.SELECT_PUBLIC_KEYS);
    		select.putExtra(Apg.EXTRA_SELECTION, mEncryptionKeyIds);
    		startActivityForResult(select, Apg.SELECT_PUBLIC_KEYS);
			return true;
		case R.id.preferences:
			Intent pref = new Intent(SeCam.this, Preferences.class);
			startActivity(pref);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}		
	}
    
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
		Camera mCamera = mPreview.getCamera();
		
    	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mCamera != null)
    	{
    		mCamera.takePicture(null, null, this.onPictureTakenJPEG);
    		return true;
    	}
    	else
    		return super.onKeyDown(keyCode, event);    	
	}
    
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if (resultCode != Activity.RESULT_OK || data == null)
        {
            return;
        }
    	
    	switch (requestCode)
    	{
    	case Apg.SELECT_PUBLIC_KEYS:
    		mEncryptionKeyIds = data.getLongArrayExtra(Apg.EXTRA_SELECTION);
    		break;
    	case Apg.ENCRYPT_MESSAGE:
    		mEncryptedData = data.getStringExtra(Apg.EXTRA_ENCRYPTED_MESSAGE);
    		saveToFile(mEncryptedData);
    		break;
    	case Apg.SELECT_SECRET_KEY:
    		break;
    	case Apg.DECRYPT_MESSAGE:
    		break;
    	}
    }
    
    boolean testApgAvailability()
    {
    	//TODO: implement me!
    	//PackageInfo pi = context.getPackageManager().getPackageInfo("org.thialfihar.android.apg", 0);
    	return false;
    }
    
    boolean loadSelectedKeys()
    {
    	SharedPreferences settings = getPreferences(MODE_PRIVATE);
    	
    	String serial = settings.getString("publicKeys", "");
    	
    	if (serial == "")
    		return false;
    	
    	String[] sKeys = serial.split("\\s");
    	mEncryptionKeyIds = new long[sKeys.length];
    	
    	for (int i=0;i<sKeys.length;i++)
    	{
    		mEncryptionKeyIds[i] = Long.parseLong(sKeys[i]);    		
    	}
    		
    	return true;
    }
    
    void saveSelectedKeys()
    {
    	StringBuffer serial = new StringBuffer();
    	
    	if (mEncryptionKeyIds == null)
    		return;
    	
    	for (Long key : mEncryptionKeyIds)
    	{
    		serial.append(key);
    	}
    	
    	SharedPreferences settings = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editor = settings.edit();

    	editor.putString("publicKeys", serial.toString());
    	editor.commit();
    }
    
    void saveToFile(String data) 
    {
    	String state = Environment.getExternalStorageState();
    	File file = null, dir = null;
    	
    	
    	
    	if (Environment.MEDIA_MOUNTED.equals(state))
    	{
    		dir = Environment.getExternalStoragePublicDirectory(getString(R.string.saveDirNameDefault));
    		
    		if (!dir.exists())
    		{
    			if (!dir.mkdir())
    			{
    	    		Toast.makeText(getApplicationContext(), getString(R.string.error_fileSaveError), Toast.LENGTH_LONG).show();
    	    		return;    				
    			}
    			dir.setLastModified(461523600000L);
    		}
    		
    		// try till one random name is free
    		do {
    			
    			//file = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.saveDirNameDefault)), getSaveFileName());
    			file = new File(dir, getSaveFileName());
    		}
    		while (file.exists());
    	}
    	else
    	{
    		Toast.makeText(getApplicationContext(), getString(R.string.warning_noExternalStorage), Toast.LENGTH_LONG).show();
    	}
    	
    	if (file == null)
    	{
    		Toast.makeText(getApplicationContext(), getString(R.string.error_fileSaveError), Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	OutputStream os;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
    		Toast.makeText(getApplicationContext(), getString(R.string.error_fileSaveError), Toast.LENGTH_LONG).show();
    		return;
		}	
				
		try {
			os.write(data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		file.setLastModified(461523600000L);
    }
    
    // generates a pseudorandom file name (.ejpg for encrypted jpeg)
    String getSaveFileName()
    {
    	return Integer.toHexString(mPRNG.nextInt()) + ".ejpg";
    }
        
	PictureCallback onPictureTakenJPEG = new PictureCallback() {	
		public void onPictureTaken(byte[] data,Camera cam)
		{
			Intent ear = new Intent(Apg.Intent.ENCRYPT_AND_RETURN);
			
			ear.setType("application/octet-stream");
			ear.putExtra(Apg.EXTRA_DATA, data);
			ear.putExtra(Apg.EXTRA_ENCRYPTION_KEY_IDS, mEncryptionKeyIds);
			
			startActivityForResult(ear, Apg.ENCRYPT_MESSAGE);
			
			cam.startPreview();
			return;
		}
	};    
}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Context context;
    public Camera mCamera;

    
    Preview(Context context) {
        super(context);
        this.context = context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
        
    public Camera getCamera()
    {
    	return this.mCamera;
    }
    
    
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
    	mCamera = Camera.open();
    	
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
//    		mCamera.setPreviewCallback(null);
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