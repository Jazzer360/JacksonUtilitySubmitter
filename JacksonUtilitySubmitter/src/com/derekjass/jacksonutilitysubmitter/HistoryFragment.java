package com.derekjass.jacksonutilitysubmitter;

import static com.derekjass.jacksonutilitysubmitter.SubmitFragment.getIntFromEditText;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.EditText;

import com.derekjass.jacksonutilitysubmitter.provider.ReadingsContract.Readings;
import com.derekjass.jacksonutilitysubmitter.provider.ReadingsCursorAdapter;

public class HistoryFragment extends ListFragment
implements LoaderCallbacks<Cursor> {

	public static class EditHistoryDialog extends DialogFragment {

		public static final String URI_KEY = "uri";

		private DatePicker datePicker;

		private EditText electricText;
		private EditText waterText;
		private EditText gasText;

		private View gasViews;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle args = getArguments();
			final Uri uri = (Uri) (args != null
					? args.getParcelable(URI_KEY) : null);
			final boolean editing = uri == null ? false : true;

			LayoutInflater inflater = getActivity().getLayoutInflater();

			View v = inflater.inflate(R.layout.dialog_edit_history,
					(ViewGroup) getView(), false);

			datePicker = (DatePicker) v.findViewById(R.id.datePicker);
			electricText = (EditText) v.findViewById(R.id.electricReading);
			waterText = (EditText) v.findViewById(R.id.waterReading);
			gasText = (EditText) v.findViewById(R.id.gasReading);
			gasViews = (View) v.findViewById(R.id.gasFields);

			if (editing) setupViews(uri);

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			boolean gasEnabled = prefs
					.getBoolean(getString(R.string.pref_enable_gas), false);
			if (gasEnabled || !TextUtils.isEmpty(gasText.getText())) {
				gasViews.setVisibility(View.VISIBLE);
			} else {
				gasViews.setVisibility(View.GONE);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(
					getActivity());
			builder.setTitle(editing
					? R.string.edit_history : R.string.add_history);
			builder.setView(v);
			builder.setNegativeButton(android.R.string.cancel,
					new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
			if (editing) {
				builder.setNeutralButton(R.string.delete,
						new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getActivity().getContentResolver().delete(uri, null, null);
				}
			});
			}
			builder.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ContentValues cv = new ContentValues();
					Calendar c = Calendar.getInstance();
					c.clear();
					c.set(datePicker.getYear(), datePicker.getMonth(),
							datePicker.getDayOfMonth());
					cv.put(Readings.COLUMN_DATE, c.getTimeInMillis());
					cv.put(Readings.COLUMN_ELECTRIC,
							getIntFromEditText(electricText));
					cv.put(Readings.COLUMN_WATER,
							getIntFromEditText(waterText));
					cv.put(Readings.COLUMN_GAS,
							getIntFromEditText(gasText));
					if (editing) {
						getActivity().getContentResolver().update(
								uri, cv, null, null);
					} else {
						getActivity().getContentResolver().insert(uri, cv);
					}
				}
			});

			return builder.create();
		}

		private void setupViews(Uri uri) {
			Cursor c = getActivity().getContentResolver()
					.query(uri, null, null, null, null);
			int dateCol = c.getColumnIndexOrThrow(Readings.COLUMN_DATE);
			int electricCol = c.getColumnIndexOrThrow(Readings.COLUMN_ELECTRIC);
			int waterCol = c.getColumnIndexOrThrow(Readings.COLUMN_WATER);
			int gasCol = c.getColumnIndexOrThrow(Readings.COLUMN_GAS);

			c.moveToFirst();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(c.getLong(dateCol));
			int electric = c.getInt(electricCol);
			int water = c.getInt(waterCol);
			int gas = c.getInt(gasCol);
			c.close();

			datePicker.updateDate(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			if (electric >= 0) {
				electricText.setText(String.valueOf(electric));
			}
			if (water >= 0) {
				waterText.setText(String.valueOf(water));
			}
			if (gas >= 0) {
				gasText.setText(String.valueOf(gas));
			}
		}
	}

	private CursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_history_menu, menu);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this).forceLoad();

		setEmptyText(getString(R.string.no_data));

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showEditHistoryDialog(Uri.withAppendedPath(
						Readings.CONTENT_URI, String.valueOf(id)));
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			showNewHistoryDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void showEditHistoryDialog(Uri uri) {
		DialogFragment dialog = new EditHistoryDialog();
		Bundle args = new Bundle();
		args.putParcelable(EditHistoryDialog.URI_KEY, uri);
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "EditHistoryDialog");
	}

	private void showNewHistoryDialog() {
		new EditHistoryDialog().show(getFragmentManager(), "NewHistoryDialog");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(),
				Readings.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (mAdapter == null) {
			mAdapter = new ReadingsCursorAdapter(getActivity(), data, 0);
			setListAdapter(mAdapter);
		} else {
			mAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}
