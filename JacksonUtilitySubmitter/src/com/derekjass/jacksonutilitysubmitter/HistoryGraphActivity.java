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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_history_graph);

		graph = (BarGraph) findViewById(R.id.graph);
		graph.setMaxValue(200);
		graph.setBarCount(12);
		graph.setBarColor(0xFF33B5E5);
		
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("Jan");
		labels.add("Feb");
		labels.add("Mar");
		graph.setLabels(labels);
	}

	public void setupGraph(View v) {
		graph.setBarCount((int) (Math.random() * 12 + 1));
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
