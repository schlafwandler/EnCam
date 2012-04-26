package de.chaos_darmstadt.schlafwandler.EnCam;

public class Apg {
	public static final int DECRYPT_MESSAGE = 0x21070001;
	public static final int ENCRYPT_MESSAGE = 0x21070002;
	public static final int SELECT_PUBLIC_KEYS = 0x21070003;
	public static final int SELECT_SECRET_KEY = 0x21070004;

	public static final String EXTRA_TEXT = "text";
	public static final String EXTRA_DATA = "data";
	public static final String EXTRA_STATUS = "status";
	public static final String EXTRA_ERROR = "error";
	public static final String EXTRA_DECRYPTED_MESSAGE = "decryptedMessage";
	public static final String EXTRA_ENCRYPTED_MESSAGE = "encryptedMessage";
	public static final String EXTRA_SIGNATURE = "signature";
	public static final String EXTRA_SIGNATURE_KEY_ID = "signatureKeyId";
	public static final String EXTRA_SIGNATURE_USER_ID = "signatureUserId";
	public static final String EXTRA_SIGNATURE_SUCCESS = "signatureSuccess";
	public static final String EXTRA_SIGNATURE_UNKNOWN = "signatureUnknown";
	public static final String EXTRA_USER_ID = "userId";
	public static final String EXTRA_KEY_ID = "keyId";
	public static final String EXTRA_REPLY_TO = "replyTo";
	public static final String EXTRA_SEND_TO = "sendTo";
	public static final String EXTRA_SUBJECT = "subject";
	public static final String EXTRA_ENCRYPTION_KEY_IDS = "encryptionKeyIds";
	public static final String EXTRA_SELECTION = "selection";
	public static final String EXTRA_MESSAGE = "message";
	public static final String EXTRA_PROGRESS = "progress";
	public static final String EXTRA_MAX = "max";
	public static final String EXTRA_ACCOUNT = "account";

	public static class Intent {
		public static final String DECRYPT = "org.thialfihar.android.apg.intent.DECRYPT";
		public static final String ENCRYPT = "org.thialfihar.android.apg.intent.ENCRYPT";
		public static final String DECRYPT_FILE = "org.thialfihar.android.apg.intent.DECRYPT_FILE";
		public static final String ENCRYPT_FILE = "org.thialfihar.android.apg.intent.ENCRYPT_FILE";
		public static final String DECRYPT_AND_RETURN = "org.thialfihar.android.apg.intent.DECRYPT_AND_RETURN";
		public static final String ENCRYPT_AND_RETURN = "org.thialfihar.android.apg.intent.ENCRYPT_AND_RETURN";
		public static final String SELECT_PUBLIC_KEYS = "org.thialfihar.android.apg.intent.SELECT_PUBLIC_KEYS";
		public static final String SELECT_SECRET_KEY = "org.thialfihar.android.apg.intent.SELECT_SECRET_KEY";
	}
}
