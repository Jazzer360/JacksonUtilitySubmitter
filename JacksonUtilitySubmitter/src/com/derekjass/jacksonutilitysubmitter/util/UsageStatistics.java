package com.derekjass.jacksonutilitysubmitter.util;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import android.database.Cursor;

public class UsageStatistics {

	private NavigableMap<Long, Integer> mReadings;

	public UsageStatistics(Cursor data, int timeColIndex, int valueColIndex) {
		mReadings = new TreeMap<Long, Integer>();
		if (!data.moveToFirst()) return;
		do {
			long time = data.getLong(timeColIndex);
			int reading = data.getInt(valueColIndex);
			mReadings.put(time, reading);
		} while (data.moveToNext());
	}

	public int getUsage(long from, long to) {
		if (mReadings.isEmpty() ||
				from < mReadings.firstKey() ||
				to > mReadings.lastKey()) return 0;

		float totalUsage = 0f;

		Entry<Long, Integer> fromEntry = mReadings.floorEntry(from);
		Entry<Long, Integer> toEntry = mReadings.higherEntry(from);

		do {
			if (fromEntry.getKey() < from || toEntry.getKey() > to) {
				long span = toEntry.getKey() - fromEntry.getKey();
				int usage = getUnitsUsed(
						fromEntry.getValue(), toEntry.getValue());
				float rate = (float) usage / span;

				totalUsage += rate * (Math.min(toEntry.getKey(), to) -
						Math.max(fromEntry.getKey(), from));
			} else {
				totalUsage += getUnitsUsed(
						fromEntry.getValue(), toEntry.getValue());
			}

			fromEntry = toEntry;
			toEntry = mReadings.higherEntry(fromEntry.getKey());
		} while (fromEntry.getKey() < to);

		return (int) totalUsage;
	}

	private int getUnitsUsed(int start, int end) {
		if (end > start) return end - start;
		int meterDigits = 1;
		int lastReading = start;
		while ((lastReading /= 10) > 0) meterDigits++;
		int meterCapacity = (int) Math.pow(10, meterDigits);
		return meterCapacity - start + end;
	}
}
