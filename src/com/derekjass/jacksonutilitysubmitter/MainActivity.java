package com.derekjass.jacksonutilitysubmitter;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String NAME_KEY =
			"com.derekjass.jacksonutilitysubmitter.NAME";
	private static final String ADDRESS_KEY =
			"com.derekjass.jacksonutilitysubmitter.ADDRESS";
	static final String PREFS_FILE =
			"com.derekjass.jacksonutilitysubmitter.PREFS";

	private EditText nameText;
	private EditText addressText;
	private EditText electricText;
	private EditText waterText;

	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sendBroadcast(new Intent(this, SetAlarmReceiver.class));

		nameText = (EditText) findViewById(R.id.nameEditText);
		addressText = (EditText) findViewById(R.id.addressEditText);
		electricText = (EditText) findViewById(R.id.electricEditText);
		waterText = (EditText) findViewById(R.id.waterEditText);

		prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

		String name = prefs.getString(NAME_KEY, null);
		String address = prefs.getString(ADDRESS_KEY, null);

		if (!TextUtils.isEmpty(name)) {
			nameText.setText(name);
			addressText.requestFocus();
		}
		if (!TextUtils.isEmpty(address)) {
			addressText.setText(address);
			electricText.requestFocus();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putString(NAME_KEY, nameText.getText().toString());
		prefsEditor.putString(ADDRESS_KEY, addressText.getText().toString());
		prefsEditor.commit();
	}

	public void submitReadings(View v) {
		StringBuilder body = new StringBuilder();
		body.append(nameText.getText());
		body.append("\n");
		body.append(addressText.getText());
		body.append("\n\n");
		body.append(getString(R.string.electric));
		body.append(" ");
		body.append(electricText.getText());
		body.append("\n");
		body.append(getString(R.string.water));
		body.append(" ");
		body.append(waterText.getText());

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL,
				new String[]{getString(R.string.email_address)});
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
		i.putExtra(Intent.EXTRA_TEXT, body.toString());

		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(i, 0);
		boolean isIntentSafe = activities.size() > 0;

		if (isIntentSafe) {
			startActivity(i);
		} else {
			Toast.makeText(this, getString(R.string.error_no_email_client),
					Toast.LENGTH_SHORT).show();
		}
	}
}
