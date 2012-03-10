package de.chaos_darmstadt.schlafwandler.EnCam;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class Help extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		TextView helpText = (TextView) findViewById(R.id.helpTextTextView);
		helpText.setText(Html.fromHtml(getString(R.string.helpText).replaceAll(
				"lt;", "<")));
	}
}
