package com.derekjass.jacksonutilitysubmitter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class HistoryGraphActivity extends ActionBarActivity {

	private View mBar;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_history_graph);

		mBar = findViewById(R.id.bar);
		mBar.getBackground().setColorFilter(
				new LightingColorFilter(Color.GREEN, 0));
	}

	@SuppressLint("NewApi")
	public void animateBar(View v) {
		mBar.setPivotY(mBar.getHeight());
		mBar.animate()
		.setInterpolator(new AccelerateDecelerateInterpolator())
		.scaleY((float) (Math.random()));
	}
}
