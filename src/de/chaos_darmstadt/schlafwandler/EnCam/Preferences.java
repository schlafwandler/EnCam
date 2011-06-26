package de.chaos_darmstadt.schlafwandler.EnCam;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.chaos_darmstadt.schlafwandler.EnCam.R;

public class Preferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preference);
	}
}
