package com.ipc.thesis.android;

import java.util.regex.Pattern;

import com.ipc.thesis.android.EventData.EventDataColumns;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmedEventDetailsActivity extends Activity {

	private static final String TAG = "ConfirmedEventDetailsActivity";
	
	private static final String[] PROJECTION = new String[] {
		EventDataColumns._ID, // 0
		EventDataColumns.EVENT_TYPE, // 1
		EventDataColumns.TIMESTAMP, // 2
		
		EventDataColumns.EVENT_NAME, // 3
		EventDataColumns.PLACE1, // 4
		EventDataColumns.PLACE2, // 5
		EventDataColumns.DATE1, // 6
		EventDataColumns.DATE2, // 7

		EventDataColumns.CONTACT_NAMES, // 8
		EventDataColumns.PHONE_NUMBERS, // 9

		EventDataColumns.EVENT_ANSWER, // 10
		EventDataColumns.PLACE1_VOTES, // 11
		EventDataColumns.PLACE2_VOTES, // 12
		EventDataColumns.TIME1_VOTES, // 13
		EventDataColumns.TIME2_VOTES, // 14

	};
	
	private Uri mUri;
	private Cursor mCursor;
	
	private TextView mTxtEventName;
	private TextView mTxtResult;
	private TextView mTxtPlaceChoice;
	private TextView mTxtTimeChoice;
	private TextView mTxtParticipants;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confirmed_event_details);
		
		mTxtEventName = (TextView) findViewById(R.id.event_name);
		mTxtResult = (TextView) findViewById(R.id.event_result);
		mTxtPlaceChoice = (TextView) findViewById(R.id.event_place);
		mTxtTimeChoice = (TextView) findViewById(R.id.event_time);

		mTxtParticipants = (TextView) findViewById(R.id.confirmed_participant_list);
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			String value = extras.getString("uri");
			mUri = Uri.parse(value);
			Toast.makeText(getApplicationContext(), mUri.toString(), Toast.LENGTH_SHORT).show();
		}

		// Get the event
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume()");
		super.onResume();
		setTitle("Event Planner: Event Confirmation");

		updateView();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
	}
	
	private void updateView() {
		Log.v(TAG, "updateView()");

		if (mCursor == null) {
			Log.w(TAG, "mCursor is NULL");
			return;
		}

		// Requery in case something changed while paused
		mCursor.requery();
		// Make sure we are at the one and only row in the cursor.
		mCursor.moveToFirst();

	
		String eventName = mCursor.getString(3); // event name
		mTxtEventName.setText(eventName);
		
		String participantList = "";
		Pattern p = Pattern.compile(";");
		String names[] = p.split(mCursor.getString(8)); // contact names
		for (int i = 0; i < names.length; i++) {
			participantList += names[i] + "\n";
		}
		
		// update participant list
		mTxtParticipants.setText(participantList);
		
		// update result value
		if (mCursor.getString(10).equals("Y")) {
			mTxtResult.setText("Result: YES");
		}
		else {
			mTxtResult.setText("Result: NO");
			// NO need to update place and time
			return;
		}
		
		String place = "";
		if (mCursor.getInt(11) == 1) {
			place = mCursor.getString(4); // PLACE1
		}
		else if (mCursor.getInt(12) == 1) {
			place = mCursor.getString(5); // PLACE2
		}

		String time = "";
		if (mCursor.getInt(13) == 1) {
			time = mCursor.getString(6); // TIME1
		}
		else if (mCursor.getInt(14) == 1) {
			time = mCursor.getString(7); // TIME2
		}
		
		mTxtPlaceChoice.setText("Place: " + place);
		
		mTxtTimeChoice.setText("Time: " + time);

	}
	
	
	
}
