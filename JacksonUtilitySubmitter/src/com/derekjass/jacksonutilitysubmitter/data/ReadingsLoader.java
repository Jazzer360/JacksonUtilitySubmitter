package com.derekjass.jacksonutilitysubmitter.data;

import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

public class ReadingsLoader extends AsyncTaskLoader<Cursor> {

	private ReadingsDbHelper mHelper;
	private Cursor mCursor;

	public ReadingsLoader(Context context) {
		super(context);
		mHelper = new ReadingsDbHelper(context);
	}

	@Override
	public Cursor loadInBackground() {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		mCursor = db.query(Columns.TABLE_NAME,
				null,
				null,
				null,
				null,
				null,
				Columns.DATE + " ASC");
		return mCursor;
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		mCursor.close();
	}
}
