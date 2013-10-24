package com.derekjass.jacksonutilitysubmitter;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {

	private static final int DEFAULT_TIME = 720;

	private int time;
	private TimePicker picker = null;

	public TimePreference(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);

		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimePicker(getContext());

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
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return(a.getInteger(index, DEFAULT_TIME));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			time = this.getPersistedInt(DEFAULT_TIME);
		}
		else {
			time = (Integer) defaultValue;
			persistInt(time);
		}
	}
}
