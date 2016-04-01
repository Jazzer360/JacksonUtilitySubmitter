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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements
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
                    return new GraphFragment();
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

    private ViewPager mViewPager;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PagerAdapter mPagerAdapter = new MyPageAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        if (mViewPager == null) throw new AssertionError();
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onPageSelected(int position) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar == null) throw new AssertionError();
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int pos, float offset, int pixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) throw new AssertionError();
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
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}
