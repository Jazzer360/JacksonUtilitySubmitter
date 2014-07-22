package com.derekjass.jacksonutilitysubmitter;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
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

import com.derekjass.iabhelper.BillingHelper;
import com.derekjass.iabhelper.BillingHelper.BillingError;
import com.derekjass.iabhelper.BillingHelper.OnProductPurchasedListener;
import com.derekjass.iabhelper.BillingHelper.OnPurchasesQueriedListener;
import com.derekjass.iabhelper.Purchase;
import com.derekjass.jacksonutilitysubmitter.PurchaseGraphFragment.GraphPurchasingAgent;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener, GraphPurchasingAgent {

	private class MyPageAdapter extends FragmentStatePagerAdapter {
		private static final int SUBMIT_TAB = 0;
		private static final int HISTORY_TAB = 1;
		private static final int GRAPH_TAB = 2;

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

	private enum GraphFeature {
		PURCHASED, NOT_PURCHASED, UNKNOWN;
		public static final String SKU = "history_feature";
	}

	private static final int PURCHASE_GRAPH_FEATURE_REQUEST = 0;

	private GraphFeature mGraphFeature = GraphFeature.UNKNOWN;
	private BillingHelper mBillingHelper;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.submit))
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.history)).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.graph))
				.setTabListener(this));

		sendBroadcast(new Intent(this, SetAlarmReceiver.class));

		mBillingHelper = BillingHelper.newManagedProductHelper(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBillingHelper.connect();
		mBillingHelper.queryPurchases(new OnPurchasesQueriedListener() {
			@Override
			public void onError(BillingError error) {}

			@Override
			public void onPurchasesQueried(List<Purchase> purchases) {
				mGraphFeature = GraphFeature.NOT_PURCHASED;
				for (Purchase purchase : purchases) {
					if (purchase.isPurchased()
							&& purchase.getProductId().equals(GraphFeature.SKU)) {
						mGraphFeature = GraphFeature.PURCHASED;
					}
				}
				mViewPager.getAdapter().notifyDataSetChanged();
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		mBillingHelper.disconnect();
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
			return new Fragment();
		}
	}

	@Override
	public void purchaseGraph() {
		mBillingHelper.purchaseProduct(GraphFeature.SKU, null, this,
				PURCHASE_GRAPH_FEATURE_REQUEST,
				new OnProductPurchasedListener() {
					@Override
					public void onError(BillingError error) {}

					@Override
					public void onProductPurchased(Purchase purchase) {
						if (purchase.isPurchased()) {
							if (purchase.getProductId()
									.equals(GraphFeature.SKU)) {
								mGraphFeature = GraphFeature.PURCHASED;
								mViewPager.getAdapter().notifyDataSetChanged();
							}
						}
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PURCHASE_GRAPH_FEATURE_REQUEST) {
			mBillingHelper.handleActivityResult(requestCode, resultCode, data);
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
