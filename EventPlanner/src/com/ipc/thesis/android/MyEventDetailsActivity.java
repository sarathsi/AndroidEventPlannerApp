package com.ipc.thesis.android;

import java.util.regex.Pattern;

import com.ipc.eventplannerservice.IEventPlannerService;
import com.ipc.thesis.android.EventData.EventDataColumns;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MyEventDetailsActivity extends Activity {

	private static final String TAG = "MyEventDetailsActivity";

	private static final String MSG_HEADER = "EPSMS";
	private static final String FIELD_SEPARATOR = "#";
	private static final String DATA_SEPARATOR = ";";
	
	private IEventPlannerService mEventPlannerService = null;

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

	private TextView mTxtViewHeading;
	private TextView mTxtEventName;
	private CheckBox chkEventAnswer;
	private RadioButton rbPlace1;
	private RadioButton rbPlace2;
	private RadioButton rbDateTime1;
	private RadioButton rbDateTime2;
	
	private TextView mTxtParticipants;
	
	private Button mBtnSendConfirmation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_decision);

		mTxtViewHeading = (TextView) findViewById(R.id.decision_view_heading);
		mTxtViewHeading.setText("Make event decision");
		
		mTxtEventName = (TextView) findViewById(R.id.event_name);
		chkEventAnswer = (CheckBox) findViewById(R.id.confirmation_check);
		
		rbPlace1 = (RadioButton) findViewById(R.id.place1_radio);
		rbPlace2 = (RadioButton) findViewById(R.id.place2_radio);
		rbDateTime1 = (RadioButton) findViewById(R.id.datetime1_radio);
		rbDateTime2 = (RadioButton) findViewById(R.id.datetime2_radio);
		
		mTxtParticipants = (TextView) findViewById(R.id.decision_participant_list);
		
		mBtnSendConfirmation = (Button) findViewById(R.id.btnSendConfirmation);
		mBtnSendConfirmation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick() mBtnSendConfirmation");
				sendEventConfirmation();
			}
		});
		
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			String value = extras.getString("uri");
			mUri = Uri.parse(value);
			//Toast.makeText(getApplicationContext(), mUri.toString(), Toast.LENGTH_SHORT).show();
		}
		/*
		final Intent intent = getIntent();
		mUri = intent.getData();
		Toast.makeText(getApplicationContext(), mUri.toString(), Toast.LENGTH_SHORT).show();
		 */

		// Get the event
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);

		bind();
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume()");
		super.onResume();
		setTitle("Event details: Decision view");

		updateView();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");
		unbind(); //Important
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

		String answer = mCursor.getString(10); //get Answer value
		if (answer.length() != 0) {
			// Confirmation has been sent already - disable "send confirmation" button
			mBtnSendConfirmation.setEnabled(false);
			mTxtViewHeading.setText("Confirmation sent already");
		}
		else {
			mTxtViewHeading.setText("Make event decision");
		}
		
		
		String eventName = mCursor.getString(3); // event name
		mTxtEventName.setText(eventName);

		int p1Votes = mCursor.getInt(11);
		int p2Votes = mCursor.getInt(12);
		int t1Votes = mCursor.getInt(13);
		int t2Votes = mCursor.getInt(14);

		rbPlace1.setText(mCursor.getString(4) + " (" + p1Votes + ")");
		rbPlace2.setText(mCursor.getString(5) + " (" + p2Votes + ")");

		rbDateTime1.setText(mCursor.getString(6) + " (" + t1Votes + ")");
		rbDateTime2.setText(mCursor.getString(7) + " (" + t2Votes + ")");

		String participantList = "";
		Pattern p = Pattern.compile(";");
		String names[] = p.split(mCursor.getString(8)); // contact names
		
		for (int i = 0; i < names.length; i++) {
			participantList += names[i] + "\n";
		}
		
		mTxtParticipants.setText(participantList);
	}


	private void sendEventConfirmation() {
		Log.v(TAG, "sendEventConfirmation()");

		mCursor.requery();
		mCursor.moveToFirst();

		String eventName = mCursor.getString(3); // event name
		String timeStamp = mCursor.getString(2); // timestamp
		String answer = getAnswer();
		String placeChoice = "";
		String timeChoice = "";
		
		// Selected Place
		if (rbPlace1.isChecked()) {
			placeChoice = "1";
		}
		else if (rbPlace2.isChecked()) {
			placeChoice = "2";
		}

		// Selected Time
		if (rbDateTime1.isChecked()) {
			timeChoice = "1";
		}
		else if (rbDateTime2.isChecked()) {
			timeChoice = "2";
		}
		
		String phoneNumberList = mCursor.getString(9); // number1;number2;number3
		Log.v(TAG, "phoneNumberList: " + phoneNumberList);
		
		String eventConfirmationSMS = MSG_HEADER + 
		FIELD_SEPARATOR + EventData.MSG_TYPE_RESULT + 
		FIELD_SEPARATOR + timeStamp + 
		FIELD_SEPARATOR + eventName +
		FIELD_SEPARATOR + answer +
		FIELD_SEPARATOR + placeChoice +
		FIELD_SEPARATOR + timeChoice;

		Log.v(TAG, "sendEventConfirmation(): " + eventConfirmationSMS);
		Toast.makeText(getApplicationContext(), "Event Confirmation: " + eventConfirmationSMS, Toast.LENGTH_SHORT).show();

		// IPC call - IMPORTANT
		try {
			mEventPlannerService.sendEventConfirmation(phoneNumberList, eventConfirmationSMS);
			
		}
		catch (RemoteException ee) {
			Log.e(TAG, ee.getMessage(), ee);
		}

		updateConfirmationInDatabase();

		// finish the activity
		finish();
	}

	private String getAnswer() {
		Log.v(TAG, "getAnswer()");
		if (chkEventAnswer.isChecked()) {
			return "Y";
		}
		else {
			return "N";
		}
	}

	private int radioValue(RadioButton rb) {
		Log.v(TAG, "radioValue()");
		if (rb.isChecked())
			return 1;
		else
			return 0;
	}
	
	private void updateConfirmationInDatabase() {
		Log.v(TAG, "updateConfirmationInDatabase()");

		// Insert record in Confirmed events table
		
		mCursor.requery();
		mCursor.moveToFirst();
		
		ContentValues values = new ContentValues();

		values.put(EventDataColumns.EVENT_TYPE, "R"); // Result / Confirmation
		values.put(EventDataColumns.TIMESTAMP, mCursor.getString(2));
		values.put(EventDataColumns.EVENT_NAME, mCursor.getString(3));

		values.put(EventDataColumns.PLACE1, mCursor.getString(4));
		values.put(EventDataColumns.PLACE2, mCursor.getString(5));
		values.put(EventDataColumns.DATE1, mCursor.getString(6));
		values.put(EventDataColumns.DATE2, mCursor.getString(7));
		values.put(EventDataColumns.TIME1, mCursor.getString(6));
		values.put(EventDataColumns.TIME2, mCursor.getString(7));

		values.put(EventDataColumns.CONTACT_NAMES, mCursor.getString(8));
		values.put(EventDataColumns.PHONE_NUMBERS, mCursor.getString(9)); // friends phone numbers
		values.put(EventDataColumns.EVENT_FROM, "self"); // mCursor.getString()
		
		///////////////////////////////////////////////////
		
		values.put(EventDataColumns.EVENT_ANSWER, getAnswer());
		values.put(EventDataColumns.PLACE1_VOTES, radioValue(rbPlace1));
		values.put(EventDataColumns.PLACE2_VOTES, radioValue(rbPlace2));
		values.put(EventDataColumns.TIME1_VOTES, radioValue(rbDateTime1));
		values.put(EventDataColumns.TIME2_VOTES, radioValue(rbDateTime2));

		values.put(EventDataColumns.CREATED_DATE, System.currentTimeMillis());
		values.put(EventDataColumns.MODIFIED_DATE, System.currentTimeMillis());

		// Insert the record into "Confirmed Events" table
		try {
			Log.v(TAG, "getContentResolver().insert");
			Uri uri = getContentResolver().insert(EventDataColumns.CONTENT_URI_CONFIRMED_EVENTS, values);
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		}

		// Delete the record from "My Events" table
        try {
        	getContentResolver().delete(mUri, null, null);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
		
	}
	
	public void bind() {
		Log.v(TAG, "bind()");
		bindService(new Intent(IEventPlannerService.class
				.getName()),
				serviceConn, Context.BIND_AUTO_CREATE);
	}

	public void unbind() {
		Log.v(TAG, "unbind()");
		unbindService(serviceConn);
	}

	private ServiceConnection serviceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "onServiceConnected() called");
			mEventPlannerService = IEventPlannerService.Stub.asInterface(service);
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "onServiceDisconnected() called");
			mEventPlannerService = null;
		}
	};


}
