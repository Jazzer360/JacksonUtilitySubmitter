package com.derekjass.jacksonutilitysubmitter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper;
import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;

public class MainActivity extends ActionBarActivity {

	private EditText mNameText;
	private EditText mAddressText;
	private EditText mElectricText;
	private EditText mWaterText;

	private SharedPreferences mPrefs;

	private ReadingsDbHelper mDbHelper;

	private BackupManager mBackupManager;

	private IInAppBillingService mBillingService;
	private HistoryFeature mHistoryFeature = HistoryFeature.UNKNOWN;

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBillingService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBillingService = IInAppBillingService.Stub.asInterface(service);
			mCheckForPurchase.execute();
		}
	};

	private AsyncTask<Void, Void, HistoryFeature> mCheckForPurchase =
			new AsyncTask<Void, Void, HistoryFeature>() {
		@Override
		protected HistoryFeature doInBackground(Void... params) {
			try {
				Bundle purchases = mBillingService.getPurchases(
						3, getPackageName(), "inapp", null);
				if (purchases.getInt("RESPONSE_CODE") != 0) {
					return HistoryFeature.UNKNOWN;
				}
				ArrayList<String> skus = purchases.getStringArrayList(
						"INAPP_PURCHASE_ITEM_LIST");
				return skus.contains(HistoryFeature.SKU) ?
						HistoryFeature.PURCHASED : HistoryFeature.NOT_PURCHASED;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return HistoryFeature.UNKNOWN;
		}

		@Override
		protected void onPostExecute(HistoryFeature result) {
			mHistoryFeature = result;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bindService(new
				Intent("com.android.vending.billing.InAppBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);

		sendBroadcast(new Intent(this, SetAlarmReceiver.class));

		mNameText = (EditText) findViewById(R.id.nameEditText);
		mAddressText = (EditText) findViewById(R.id.addressEditText);
		mElectricText = (EditText) findViewById(R.id.electricEditText);
		mWaterText = (EditText) findViewById(R.id.waterEditText);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		mBackupManager = new BackupManager(this);

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
			launchHistoryActivity();
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void launchHistoryActivity() {
		switch (mHistoryFeature) {
		case NOT_PURCHASED:
			promptHistoryPurchase();
			break;
		case PURCHASED:
			startActivity(new Intent(this, HistoryGraphActivity.class));
			break;
		case UNKNOWN:
			AsyncTask.Status status = mCheckForPurchase.getStatus();
			if (!(status == Status.RUNNING || status == Status.PENDING)) {
				mCheckForPurchase.execute();
			}
			break;
		}
	}

	private void promptHistoryPurchase() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.purchase_feature);
		builder.setMessage(R.string.feature_description);
		builder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mPurchaseTask.execute();
			}
		});
		builder.setNegativeButton(android.R.string.no, null);
		builder.show();
	}

	private AsyncTask<Void, Void, PendingIntent> mPurchaseTask =
			new AsyncTask<Void, Void, PendingIntent>() {
		@Override
		protected PendingIntent doInBackground(Void... params) {
			try {
				Bundle bundle = mBillingService.getBuyIntent(3,
						getPackageName(), HistoryFeature.SKU, "inapp", null);
				if (bundle.getInt("RESPONSE_CODE") == 0) {
					return bundle.getParcelable("BUY_INTENT");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(PendingIntent result) {
			if (result == null) return;
			try {
				startIntentSenderForResult(result.getIntentSender(), 0,
						new Intent(), 0, 0, 0);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode) {
		case 0:
			if (resultCode == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(
							data.getStringExtra("INAPP_PURCHASE_DATA"));
					String sku = jo.getString("productId");
					if (sku.equals(HistoryFeature.SKU)) {
						mHistoryFeature = HistoryFeature.PURCHASED;
						launchHistoryActivity();
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
			break;
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBillingService != null) {
			unbindService(mServiceConn);
		}
	}

	public void submitReadings(View v) {
		mSaveReadingsTask.execute(getContentValues());
		saveSubmittalTime();

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
			startActivity(i);
		} else {
			Toast.makeText(this,
					R.string.error_no_email_client,
					Toast.LENGTH_SHORT).show();
		}
	}

	private AsyncTask<ContentValues, Void, Void> mSaveReadingsTask =
			new AsyncTask<ContentValues, Void, Void>() {
		@Override
		protected Void doInBackground(ContentValues... params) {
			if (mDbHelper == null) {
				mDbHelper = new ReadingsDbHelper(MainActivity.this);
			}
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			db.insert(Columns.TABLE_NAME, null, params[0]);
			mDbHelper.close();
			mBackupManager.dataChanged();
			return null;
		}
	};

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
		int result = -1;
		if (TextUtils.isEmpty(view.getText())) return result;
		try {
			result = Integer.valueOf(view.getText().toString());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}

	private enum HistoryFeature {
		PURCHASED, NOT_PURCHASED, UNKNOWN;
		public static String SKU = "history_feature";
	}
}
