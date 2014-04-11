package com.derekjass.jacksonutilitysubmitter;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.derekjass.jacksonutilitysubmitter.PurchaseGraphFragment.GraphPurchasingAgent;

public class MainActivity extends ActionBarActivity
implements ActionBar.TabListener, GraphPurchasingAgent {

	private static final int PURCHASE_GRAPH_FEATURE_REQUEST = 0;

	private IInAppBillingService mBillingService;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBillingService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBillingService = IInAppBillingService.Stub.asInterface(service);
			new CheckPurchasesTask().execute();
		}
	};

	private class CheckPurchasesTask
	extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				if (mBillingService == null) return false;
				Bundle purchases = mBillingService.getPurchases(
						3, getPackageName(), "inapp", null);
				if (purchases.getInt("RESPONSE_CODE") != 0) return false;
				ArrayList<String> details = purchases.getStringArrayList(
						"INAPP_PURCHASE_DATA_LIST");

				for (String detail : details) {
					JSONObject jo = new JSONObject(detail);
					if (jo.getInt("purchaseState") == 0 &&
							jo.getString("productId").equals(
									GraphFeature.SKU)) {
						mGraphFeature = GraphFeature.PURCHASED;
					}
				}

				if (mGraphFeature == GraphFeature.UNKNOWN) {
					mGraphFeature = GraphFeature.NOT_PURCHASED;
				}
				return true;
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean checkWasSuccessful) {
			if (checkWasSuccessful) {
				mViewPager.getAdapter().notifyDataSetChanged();
			} else {
				Toast.makeText(MainActivity.this,
						R.string.error_billing,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private volatile GraphFeature mGraphFeature = GraphFeature.UNKNOWN;
	private enum GraphFeature {
		PURCHASED, NOT_PURCHASED, UNKNOWN;
		public static final String SKU = "history_feature";
	}

	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		bindService(new
				Intent("com.android.vending.billing.InAppBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(new MyPageAdapter(getSupportFragmentManager()));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
			@Override
			public void onPageScrolled(int pos, float offset, int pixels) {}
			@Override
			public void onPageScrollStateChanged(int state) {}
		});

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.submit)).setTabListener(this));
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.history)).setTabListener(this));
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.graph)).setTabListener(this));

		sendBroadcast(new Intent(this, SetAlarmReceiver.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBillingService != null) {
			unbindService(mServiceConn);
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
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Fragment getGraphFragment() {
		switch (mGraphFeature) {
		case NOT_PURCHASED:
			return new PurchaseGraphFragment();
		case PURCHASED:
			return new GraphFragment();
		case UNKNOWN:
		default:
			new CheckPurchasesTask().execute();
			return new PurchaseGraphFragment();
		}
	}


	@Override
	public void purchaseGraph() {
		new PurchaseGraphFeatureTask().execute();
	}


	private class PurchaseGraphFeatureTask
	extends AsyncTask<Void, Void, PendingIntent> {
		@Override
		protected PendingIntent doInBackground(Void... params) {
			try {
				if (mBillingService == null) return null;
				Bundle bundle = mBillingService.getBuyIntent(3,
						getPackageName(), GraphFeature.SKU, "inapp", null);
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
			if (result == null) {
				Toast.makeText(MainActivity.this,
						R.string.error_billing,
						Toast.LENGTH_LONG).show();
				return;
			}
			try {
				startIntentSenderForResult(result.getIntentSender(),
						PURCHASE_GRAPH_FEATURE_REQUEST,
						new Intent(), 0, 0, 0);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode) {
		case PURCHASE_GRAPH_FEATURE_REQUEST:
			if (resultCode == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(
							data.getStringExtra("INAPP_PURCHASE_DATA"));
					if (jo.getString("productId").equals(GraphFeature.SKU) &&
							jo.getInt("purchaseState") == 0) {
						mGraphFeature = GraphFeature.PURCHASED;
						mViewPager.getAdapter().notifyDataSetChanged();
						mViewPager.setCurrentItem(GRAPH_TAB);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return;
		}
	}

	private static final int SUBMIT_TAB = 0;
	private static final int HISTORY_TAB = 1;
	private static final int GRAPH_TAB = 2;

	private class MyPageAdapter extends FragmentStatePagerAdapter {

		public MyPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {
			switch (index) {
			case SUBMIT_TAB:
				return new SubmitFragment();
			case HISTORY_TAB:
				return new HistoryFragment();
			case GRAPH_TAB:
				return getGraphFragment();
			default:
				throw new IllegalArgumentException(
						"No page fragment for positon " + index);
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public int getItemPosition(Object object) {
			if (object instanceof PurchaseGraphFragment) {
				return POSITION_NONE;
			}
			return super.getItemPosition(object);
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
}
