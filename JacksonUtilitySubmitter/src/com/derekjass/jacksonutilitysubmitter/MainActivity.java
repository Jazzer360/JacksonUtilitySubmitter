package com.derekjass.jacksonutilitysubmitter;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener {

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
				mGraphFeatureFragment = GraphFeatureFragment.newInstance();
				return mGraphFeatureFragment;
			default:
				throw new IllegalArgumentException(
						"No page fragment for positon " + index);
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	}

	public static final int PURCHASE_GRAPH_FEATURE_REQUEST = 0;

	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;
	private GraphFeatureFragment mGraphFeatureFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		mPagerAdapter = new MyPageAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PURCHASE_GRAPH_FEATURE_REQUEST) {
			mGraphFeatureFragment.onActivityResult(requestCode, resultCode,
					data);
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
