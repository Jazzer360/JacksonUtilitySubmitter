package com.derekjass.jacksonutilitysubmitter.util;

import java.util.NavigableSet;
import java.util.TreeSet;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;

public class UsageStatistics {

	private static class Entry implements Comparable<Entry> {

		private long mTime;
		private int mReading;

		private Entry(long time, int reading) {
			mTime = time;
			mReading = reading;
		}

		@Override
		public int compareTo(Entry another) {
			if (mTime == another.mTime) return 0;
			return (mTime < another.mTime) ? -1 : 1;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + mReading;
			result = prime * result + (int) (mTime ^ (mTime >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof Entry)) return false;
			Entry other = (Entry) obj;
			if (mReading != other.mReading) return false;
			if (mTime != other.mTime) return false;
			return true;
		}
	}

	private NavigableSet<Entry> mReadings;

	// TODO make compatible with API 8
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public UsageStatistics(Cursor data, int dateCol, int readingCol) {
		mReadings = new TreeSet<Entry>();
		if (!data.moveToFirst()) return;
		do {
			mReadings.add(
					new Entry(data.getLong(dateCol),
							data.getInt(readingCol)));
		} while (data.moveToNext());
	}

}
