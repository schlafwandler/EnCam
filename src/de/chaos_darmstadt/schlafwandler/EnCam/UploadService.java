package de.chaos_darmstadt.schlafwandler.EnCam;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;

import org.apache.commons.net.ftp.FTPClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class UploadService extends IntentService {
	static final String PATH = "path";

	public UploadService() {
		super("upload");
	}

	private Bundle sendMail(String path) {

		try {
			SharedPreferences mPrefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			Mail mail = new Mail();

			mail.setContent(mPrefs.getString("mailTo",
					getString(R.string.config_mailTo)), mPrefs.getString(
					"mailSubject", getString(R.string.config_mailSubject)),
					mPrefs.getString("mailBody",
							getString(R.string.config_mailBody)));

			mail.setAccount(mPrefs.getString("mailFrom",
					getString(R.string.config_mailFrom)), mPrefs.getString(
					"mailHost", getString(R.string.config_mailHost)),
					mPrefs.getString("mailPort",
							getString(R.string.config_mailPort)), mPrefs
							.getString("mailUser",
									getString(R.string.config_mailUser)),
					mPrefs.getString("mailPassword",
							getString(R.string.config_mailPassword)), mPrefs
							.getString("mailSsl", "false"));

			mail.addAttachment(path);
			if (!mail.send()) {
				return makeResultBundle(R.string.error_mailSendError, false);
			}
		} catch (Exception e) {
			return makeResultBundle(R.string.error_mailSendError, false);
		}
		return makeResultBundle(R.string.success_mailSend, true);
	}

	private Bundle ftpUpload(String path) {
		SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		Bundle result = makeResultBundle(R.string.error_ftpUploadError, false);
		try {
			FTPClient ftpClient = new FTPClient();

			ftpClient.connect(mPrefs.getString("ftpHost",
					getString(R.string.config_ftpHost)), Integer
					.parseInt(mPrefs.getString("ftpPort",
							getString(R.string.config_ftpPort))));
			ftpClient.enterLocalPassiveMode();
			ftpClient.login(mPrefs.getString("ftpUser",
					getString(R.string.config_ftpUser)), mPrefs.getString(
					"ftpPassword", getString(R.string.config_ftpPassword)));
			ftpClient.changeWorkingDirectory(mPrefs.getString("ftpDirectory",
					getString(R.string.config_ftpDirectory)));
			ftpClient
					.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			BufferedInputStream buffIn = null;
			buffIn = new BufferedInputStream(new FileInputStream(path));
			ftpClient.enterLocalPassiveMode();
			if (ftpClient.storeFile(path.substring(
					path.lastIndexOf(Matcher.quoteReplacement("/")) + 1,
					path.length()), buffIn))
				result = makeResultBundle(R.string.success_ftpUpload, true);
			else
				result = makeResultBundle(R.string.error_ftpUploadError, true);
			buffIn.close();
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (Exception e) {
			result = makeResultBundle(R.string.error_ftpUploadError, true);

		}
		return result;
	}

	private Bundle makeResultBundle(int msg, boolean success) {
		Bundle result = new Bundle();
		result.putInt("msg", msg);
		result.putBoolean("success", success);
		return result;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Bundle bdl = null;
		String path = intent.getStringExtra(PATH);
		String service = mPrefs.getString("uploadService", "");

		if (service.equals("mail"))
			bdl = sendMail(path);
		else if (service.equals("ftp"))
			bdl = ftpUpload(path);

		if (bdl != null) {
			if (bdl.getBoolean("success", false) == false) {
				mPrefs.edit()
						.putString(
								"failedUploads",
								mPrefs.getString("failedUploads", "") + ";"
										+ path).commit();
			} else {
				if (mPrefs.getBoolean("uploadDeleteAfter", false)) {
					File file = new File(path);
					if (file.delete())
						bdl = makeResultBundle(
								R.string.success_uploadAndDelete, true);
					else {
						bdl = makeResultBundle(
								R.string.warning_uploadDeleteFailed, true);
					}
				}
				;
			}

			// send msg for toast notification
			Message msg = new Message();
			msg.setData(bdl);
			handler.sendMessage(msg);
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(
					getApplicationContext(),
					getString(msg.getData().getInt("msg",
							R.string.error_generalError)), Toast.LENGTH_LONG)
					.show();
		}
	};

}
