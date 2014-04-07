package com.derekjass.jacksonutilitysubmitter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.derekjass.jacksonutilitysubmitter.provider.ReadingsContract.Readings;
import com.derekjass.jacksonutilitysubmitter.util.UsageStatistics;
import com.derekjass.jacksonutilitysubmitter.views.BarGraph;

public class GraphFragment extends Fragment
implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

	private ProgressBar mProgress;
	private LinearLayout mGraphs;
	private BarGraph mElectricGraph;
	private BarGraph mWaterGraph;
	private BarGraph mGasGraph;
	private TextView mGasText;

	private UsageStatistics mElectricStats;
	private UsageStatistics mWaterStats;
	private UsageStatistics mGasStats;

	private SharedPreferences mPrefs;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().initLoader(0, null, this).forceLoad();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_graph,
				container, false);

		mProgress = (ProgressBar) view.findViewById(R.id.progress);
		mGraphs = (LinearLayout) view.findViewById(R.id.graphs);
		mElectricGraph = (BarGraph) view.findViewById(R.id.electricGraph);
		mWaterGraph = (BarGraph) view.findViewById(R.id.waterGraph);
		mGasGraph = (BarGraph) view.findViewById(R.id.gasGraph);
		mGasText = (TextView) view.findViewById(R.id.gasText);

		return view;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(),
				Readings.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int dateIndex = data.getColumnIndexOrThrow(Readings.COLUMN_DATE);
		int electricIndex = data.getColumnIndexOrThrow(Readings.COLUMN_ELECTRIC);
		int waterIndex = data.getColumnIndexOrThrow(Readings.COLUMN_WATER);
		int gasIndex = data.getColumnIndexOrThrow(Readings.COLUMN_GAS);
		mElectricStats = new UsageStatistics(data, dateIndex, electricIndex);
		mWaterStats = new UsageStatistics(data, dateIndex, waterIndex);
		mGasStats = new UsageStatistics(data, dateIndex, gasIndex);

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
		ArrayList<Integer> gasValues = new ArrayList<Integer>();

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
			gasValues.add(mGasStats.getUsage(start, end));
		}

		mElectricGraph.setMaxValue(Collections.max(electricValues));
		mWaterGraph.setMaxValue(Collections.max(waterValues));
		mGasGraph.setMaxValue(Collections.max(gasValues));
		mElectricGraph.setLabels(labels);
		mWaterGraph.setLabels(labels);
		mGasGraph.setLabels(labels);
		mProgress.setVisibility(View.GONE);
		mGraphs.setVisibility(View.VISIBLE);

		mElectricGraph.setValues(electricValues);
		mWaterGraph.setValues(waterValues);
		mGasGraph.setValues(gasValues);

		boolean gasEnabled = mPrefs.getBoolean(
				getString(R.string.pref_enable_gas), false);
		mGasGraph.setVisibility(gasEnabled ? View.VISIBLE : View.GONE);
		mGasText.setVisibility(gasEnabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.pref_enable_gas))) {
			setupGraphs();
		}
	}
}
