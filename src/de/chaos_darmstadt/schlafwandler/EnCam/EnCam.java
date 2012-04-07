package de.chaos_darmstadt.schlafwandler.EnCam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

// ----------------------------------------------------------------------

public class EnCam extends Activity {
	private Preview mPreview;
	private SharedPreferences mPrefs;

	private long mEncryptionKeyIds[] = null;
	private String mEncryptedData = null;
	static final int DIALOG_ABOUT_ID = 0;
	static final int DIALOG_CHECKAPG_ID = 1;
	static final int DIALOG_NOKEYS_ID = 2;

	String APP_NAME;
	static final String APP_PACKAGE = "de.chaos_darmstadt.schlafwandler.EnCam";
	String APP_VERSION;

	private ResponseReceiver receiver;

	private Random mPRNG = null;
	private Random random;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		loadSelectedKeys();

		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		setContentView(R.layout.main);
		mPreview = (Preview) findViewById(R.id.preview);

		Button TakePictureButton = (Button) findViewById(R.id.TakePictureButton);
		TakePictureButton.getBackground().setAlpha(100);

		// new random number generator (not secure, for file names only)
		mPRNG = new Random();

		APP_VERSION = getString(R.string.app_version);
		APP_NAME = getString(R.string.app_name);

		if (!testApgAvailability())
			showDialog(DIALOG_CHECKAPG_ID);

		IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mEncryptionKeyIds == null || mEncryptionKeyIds.length == 0)
			showDialog(DIALOG_NOKEYS_ID);

		IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(receiver, filter);

	}

	@Override
	public void onPause() {
		super.onPause();
		saveSelectedKeys();
		unregisterReceiver(receiver);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.selectKeys:
			selectKeys();
			return true;
		case R.id.preferences:
			Intent pref = new Intent(EnCam.this, Preferences.class);
			startActivity(pref);
			return true;
		case R.id.help:
			Intent help = new Intent(EnCam.this, Help.class);
			startActivity(help);
			return true;
		case R.id.about:
			showDialog(DIALOG_ABOUT_ID);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void selectKeys() {
		Intent select = new Intent(Apg.Intent.SELECT_PUBLIC_KEYS);
		select.putExtra(Apg.EXTRA_SELECTION, mEncryptionKeyIds);
		startActivityForResult(select, Apg.SELECT_PUBLIC_KEYS);
	}

	protected Dialog aboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(
				getString(R.string.menu_about) + " " + APP_NAME + " "
						+ APP_VERSION)
				.setMessage(
						Html.fromHtml(getString(R.string.about_text)
								.replaceAll("lt;", "<")))
				.setCancelable(false)
				.setNeutralButton(getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dismissDialog(DIALOG_ABOUT_ID);
							}
						});
		AlertDialog aboutDialog = builder.create();
		return aboutDialog;
	}

	protected Dialog checkApgDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.warning_noApg))
				.setCancelable(false)
				.setNeutralButton(getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent openMarket = new Intent(
										Intent.ACTION_VIEW);
								openMarket.setData(Uri
										.parse("market://details?id=org.thialfihar.android.apg"));
								startActivity(openMarket);
								finish();
							}
						});
		AlertDialog checkApgDialog = builder.create();
		return checkApgDialog;
	}

	protected Dialog noKeysDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.warning_noKeySelected))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.bt_chooseKey),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dismissDialog(DIALOG_NOKEYS_ID);
								selectKeys();
							}
						})
				.setNegativeButton(getString(R.string.bt_close),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dismissDialog(DIALOG_NOKEYS_ID);
								finish();
							}
						});
		AlertDialog noKeysDialog = builder.create();
		return noKeysDialog;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_ABOUT_ID:
			dialog = aboutDialog();
			break;
		case DIALOG_CHECKAPG_ID:
			dialog = checkApgDialog();
			break;
		case DIALOG_NOKEYS_ID:
			dialog = noKeysDialog();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Camera mCamera = mPreview.getCamera();

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mCamera != null) {
			mCamera.takePicture(null, null, this.onPictureTakenJPEG);
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	public void onTakePictureButton(View v) {
		Camera mCamera = mPreview.getCamera();
		mCamera.takePicture(null, null, this.onPictureTakenJPEG);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}

		switch (requestCode) {
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

	boolean testApgAvailability() {
		try {
			getPackageManager().getPackageInfo("org.thialfihar.android.apg", 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	boolean loadSelectedKeys() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		String serial = settings.getString("publicKeys", "");

		if (serial == "")
			return false;

		String[] sKeys = serial.split("\\s");
		mEncryptionKeyIds = new long[sKeys.length];

		for (int i = 0; i < sKeys.length; i++) {
			mEncryptionKeyIds[i] = Long.parseLong(sKeys[i]);
		}

		return true;
	}

	void saveSelectedKeys() {
		StringBuffer serial = new StringBuffer();

		if (mEncryptionKeyIds == null) {
			return;
		}

		for (Long key : mEncryptionKeyIds) {
			serial.append(key);
		}

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString("publicKeys", serial.toString());
		editor.commit();
	}

	void saveToFile(String data) {
		String state = Environment.getExternalStorageState();
		File file = null, dir = null;

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			dir = Environment.getExternalStoragePublicDirectory(mPrefs
					.getString("pref_path", "Encrypted Pictures"));

			if (!dir.exists()) {
				if (!dir.mkdir()) {
					notify(R.string.error_fileSaveError);
					return;
				}
				dir.setLastModified(461523600000L);
				setDate(dir);
			}

			// try till one random name is free
			do {
				file = new File(dir, getSaveFileName());
			} while (file.exists());
		} else {
			notify(R.string.warning_noExternalStorage);
		}

		if (file == null) {
			notify(R.string.error_fileSaveError);
			return;
		}

		OutputStream os;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			notify(R.string.error_fileSaveError);
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

		// file.setLastModified(461523600000L);
		setDate(file);
		if (mPrefs.getString("enableMail",
				getString(R.string.config_enableMail)).equals("true"))
			upload(file.getAbsolutePath(), UploadService.KIND_MAIL);
		if (mPrefs.getString("enableFtp", getString(R.string.config_enableFtp))
				.equals("true"))
			upload(file.getAbsolutePath(), UploadService.KIND_FTP);
		if (mPrefs.getString("enableShare",
				getString(R.string.config_enableShare)).equals("true"))
			upload(file.getAbsolutePath(), UploadService.KIND_SHARE);

	}

	private void setDate(File file) {
		String selection = mPrefs.getString("date", "random");
		long time;
		if (selection.equals("static")) {
			time = 461523600000L;

		} else if (selection.equals("actual")) {
			time = System.currentTimeMillis();
		} else {
			random = new Random();
			time = (long) (random.nextDouble() * (500000000000L));
		}

		file.setLastModified(time);
	}

	public void upload(String path, int kind) {
		String[] data;

		switch (kind) {
		case UploadService.KIND_MAIL:
			data = new String[] {
					path,
					mPrefs.getString("mailTo",
							getString(R.string.config_mailTo)),
					mPrefs.getString("mailSubject",
							getString(R.string.config_mailSubject)),
					mPrefs.getString("mailBody",
							getString(R.string.config_mailBody)),
					mPrefs.getString("mailFrom",
							getString(R.string.config_mailFrom)),
					mPrefs.getString("mailHost",
							getString(R.string.config_mailHost)),
					mPrefs.getString("mailPort",
							getString(R.string.config_mailPort)),
					mPrefs.getString("mailUser",
							getString(R.string.config_mailUser)),
					mPrefs.getString("mailPassword",
							getString(R.string.config_mailPassword)),
					mPrefs.getString("mailSsl", "false") };
			break;
		case UploadService.KIND_FTP:
			data = new String[] {
					path,
					mPrefs.getString("ftpHost",
							getString(R.string.config_ftpHost)),
					mPrefs.getString("ftpPort",
							getString(R.string.config_ftpPort)),
					mPrefs.getString("ftpUser",
							getString(R.string.config_ftpUser)),
					mPrefs.getString("ftpPassword",
							getString(R.string.config_ftpPassword)),
					mPrefs.getString("ftpDirectory",
							getString(R.string.config_ftpDirectory)) };
			break;
		case UploadService.KIND_SHARE:
			data = new String[] { path };
			share(data);
			break;
		default:
			data = new String[] {};
		}

		if (kind != UploadService.KIND_NONE && kind != UploadService.KIND_SHARE) {
			Intent upload = new Intent(this, UploadService.class);
			upload.putExtra(UploadService.KIND, kind);
			upload.putExtra(UploadService.CONNECTION_DATA, data);
			startService(upload);
		}

	}

	private void share(String[] data) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("application/octet-stream");

		// For a file in shared storage. For data in private storage, use a
		// ContentProvider.
		Uri uri = new Uri.Builder().path(data[0]).build();
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

		startActivity(Intent.createChooser(shareIntent,
				getString(R.string.dialog_share)));
	}

	// generates a pseudorandom file name (.ejpg for encrypted jpeg)
	String getSaveFileName() {
		return Integer.toHexString(mPRNG.nextInt()) + ".ejpg";
	}

	private void notify(int string) {
		Toast.makeText(getApplicationContext(), getString(string),
				Toast.LENGTH_LONG).show();
	}

	PictureCallback onPictureTakenJPEG = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera cam) {
			Intent ear = new Intent(Apg.Intent.ENCRYPT_AND_RETURN);

			ear.setType("application/octet-stream");
			ear.putExtra(Apg.EXTRA_DATA, data);
			ear.putExtra(Apg.EXTRA_ENCRYPTION_KEY_IDS, mEncryptionKeyIds);

			startActivityForResult(ear, Apg.ENCRYPT_MESSAGE);

			cam.startPreview();
			return;
		}
	};

	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "de.chaos_darmstadt.schlafwandler.EnCam.intent.action.MESSAGE_PROCESSED";

		@Override
		public void onReceive(Context context, Intent intent) {
			EnCam.this.notify(intent.getIntExtra(UploadService.RESULT,
					R.string.error_mailSendError));
		}

	}

}

// ----------------------------------------------------------------------

