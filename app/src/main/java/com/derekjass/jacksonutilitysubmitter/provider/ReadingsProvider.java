package com.derekjass.jacksonutilitysubmitter.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.derekjass.jacksonutilitysubmitter.provider.ReadingsContract.Readings;

public class ReadingsProvider extends ContentProvider {

    private static final class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "Readings.db";

        private static final String SQL_CREATE_READINGS =
                "CREATE TABLE " + Readings.TABLE_NAME + " (" +
                        Readings._ID + " INTEGER PRIMARY KEY," +
                        Readings.COLUMN_DATE + " INTEGER," +
                        Readings.COLUMN_ELECTRIC + " INTEGER," +
                        Readings.COLUMN_WATER + " INTEGER," +
                        Readings.COLUMN_GAS + " INTEGER)";

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_READINGS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
        }

    }

    private DatabaseHelper mDbHelper;
    private static UriMatcher sUriMatcher;

    private static final int URI_READINGS = 1;
    private static final int URI_READING_ID = 2;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ReadingsContract.AUTHORITY,
                Readings.TABLE_NAME, URI_READINGS);
        sUriMatcher.addURI(ReadingsContract.AUTHORITY,
                Readings.TABLE_NAME + "/#", URI_READING_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table;
        String where;
        String orderBy = TextUtils.isEmpty(sortOrder) ?
                Readings.COLUMN_DATE + " DESC" : sortOrder;

        switch (sUriMatcher.match(uri)) {
            case URI_READINGS:
                table = Readings.TABLE_NAME;
                where = selection;
                break;
            case URI_READING_ID:
                table = Readings.TABLE_NAME;
                where = Readings._ID + "=" + uri.getLastPathSegment();
                if (selection != null) where += " AND " + selection;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(table,
                projection,
                where,
                selectionArgs,
                null,
                null,
                orderBy);
        Context context = getContext();
        assert context != null;
        c.setNotificationUri(context.getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case URI_READINGS:
                return Readings.CONTENT_TYPE;
            case URI_READING_ID:
                return Readings.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        String table;

        switch (sUriMatcher.match(uri)) {
            case URI_READINGS:
                table = Readings.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values = (initialValues != null ?
                new ContentValues(initialValues) : new ContentValues());

        if (!values.containsKey(Readings.COLUMN_DATE)) {
            values.put(Readings.COLUMN_DATE, System.currentTimeMillis());
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(table, null, values);

        if (rowId != -1) {
            Uri matchUri = ContentUris.withAppendedId(uri, rowId);
            Context context = getContext();
            assert context != null;
            context.getContentResolver().notifyChange(matchUri, null);
            return matchUri;
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String table;
        String where;

        switch (sUriMatcher.match(uri)) {
            case URI_READINGS:
                table = Readings.TABLE_NAME;
                where = selection;
                break;
            case URI_READING_ID:
                table = Readings.TABLE_NAME;
                where = Readings._ID + "=" + uri.getLastPathSegment();
                if (selection != null) where += " AND " + selection;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(table, where, selectionArgs);

        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String table;
        String where;

        switch (sUriMatcher.match(uri)) {
            case URI_READINGS:
                table = Readings.TABLE_NAME;
                where = selection;
                break;
            case URI_READING_ID:
                table = Readings.TABLE_NAME;
                where = Readings._ID + "=" + uri.getLastPathSegment();
                if (selection != null) where += " AND " + selection;
                break;
            default:
                throw new IllegalArgumentException("Uknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(table, values, where, selectionArgs);

        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

}
