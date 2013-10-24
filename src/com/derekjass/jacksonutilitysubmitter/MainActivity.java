package com.derekjass.jacksonutilitysubmitter;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private static final int SUBMITTAL_REQUEST = 1;

	private EditText nameText;
	private EditText addressText;
	private EditText electricText;
	private EditText waterText;

	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enableActionBarOverflow();
		setContentView(R.layout.activity_main);

		sendBroadcast(new Intent(this, SetAlarmReceiver.class));

		nameText = (EditText) findViewById(R.id.nameEditText);
		addressText = (EditText) findViewById(R.id.addressEditText);
		electricText = (EditText) findViewById(R.id.electricEditText);
		waterText = (EditText) findViewById(R.id.waterEditText);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		prefs = getSharedPreferences(PrefKeys.FILE, MODE_PRIVATE);

		String name = prefs.getString(PrefKeys.NAME, null);
		String address = prefs.getString(PrefKeys.ADDRESS, null);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.activity_main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putString(
				PrefKeys.NAME, nameText.getText().toString());
		prefsEditor.putString(
				PrefKeys.ADDRESS, addressText.getText().toString());
		prefsEditor.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (requestCode == SUBMITTAL_REQUEST) {
			if (resultCode == RESULT_OK) {
				saveSubmittalTime();
				showToast(R.string.submittal_succeeded);
			} else {
				showToast(R.string.submittal_failed);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void submitReadings(View v) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL,
				new String[]{getString(R.string.email_address)});
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
		i.putExtra(Intent.EXTRA_TEXT, getMessageBody());
	
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(i, 0);
		boolean isIntentSafe = activities.size() > 0;
	
		if (isIntentSafe) {
			startActivityForResult(i, SUBMITTAL_REQUEST);
		} else {
			showToast(R.string.error_no_email_client);
		}
	}

	private String getMessageBody() {
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

		return body.toString();
	}

	private void saveSubmittalTime() {
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putLong(PrefKeys.LAST_SUBMIT, System.currentTimeMillis());
		prefsEditor.commit();
	}

	private void showToast(int resId) {
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}

	private void enableActionBarOverflow() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
