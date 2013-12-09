package com.derekjass.jacksonutilitysubmitter;

import java.util.ArrayList;

import android.graphics.Color;
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
	}

	public void setupGraph(View v) {
		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(10);
		values.add(8);
		graph.setMaxValue(11);
		graph.setBarCount(2);
		graph.setBarColor(Color.GREEN);
		graph.setValues(values);
	}
}
