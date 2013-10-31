package com.derekjass.jacksonutilitysubmitter;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {

	static final int DEFAULT_TIME = 720;

	private int time;
	private TimePicker picker;

	public TimePreference(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);

		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimePicker(getContext());
		picker.setIs24HourView(false);

		return picker;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		picker.setCurrentHour(time / 60);
		picker.setCurrentMinute(time % 60);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			time = picker.getCurrentHour() * 60;
			time += picker.getCurrentMinute();

			persistInt(time);

			setTimeSummary(time);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return(a.getInteger(index, DEFAULT_TIME));
	}

	@Override
	protected void onSetInitialValue(boolean restoreVal, Object defaultVal) {
		if (restoreVal) {
			time = this.getPersistedInt(DEFAULT_TIME);
		}
		else {
			time = (Integer) defaultVal;
			persistInt(time);
		}

		setTimeSummary(time);
	}

	private void setTimeSummary(int time) {
		StringBuilder summary = new StringBuilder();
		int hours = time / 60;
		int minutes = time % 60;

		if (hours == 0) {
			summary.append(12);
		} else if (hours > 12) {
			summary.append(hours - 12);
		} else {
			summary.append(hours);
		}

		summary.append(":").append(String.format("%02d", minutes));
		summary.append(hours >= 12 ? " PM" : " AM");

		setSummary(summary.toString());
	}
}
