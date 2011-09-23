package de.chaos_darmstadt.schlafwandler.EnCam;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import de.chaos_darmstadt.schlafwandler.EnCam.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

// ----------------------------------------------------------------------

public class EnCam extends Activity {
    private Preview mPreview;
    private SharedPreferences mPrefs;
    
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

    	mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
    	setContentView(R.layout.main);
    	mPreview = (Preview)findViewById(R.id.preview);
    	
    	Button TakePictureButton = (Button)findViewById(R.id.TakePictureButton);
    	TakePictureButton.getBackground().setAlpha(100);
        
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
			Intent pref = new Intent(EnCam.this, Preferences.class);
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
	
	public void onTakePictureButton(View v)
	{
		Camera mCamera = mPreview.getCamera();
		mCamera.takePicture(null, null, this.onPictureTakenJPEG);
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
    		dir = Environment.getExternalStoragePublicDirectory(mPrefs.getString("savePicturesPath", "Encrypted Pictures"));
    		
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

