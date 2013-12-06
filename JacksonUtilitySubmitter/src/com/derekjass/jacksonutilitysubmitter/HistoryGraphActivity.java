package com.derekjass.jacksonutilitysubmitter;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.derekjass.jacksonutilitysubmitter.views.BarView;

public class HistoryGraphActivity extends ActionBarActivity {

	private BarView mBar;
	private BarView mBar2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_history_graph);

		mBar = (BarView) findViewById(R.id.bar);
		mBar.getBackground().mutate().setColorFilter(
				new LightingColorFilter(Color.GREEN, 0));

		mBar2 = (BarView) findViewById(R.id.bar2);
		mBar2.getBackground().mutate().setColorFilter(
				new LightingColorFilter(Color.BLUE, 0));
	}

	public void animateBar(View v) {
		mBar.setScale((float) Math.random());

		mBar2.setScale((float) Math.random());
	}
}
