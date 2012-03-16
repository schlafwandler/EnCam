package de.chaos_darmstadt.schlafwandler.EnCam;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.regex.Matcher;

import org.apache.commons.net.ftp.FTPClient;

import android.app.IntentService;
import android.content.Intent;
import de.chaos_darmstadt.schlafwandler.EnCam.EnCam.ResponseReceiver;

public class UploadService extends IntentService {
	static final int KIND_NONE = 0;
	static final int KIND_MAIL = 1;
	static final int KIND_FTP = 2;
	static final int KIND_SHARE = 3;

	static final String KIND = "kind";
	static final String RESULT = "result";
	static final String CONNECTION_DATA = "data";

	public UploadService() {
		super("upload");
	}

	private int sendMail(String[] data) {

		try {
			Mail mail = new Mail();

			mail.setContent(data[1], data[2], data[3]);

			mail.setAccount(data[4], data[5], data[6], data[7], data[8],
					data[9]);

			if (!data[0].equals("")) {
				mail.addAttachment(data[0]);
			}
			if (!mail.send())
				return R.string.error_mailSendError;
		} catch (Exception e) {
			return R.string.error_mailSendError;
		}
		return R.string.success_mailSend;
	}

	private int ftpUpload(String[] data) {
		int result = R.string.error_ftpUploadError;
		try {
			FTPClient ftpClient = new FTPClient();

			ftpClient.connect(data[1], Integer.parseInt(data[2]));
			ftpClient.enterLocalPassiveMode();
			ftpClient.login(data[3], data[4]);
			ftpClient.changeWorkingDirectory(data[5]);
			ftpClient
					.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			BufferedInputStream buffIn = null;
			buffIn = new BufferedInputStream(new FileInputStream(data[0]));
			ftpClient.enterLocalPassiveMode();
			if (ftpClient.storeFile(data[0].substring(
					data[0].lastIndexOf(Matcher.quoteReplacement("/")) + 1,
					data[0].length()), buffIn))
				result = R.string.success_ftpUpload;
			else
				result = R.string.error_ftpUploadError;
			buffIn.close();
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (Exception e) {
			result = R.string.error_ftpUploadError;
		}
		return result;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		int result = R.string.error_generalError;

		switch (intent.getIntExtra(KIND, KIND_NONE)) {
		case KIND_MAIL:
			result = sendMail(intent.getStringArrayExtra(CONNECTION_DATA));
			break;
		case KIND_FTP:
			result = ftpUpload(intent.getStringArrayExtra(CONNECTION_DATA));
			break;
		}
		broadcastIntent.putExtra(RESULT, result);
		sendBroadcast(broadcastIntent);

	}
}
