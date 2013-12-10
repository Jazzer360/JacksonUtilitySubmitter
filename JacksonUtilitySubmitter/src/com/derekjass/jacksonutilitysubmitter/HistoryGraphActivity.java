package com.derekjass.jacksonutilitysubmitter;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.derekjass.jacksonutilitysubmitter.views.BarGraph;

public class HistoryGraphActivity extends ActionBarActivity {

	private BarGraph graph;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_history_graph);

		graph = (BarGraph) findViewById(R.id.graph);
		graph.setMaxValue(200);
		graph.setBarCount(10);
		graph.setBarColor(0xFF33B5E5);
	}

	public void setupGraph(View v) {
		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		values.add((int) (Math.random() * 200));
		graph.setValues(values);
	}
}
