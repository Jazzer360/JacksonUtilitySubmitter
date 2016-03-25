package com.derekjass.jacksonutilitysubmitter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ReadingsContract {
    private ReadingsContract() {
    }

    static final String AUTHORITY =
            "com.derekjass.jacksonutilitysubmitter.provider";

    public interface ReadingsColumns {
        String COLUMN_DATE = "date";
        String COLUMN_ELECTRIC = "electric";
        String COLUMN_WATER = "water";
        String COLUMN_GAS = "gas";
    }

    public static final class Readings implements BaseColumns, ReadingsColumns {

        private Readings() {
        }

        static final String TABLE_NAME = "readings";
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.com.derekjass.provider." +
                        TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.com.derekjass.provider." +
                        TABLE_NAME;
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + TABLE_NAME);
    }
}
