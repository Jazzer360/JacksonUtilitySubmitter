package com.derekjass.jacksonutilitysubmitter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {

	private static final int DEFAULT_TIME = 720;

	private int mTime;
	private TimePicker mPicker;

	public TimePreference(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);

		setDialogLayoutResource(R.layout.preference_time);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		mPicker = (TimePicker) v;

		mPicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
		mPicker.setCurrentHour(mTime / 60);
		mPicker.setCurrentMinute(mTime % 60);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			mTime = mPicker.getCurrentHour() * 60;
			mTime += mPicker.getCurrentMinute();

			persistInt(mTime);

			setTimeSummary();
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return(a.getInteger(index, DEFAULT_TIME));
	}

	@Override
	protected void onSetInitialValue(boolean restoreVal, Object defaultVal) {
		if (restoreVal) {
			mTime = getPersistedInt(DEFAULT_TIME);
		}
		else {
			mTime = (Integer) defaultVal;
			persistInt(mTime);
		}

		setTimeSummary();
	}

	private void setTimeSummary() {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.add(Calendar.MINUTE, mTime);

		SimpleDateFormat timeFormat = new SimpleDateFormat(
				DateFormat.is24HourFormat(getContext()) ?
						"HH:mm" : "h:mm a", Locale.US);

		setSummary(timeFormat.format(c.getTime()));
	}
}
