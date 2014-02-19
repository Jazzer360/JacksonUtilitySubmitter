package com.derekjass.jacksonutilitysubmitter.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.ParcelFileDescriptor;

public class ReadingsBackupAgent extends BackupAgent {

	private static final String READINGS_KEY = "readings";

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		ReadingsDbHelper helper = new ReadingsDbHelper(getApplicationContext());
		SQLiteDatabase readingsDb = helper.getReadableDatabase();
		Cursor cursor = readingsDb.query(
				Columns.TABLE_NAME,
				null,
				null,
				null,
				null,
				null,
				Columns.DATE + " ASC");

		ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
		DataOutputStream outWriter = new DataOutputStream(bufStream);

		if (cursor.moveToFirst() == false) return;

		int dateCol = cursor.getColumnIndexOrThrow(Columns.DATE);
		int electricCol = cursor.getColumnIndexOrThrow(Columns.ELECTRIC);
		int waterCol = cursor.getColumnIndexOrThrow(Columns.WATER);
		int gasCol = cursor.getColumnIndexOrThrow(Columns.GAS);

		do {
			outWriter.writeLong(cursor.getLong(dateCol));
			outWriter.writeInt(cursor.getInt(electricCol));
			outWriter.writeInt(cursor.getInt(waterCol));
			outWriter.writeInt(cursor.getInt(gasCol));
		} while (cursor.moveToNext());

		byte[] buffer = bufStream.toByteArray();
		int len = buffer.length;
		data.writeEntityHeader(READINGS_KEY, len);
		data.writeEntityData(buffer, len);

		readingsDb.close();
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		while (data.readNextHeader()) {
			String key = data.getKey();
			int dataSize = data.getDataSize();

			if (READINGS_KEY.equals(key)) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				ByteArrayInputStream baStream = new ByteArrayInputStream(
						dataBuf);
				DataInputStream in = new DataInputStream(baStream);

				ReadingsDbHelper helper = new ReadingsDbHelper(
						getApplicationContext());
				SQLiteDatabase db = helper.getWritableDatabase();

				long date;
				while ((date = in.readLong()) >= 0) {
					ContentValues cv = new ContentValues();
					cv.put(Columns.DATE, date);
					cv.put(Columns.ELECTRIC, in.readInt());
					cv.put(Columns.WATER, in.readInt());
					cv.put(Columns.GAS, in.readInt());

					db.insert(Columns.TABLE_NAME, null, cv);
				}
			} else {
				data.skipEntityData();
			}
		}
	}
}
