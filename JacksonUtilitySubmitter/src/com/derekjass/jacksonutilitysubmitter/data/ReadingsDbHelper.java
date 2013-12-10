package com.derekjass.jacksonutilitysubmitter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ReadingsDbHelper extends SQLiteOpenHelper {

	public static final class Columns implements BaseColumns {
		public static final String TABLE_NAME = "readings";
		public static final String DATE = "date";
		public static final String ELECTRIC = "electric";
		public static final String WATER = "water";
		public static final String GAS = "gas";
	}

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "Readings.db";

	private static final String SQL_CREATE_MATCHES =
			"CREATE TABLE " + Columns.TABLE_NAME + " (" +
					Columns._ID + " INTEGER PRIMARY KEY," +
					Columns.DATE + " INTEGER," +
					Columns.ELECTRIC + " INTEGER," +
					Columns.WATER + " INTEGER," +
					Columns.GAS + " INTEGER)";

	public ReadingsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_MATCHES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
	}
}
