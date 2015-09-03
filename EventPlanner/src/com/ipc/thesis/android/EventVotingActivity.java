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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class EventVotingActivity extends Activity {

	private static final String TAG = "EventVotingActivity";

	private IEventPlannerService mEventPlannerService = null;

	private static final String MSG_HEADER = "EPSMS";
	private static final String FIELD_SEPARATOR = "#";
	private static final String DATA_SEPARATOR = ";";

	private static final String[] PROJECTION = new String[] {

		// THIS is just projection , field indexes. not indexes from table

		EventDataColumns._ID, // 0
		EventDataColumns.EVENT_TYPE, // 1
		EventDataColumns.TIMESTAMP, // 2

		EventDataColumns.EVENT_NAME, // 3
		EventDataColumns.PLACE1, // 4
		EventDataColumns.PLACE2, // 5
		EventDataColumns.DATE1, // 6
		EventDataColumns.DATE2, // 7
		EventDataColumns.CONTACT_NAMES, // 8

		EventDataColumns.EVENT_FROM, // 9
		
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
	private CheckBox chkPlace1;
	private CheckBox chkPlace2;
	private CheckBox chkDateTime1;
	private CheckBox chkDateTime2;

	private TextView mTxtParticipants;
	
	private Button mBtnSendReply;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "onCreate()");
		
		setContentView(R.layout.event_voting);

		mTxtViewHeading = (TextView) findViewById(R.id.view_heading);
		mTxtViewHeading.setText("Answer the event request");
		
		mTxtEventName = (TextView) findViewById(R.id.event_name);
		chkEventAnswer = (CheckBox) findViewById(R.id.attending_check);

		chkPlace1 = (CheckBox) findViewById(R.id.place1_check);
		chkPlace2 = (CheckBox) findViewById(R.id.place2_check);
		chkDateTime1 = (CheckBox) findViewById(R.id.datetime1_check);
		chkDateTime2 = (CheckBox) findViewById(R.id.datetime2_check);

		mTxtParticipants = (TextView) findViewById(R.id.voting_participant_list);
		
		mBtnSendReply = (Button) findViewById(R.id.btnSendReply);
		mBtnSendReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick() mBtnSendReply");
				sendEventReply();
			}
		});


		Log.v(TAG, "getExtras(): ");
		
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

		Log.v(TAG, "managedQuery(): ");
		
		// Get the event
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);

		bind();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");

		unbind(); //Important

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume()");

		super.onResume();
		setTitle("Event details: Voting view");

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
			// Answer has been sent already
			// disable send reply button
			mBtnSendReply.setEnabled(false);
			mTxtViewHeading.setText("You Answered Already");
		}
		else {
			mTxtViewHeading.setText("Answer the event request");
		}

		
		String eventName = mCursor.getString(3); // event name
		mTxtEventName.setText(eventName);

		chkPlace1.setText(mCursor.getString(4));
		chkPlace2.setText(mCursor.getString(5));

		chkDateTime1.setText(mCursor.getString(6));
		chkDateTime2.setText(mCursor.getString(7));

		String contactsNames = mCursor.getString(8); // contact names
		//Toast.makeText(getApplicationContext(), contactsNames, Toast.LENGTH_SHORT).show();


		if(mCursor.getString(10).length() != 0)
			chkEventAnswer.setChecked(true);

		if(mCursor.getInt(11) == 1)
			chkPlace1.setChecked(true);
		if(mCursor.getInt(12) == 1)
			chkPlace2.setChecked(true);
		if(mCursor.getInt(13) == 1)
			chkDateTime1.setChecked(true);
		if(mCursor.getInt(14) == 1)
			chkDateTime2.setChecked(true);

		String participantList = "";
		Pattern p = Pattern.compile(";");
		String names[] = p.split(mCursor.getString(8)); // contact names
		
		for (int i = 0; i < names.length; i++) {
			participantList += names[i] + "\n";
		}
		
		mTxtParticipants.setText(participantList);
		
	}


	private void sendEventReply() {
		Log.v(TAG, "sendEventReply()");

		mCursor.requery();
		mCursor.moveToFirst();

		String eventName = mCursor.getString(3); // event name
		String timeStamp = mCursor.getString(2); // timestamp
		String answer = "";
		String placeChoice = "";
		String timeChoice = "";

		answer = getAnswer();

		if (chkPlace1.isChecked()) {
			placeChoice = "1";
		}
		if (chkPlace2.isChecked()) {
			if(placeChoice.length() == 0) {
				placeChoice = "2";
			}
			else {
				placeChoice += DATA_SEPARATOR + "2";				
			}
		}

		if (chkDateTime1.isChecked()) {
			timeChoice = "1";
		}
		if (chkDateTime2.isChecked()) {
			if(timeChoice.length() == 0) {
				timeChoice = "2";
			}
			else {
				timeChoice += DATA_SEPARATOR + "2";
			}
		}


		String eventReplySMS = MSG_HEADER + 
		FIELD_SEPARATOR + EventData.MSG_TYPE_ANSWER + 
		FIELD_SEPARATOR + timeStamp + 
		FIELD_SEPARATOR + eventName +
		FIELD_SEPARATOR + answer +
		FIELD_SEPARATOR + placeChoice +
		FIELD_SEPARATOR + timeChoice;

		String phoneNumber = mCursor.getString(9);

		Log.v(TAG, "sendEventReply()" + eventReplySMS);
		Log.v(TAG, "phoneNumber: " + phoneNumber);

		Toast.makeText(getApplicationContext(), "Event Reply: " + eventReplySMS, Toast.LENGTH_SHORT).show();


		// IPC call - IMPORTANT
		try {
			mEventPlannerService.sendEventReply(phoneNumber, eventReplySMS);
		}
		catch (RemoteException ee) {
			Log.e(TAG, ee.getMessage(), ee);
		}

		updateVotingInDatabase();

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
	
	private int checkboxValue(CheckBox cb) {
		Log.v(TAG, "checkboxValue()");
		if (cb.isChecked())
			return 1;
		else
			return 0;
	}

	private void updateVotingInDatabase() {
		Log.v(TAG, "updateVotingInDatabase()");
		
		ContentValues values = new ContentValues();
		
		values.put(EventDataColumns.EVENT_ANSWER, getAnswer());
		values.put(EventDataColumns.PLACE1_VOTES, checkboxValue(chkPlace1));
		values.put(EventDataColumns.PLACE2_VOTES, checkboxValue(chkPlace2));
		values.put(EventDataColumns.TIME1_VOTES, checkboxValue(chkDateTime1));
		values.put(EventDataColumns.TIME2_VOTES, checkboxValue(chkDateTime2));

        // Commit all of our changes to persistent storage. When the update completes
        // the content provider will notify the cursor of the change, which will
        // cause the UI to be updated.
        try {
            getContentResolver().update(mUri, values, null, null);
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
