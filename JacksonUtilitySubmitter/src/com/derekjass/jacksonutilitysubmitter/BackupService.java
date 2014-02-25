package com.derekjass.jacksonutilitysubmitter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;

import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper;
import com.derekjass.jacksonutilitysubmitter.data.ReadingsDbHelper.Columns;

public class BackupService extends IntentService {

	public BackupService() {
		super("BackupService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!isExternalStorageWritable()) return;

		ReadingsDbHelper dbHelper = new ReadingsDbHelper(this);
		Cursor data = dbHelper.getReadableDatabase().query(
				Columns.TABLE_NAME,
				null,
				null,
				null,
				null,
				null,
				Columns.DATE + " ASC");

		if (!data.moveToFirst()) return;

		int dateCol = data.getColumnIndexOrThrow(Columns.DATE);
		int electricCol = data.getColumnIndexOrThrow(Columns.ELECTRIC);
		int waterCol = data.getColumnIndexOrThrow(Columns.WATER);
		int gasCol = data.getColumnIndexOrThrow(Columns.GAS);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);

		try {
			out.writeInt(data.getCount());
			do {
				out.writeLong(data.getLong(dateCol));
				out.writeInt(data.getInt(electricCol));
				out.writeInt(data.getInt(waterCol));
				out.writeInt(data.getInt(gasCol));
			} while (data.moveToNext());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			data.close();
			dbHelper.close();
		}

		File dir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS), "MeterReadingsBackup");
		dir.mkdirs();
		if (!dir.isDirectory()) return;
		File file = new File(dir, "readings.backup");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(buffer.toByteArray());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state) ? true : false;
	}
}
