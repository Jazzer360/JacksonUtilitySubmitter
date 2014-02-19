package com.derekjass.jacksonutilitysubmitter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;
import com.derekjass.jacksonutilitysubmitter.data.ReadingsLoader;
import com.derekjass.jacksonutilitysubmitter.util.UsageStatistics;
import com.derekjass.jacksonutilitysubmitter.views.BarGraph;

public class HistoryGraphActivity extends ActionBarActivity
implements LoaderCallbacks<Cursor> {

	private ProgressBar mProgress;
	private LinearLayout mGraphs;
	private BarGraph mElectricGraph;
	private BarGraph mWaterGraph;

	private UsageStatistics mElectricStats;
	private UsageStatistics mWaterStats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_history_graph);

		mProgress = (ProgressBar) findViewById(R.id.progress);
		mGraphs = (LinearLayout) findViewById(R.id.graphs);
		mElectricGraph = (BarGraph) findViewById(R.id.electricGraph);
		mWaterGraph = (BarGraph) findViewById(R.id.waterGraph);

		getSupportLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new ReadingsLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int dateIndex = data.getColumnIndexOrThrow(Columns.DATE);
		int electricIndex = data.getColumnIndexOrThrow(Columns.ELECTRIC);
		int waterIndex = data.getColumnIndexOrThrow(Columns.WATER);
		mElectricStats = new UsageStatistics(data, dateIndex, electricIndex);
		mWaterStats = new UsageStatistics(data, dateIndex, waterIndex);

		setupGraphs();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {}

	private void setupGraphs() {
		Calendar c = Calendar.getInstance();
		int startYear = c.get(Calendar.YEAR) - 1;
		int startMonth = c.get(Calendar.MONTH);
		c.clear();
		c.set(startYear, startMonth, 1);

		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<Integer> electricValues = new ArrayList<Integer>();
		ArrayList<Integer> waterValues = new ArrayList<Integer>();

		while (true) {
			String label = c.getDisplayName(
					Calendar.MONTH, Calendar.SHORT, Locale.US);

			long start = c.getTimeInMillis();
			c.add(Calendar.MONTH, 1);
			long end = c.getTimeInMillis();

			if (end > System.currentTimeMillis()) break;

			labels.add(label);
			electricValues.add(mElectricStats.getUsage(start, end));
			waterValues.add(mWaterStats.getUsage(start, end));
		}

		mElectricGraph.setMaxValue(Collections.max(electricValues));
		mWaterGraph.setMaxValue(Collections.max(waterValues));
		mElectricGraph.setLabels(labels);
		mWaterGraph.setLabels(labels);
		mProgress.setVisibility(View.GONE);
		mGraphs.setVisibility(View.VISIBLE);

		mElectricGraph.setValues(electricValues);
		mWaterGraph.setValues(waterValues);
	}
}
