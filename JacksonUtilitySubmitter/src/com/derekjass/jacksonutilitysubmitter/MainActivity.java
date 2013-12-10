package com.derekjass.jacksonutilitysubmitter;

import java.util.List;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper;
import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;

public class MainActivity extends ActionBarActivity {

	private class SaveReadingsTask
	extends AsyncTask<ContentValues, Void, Void> {
		@Override
		protected Void doInBackground(ContentValues... params) {
			if (mDbHelper == null) {
				mDbHelper = new ReadingsDbHelper(MainActivity.this);
			}
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			db.insert(Columns.TABLE_NAME, null, params[0]);
			db.close();
			return null;
		}
	}

	private EditText mNameText;
	private EditText mAddressText;
	private EditText mElectricText;
	private EditText mWaterText;

	private SharedPreferences mPrefs;

	private ReadingsDbHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.read_meters);

		setContentView(R.layout.activity_main);

		sendBroadcast(new Intent(this, SetAlarmReceiver.class));

		mNameText = (EditText) findViewById(R.id.nameEditText);
		mAddressText = (EditText) findViewById(R.id.addressEditText);
		mElectricText = (EditText) findViewById(R.id.electricEditText);
		mWaterText = (EditText) findViewById(R.id.waterEditText);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		String name = mPrefs.getString(
				getString(R.string.pref_name), null);
		String address = mPrefs.getString(
				getString(R.string.pref_address), null);

		if (!TextUtils.isEmpty(name)) {
			mNameText.setText(name);
			mAddressText.requestFocus();
		}
		if (!TextUtils.isEmpty(address)) {
			mAddressText.setText(address);
			mElectricText.requestFocus();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.activity_main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_history_graph:
			startActivity(new Intent(this, HistoryGraphActivity.class));
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences.Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(getString(R.string.pref_name),
				mNameText.getText().toString());
		prefsEditor.putString(getString(R.string.pref_address),
				mAddressText.getText().toString());
		prefsEditor.commit();
	}

	public void submitReadings(View v) {
		new SaveReadingsTask().execute(getContentValues());

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
			saveSubmittalTime();
			startActivity(i);
		} else {
			Toast.makeText(this,
					R.string.error_no_email_client,
					Toast.LENGTH_SHORT).show();
		}
	}

	private String getMessageBody() {
		StringBuilder body = new StringBuilder();
		body.append(mNameText.getText());
		body.append("\n");
		body.append(mAddressText.getText());
		body.append("\n\n");
		body.append(getString(R.string.electric));
		body.append(" ");
		body.append(mElectricText.getText());
		body.append("\n");
		body.append(getString(R.string.water));
		body.append(" ");
		body.append(mWaterText.getText());

		return body.toString();
	}

	private void saveSubmittalTime() {
		SharedPreferences.Editor prefsEditor = mPrefs.edit();
		prefsEditor.putLong(getString(R.string.pref_last_submit),
				System.currentTimeMillis());
		prefsEditor.commit();
	}

	private ContentValues getContentValues() {
		ContentValues vals = new ContentValues();
		vals.put(Columns.DATE, System.currentTimeMillis());
		vals.put(Columns.ELECTRIC, getIntFromEditText(mElectricText));
		vals.put(Columns.WATER, getIntFromEditText(mWaterText));
		return vals;
	}

	private static int getIntFromEditText(EditText view) {
		int result = 0;
		try {
			result = Integer.valueOf(view.getText().toString());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}
}
