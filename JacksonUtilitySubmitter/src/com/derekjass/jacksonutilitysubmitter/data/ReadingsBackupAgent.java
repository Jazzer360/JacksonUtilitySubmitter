package com.derekjass.jacksonutilitysubmitter.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.ParcelFileDescriptor;

import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;

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

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteStream);

		int recordCount = cursor.getCount();
		out.writeInt(recordCount);

		int dateCol = cursor.getColumnIndexOrThrow(Columns.DATE);
		int electricCol = cursor.getColumnIndexOrThrow(Columns.ELECTRIC);
		int waterCol = cursor.getColumnIndexOrThrow(Columns.WATER);
		int gasCol = cursor.getColumnIndexOrThrow(Columns.GAS);

		cursor.moveToFirst();

		for (int i = 0; i < recordCount; i++) {
			out.writeLong(cursor.getLong(dateCol));
			out.writeInt(cursor.getInt(electricCol));
			out.writeInt(cursor.getInt(waterCol));
			out.writeInt(cursor.getInt(gasCol));
			cursor.moveToNext();
		}

		byte[] buffer = byteStream.toByteArray();
		int len = buffer.length;
		data.writeEntityHeader(READINGS_KEY, len);
		data.writeEntityData(buffer, len);

		cursor.close();
		helper.close();
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
				ByteArrayInputStream byteStream = new ByteArrayInputStream(
						dataBuf);
				DataInputStream in = new DataInputStream(byteStream);

				ReadingsDbHelper helper = new ReadingsDbHelper(
						getApplicationContext());
				SQLiteDatabase db = helper.getWritableDatabase();

				int recordCount = in.readInt();
				for (int i = 0; i < recordCount; i++) {
					ContentValues cv = new ContentValues();
					cv.put(Columns.DATE, in.readLong());
					cv.put(Columns.ELECTRIC, in.readInt());
					cv.put(Columns.WATER, in.readInt());
					cv.put(Columns.GAS, in.readInt());

					db.insert(Columns.TABLE_NAME, null, cv);
				}

				helper.close();
			} else {
				data.skipEntityData();
			}
		}
	}
}
