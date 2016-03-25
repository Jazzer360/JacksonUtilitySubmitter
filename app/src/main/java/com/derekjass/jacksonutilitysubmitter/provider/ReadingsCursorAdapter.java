package com.derekjass.jacksonutilitysubmitter.provider;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.derekjass.jacksonutilitysubmitter.R;
import com.derekjass.jacksonutilitysubmitter.provider.ReadingsContract.Readings;

import java.text.DateFormat;
import java.util.Date;

public class ReadingsCursorAdapter extends CursorAdapter {

    static DateFormat sSdf = DateFormat.getDateInstance();

    private LayoutInflater mInflater;

    private final int mDateCol;
    private final int mElectricCol;
    private final int mWaterCol;
    private final int mGasCol;

    public ReadingsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mInflater = LayoutInflater.from(context);

        mDateCol = c.getColumnIndexOrThrow(Readings.COLUMN_DATE);
        mElectricCol = c.getColumnIndexOrThrow(Readings.COLUMN_ELECTRIC);
        mWaterCol = c.getColumnIndexOrThrow(Readings.COLUMN_WATER);
        mGasCol = c.getColumnIndexOrThrow(Readings.COLUMN_GAS);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView = mInflater.inflate(
                R.layout.listitem_readings, parent, false);

        ViewHolder views = new ViewHolder();
        views.dateView = (TextView) newView.findViewById(R.id.dateView);
        views.electricViews = newView.findViewById(R.id.electricViews);
        views.waterViews = newView.findViewById(R.id.waterViews);
        views.gasViews = newView.findViewById(R.id.gasViews);
        views.electricText =
                (TextView) newView.findViewById(R.id.electricValue);
        views.waterText =
                (TextView) newView.findViewById(R.id.waterValue);
        views.gasText =
                (TextView) newView.findViewById(R.id.gasValue);

        newView.setTag(views);
        return newView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder views = (ViewHolder) view.getTag();

        Date date = new Date(cursor.getLong(mDateCol));

        int electricVal = cursor.getInt(mElectricCol);
        int waterVal = cursor.getInt(mWaterCol);
        int gasVal = cursor.getInt(mGasCol);

        views.dateView.setText(sSdf.format(date));
        if (electricVal >= 0) {
            views.electricText.setText(String.valueOf(electricVal));
            views.electricViews.setVisibility(View.VISIBLE);
        } else {
            views.electricViews.setVisibility(View.GONE);
        }
        if (waterVal >= 0) {
            views.waterText.setText(String.valueOf(waterVal));
            views.waterViews.setVisibility(View.VISIBLE);
        } else {
            views.waterViews.setVisibility(View.GONE);
        }
        if (gasVal >= 0) {
            views.gasText.setText(String.valueOf(gasVal));
            views.gasViews.setVisibility(View.VISIBLE);
        } else {
            views.gasViews.setVisibility(View.GONE);
        }
    }

    static class ViewHolder {
        TextView dateView;
        View electricViews;
        View waterViews;
        View gasViews;
        TextView electricText;
        TextView waterText;
        TextView gasText;
    }
}
