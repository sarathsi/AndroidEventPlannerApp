package com.ipc.thesis.android;

import com.ipc.thesis.android.EventData.EventDataColumns;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class ClosedEventsListActivity extends ListActivity {

	private static final String TAG = "ClosedEventsListActivity";

	/**
	 * The columns we are interested in from the database
	 */
	private static final String[] PROJECTION = new String[] {
		EventDataColumns._ID, // 0
		EventDataColumns.EVENT_NAME, // 1
	};

	/** The index of the title column */
	private static final int COLUMN_INDEX_EVENT_NAME = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");

		super.onDestroy();
	}
	
}
