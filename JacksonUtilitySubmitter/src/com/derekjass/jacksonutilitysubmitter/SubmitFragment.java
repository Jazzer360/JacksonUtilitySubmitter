package com.derekjass.jacksonutilitysubmitter;

import java.util.List;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.derekjass.jacksonutilitysubmitter.provider.ReadingsContract.Readings;

public class SubmitFragment extends Fragment
implements OnClickListener {

	private EditText mNameText;
	private EditText mAddressText;
	private EditText mElectricText;
	private EditText mWaterText;
	private View mGasViews;
	private EditText mGasText;

	private SharedPreferences mPrefs;

	private BackupManager mBackupManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mBackupManager = new BackupManager(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.fragment_submit, container, false);

		mNameText = (EditText) view.findViewById(R.id.nameEditText);
		mAddressText = (EditText) view.findViewById(R.id.addressEditText);
		mElectricText = (EditText) view.findViewById(R.id.electricEditText);
		mWaterText = (EditText) view.findViewById(R.id.waterEditText);
		mGasViews = (View) view.findViewById(R.id.gasFields);
		mGasText = (EditText) mGasViews.findViewById(R.id.gasEditText);

		view.findViewById(R.id.submitButton).setOnClickListener(this);

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

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		boolean showGas = mPrefs.getBoolean(
				getString(R.string.pref_enable_gas), false);

		if (showGas) {
			mGasViews.setVisibility(View.VISIBLE);
		} else {
			mGasViews.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		SharedPreferences.Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(getString(R.string.pref_name),
				mNameText.getText().toString());
		prefsEditor.putString(getString(R.string.pref_address),
				mAddressText.getText().toString());
		prefsEditor.commit();
	}

	@Override
	public void onClick(View v) {
		new SaveReadingsTask().execute(getContentValues());
		saveSubmittalTime();
		getActivity().sendBroadcast(
				new Intent(getActivity(), SetAlarmReceiver.class));

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL,
				new String[]{getString(R.string.email_address)});
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
		i.putExtra(Intent.EXTRA_TEXT, getMessageBody());

		PackageManager pm = getActivity().getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(i, 0);
		boolean isIntentSafe = activities.size() > 0;

		if (isIntentSafe) {
			startActivity(i);
		} else {
			Toast.makeText(getActivity(),
					R.string.error_no_email_client,
					Toast.LENGTH_SHORT).show();
		}
	}

	private class SaveReadingsTask
	extends AsyncTask<ContentValues, Void, Void> {
		@Override
		protected Void doInBackground(ContentValues... params) {
			getActivity().getContentResolver().insert(
					Readings.CONTENT_URI, params[0]);
			mBackupManager.dataChanged();
			return null;
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
		vals.put(Readings.COLUMN_DATE, System.currentTimeMillis());
		vals.put(Readings.COLUMN_ELECTRIC, getIntFromEditText(mElectricText));
		vals.put(Readings.COLUMN_WATER, getIntFromEditText(mWaterText));
		vals.put(Readings.COLUMN_GAS, getIntFromEditText(mGasText));
		return vals;
	}

	static int getIntFromEditText(EditText view) {
		int result = -1;
		if (TextUtils.isEmpty(view.getText())) return result;
		try {
			result = Integer.valueOf(view.getText().toString());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}
}
