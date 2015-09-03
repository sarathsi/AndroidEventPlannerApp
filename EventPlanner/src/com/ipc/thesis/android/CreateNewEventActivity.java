package com.ipc.thesis.android;

import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.ipc.thesis.android.R;

public class CreateNewEventActivity extends Activity 
implements View.OnClickListener, View.OnFocusChangeListener {
	private static final String TAG = "CreateNewEventActivity";

	private static final String MSG_HEADER = "EPSMS";
	private static final String MSG_TYPE_NEWEVENT = "N";
	//	private static final String MSG_TYPE_ANSWER = "A";
	//	private static final String MSG_TYPE_RESULT = "R";
	//	private static final String VOTING_VALUE_SEPARATOR = "-";

	private static final String FIELD_SEPARATOR = "#";
	private static final String DATA_SEPARATOR = ";"; // ","


	static final int DATE1_DIALOG_ID = 0;
	static final int TIME1_DIALOG_ID = 1;
	static final int DATE2_DIALOG_ID = 2;
	static final int TIME2_DIALOG_ID = 3;
	static final int EMPTY_EVENT_NAME_DIALOG_ID = 4;
	static final int EMPTY_PLACE_NAME_DIALOG_ID = 5;
	static final int EMPTY_DATE_TIME_DIALOG_ID = 6;

	private int current_dialog_id;

	private EditText mEtxtEventName;
	private EditText mEtxtPlace1;
	private EditText mEtxtPlace2;

	private EditText mEtxtDate1;
	private EditText mEtxtTime1;
	private EditText mEtxtDate2;
	private EditText mEtxtTime2;
	private Button mBtnSelectParticipants;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_newevent);

		//Get pointers to the widgets defined in layout file (create_newevent.xml)
		mEtxtEventName = (EditText) findViewById(R.id.eTxtEventName);
		mEtxtPlace1 = (EditText) findViewById(R.id.eTxtPlace1);
		mEtxtPlace2 = (EditText) findViewById(R.id.eTxtPlace2);

		mEtxtDate1 = (EditText) findViewById(R.id.eTxtDate1);
		mEtxtDate1.setOnFocusChangeListener(this);

		mEtxtTime1 = (EditText) findViewById(R.id.eTxtTime1);
		mEtxtTime1.setOnFocusChangeListener(this);

		mEtxtDate2 = (EditText) findViewById(R.id.eTxtDate2);
		mEtxtDate2.setOnFocusChangeListener(this);

		mEtxtTime2 = (EditText) findViewById(R.id.eTxtTime2);
		mEtxtTime2.setOnFocusChangeListener(this);

		// set button on-click listener
		mBtnSelectParticipants = (Button) findViewById(R.id.btnSelectParticipants);
		mBtnSelectParticipants.setOnClickListener(this);

		// to cancel the soft (virtual) keyboard when EditText is clicked
		// can use also android:focusable="false"
		mEtxtDate1.setInputType(0);
		mEtxtTime1.setInputType(0);
		mEtxtDate2.setInputType(0);
		mEtxtTime2.setInputType(0);

		// Switch to non-editable (with soft keyboard)
		mEtxtDate1.setKeyListener(null);
		mEtxtDate2.setKeyListener(null);
		mEtxtTime1.setKeyListener(null);
		mEtxtTime2.setKeyListener(null);

	} // end of onCreate()

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// if lost focus, do nothing for now.
		if (!hasFocus) {
			return;
		}

		switch ( v.getId()) {

		case R.id.eTxtDate1:
			showDialog(DATE1_DIALOG_ID);
			break;
		case R.id.eTxtDate2:
			showDialog(DATE2_DIALOG_ID);
			break;

		case R.id.eTxtTime1:
			showDialog(TIME1_DIALOG_ID);
			break;
		case R.id.eTxtTime2:
			showDialog(TIME2_DIALOG_ID);
			break;

		default:
		}
	}

	public void onClick(View v) {
		switch ( v.getId()) {
		case R.id.btnSelectParticipants:
			selectParticipants();
			break;

		default:
			Toast.makeText(getApplicationContext(), "Not implemented!", Toast.LENGTH_SHORT).show();
		}
	}


	private void selectParticipants() {
		Log.v(TAG, "selectParticipants");

		if (!validateInput())
			return;

		try {

			String sms = constructNewEventSMS();
			// start SelectContactsActivity
			Intent intent = new Intent(this, com.ipc.thesis.android.SelectContactsActivity.class);

			// send new event data
			intent.putExtra("sms", sms);

			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String constructNewEventSMS() {
		Log.v(TAG, "constructNewEventSMS");
		String eventName = mEtxtEventName.getText().toString();
		String place1 = mEtxtPlace1.getText().toString();
		String place2 = mEtxtPlace2.getText().toString();

		String date1 = mEtxtDate1.getText().toString();
		String date2 = mEtxtDate2.getText().toString();
		String time1 = mEtxtTime1.getText().toString();
		String time2 = mEtxtTime2.getText().toString();

		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		String timeStamp = new StringBuilder().append(day).append(month).append(year)
		.append(hour).append(min).append(sec).toString();

		String sms = MSG_HEADER + 
		FIELD_SEPARATOR + MSG_TYPE_NEWEVENT + 
		FIELD_SEPARATOR + timeStamp + 
		FIELD_SEPARATOR + eventName + 
		FIELD_SEPARATOR + place1 + FIELD_SEPARATOR + place2 + 	// TODO - check DATA_SEPARATOR
		FIELD_SEPARATOR + date1 + " " + time1 + FIELD_SEPARATOR + date2 + " " + time2;

		Log.v(TAG, "NewEventSMS: " + sms);
		//Toast.makeText(getApplicationContext(), sms, Toast.LENGTH_SHORT).show();
		return sms;
	}

	// validates all input fields
	private boolean validateInput() {
		Log.v(TAG, "validateInput");
		// TODO - replace with isEmpty() later
		if (mEtxtEventName.getText().toString().length() == 0) {
			//Toast.makeText(getApplicationContext(), "Event name is empty!", Toast.LENGTH_SHORT).show();
			showDialog(EMPTY_EVENT_NAME_DIALOG_ID);
			return false;
		}
		if (mEtxtPlace1.getText().toString().length() == 0 
				&& mEtxtPlace2.getText().toString().length() == 0) {
			//Toast.makeText(getApplicationContext(), "Enter at least one place", Toast.LENGTH_SHORT).show();
			showDialog(EMPTY_PLACE_NAME_DIALOG_ID);
			return false;
		}
		if ( (mEtxtTime1.getText().toString().length() == 0 ||
				mEtxtDate1.getText().toString().length() == 0) 
				&& (mEtxtTime2.getText().toString().length() == 0 ||
						mEtxtDate2.getText().toString().length() == 0) ) {
			//Toast.makeText(getApplicationContext(), "Enter at least one date & time!", Toast.LENGTH_SHORT).show();
			showDialog(EMPTY_DATE_TIME_DIALOG_ID);
			return false;
		}

		return true;
	}


	/******** Part of code taken from SDK API Demos example *************/
	@Override
	protected Dialog onCreateDialog(int id) {
		//Toast.makeText(getApplicationContext(), "onCreateDialog", Toast.LENGTH_SHORT).show();
		switch (id) {
		case DATE1_DIALOG_ID:
		case DATE2_DIALOG_ID:
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this,
					mDateSetListener,
					year, month, day);

		case TIME1_DIALOG_ID:
		case TIME2_DIALOG_ID:
			return new TimePickerDialog(this,
					mTimeSetListener, 1, 1, false);
		}

		String dialogTitle;
		String dialogMessage;
		if (id == EMPTY_EVENT_NAME_DIALOG_ID) {
			dialogTitle = "Empty Event Name";
			dialogMessage = "Enter event name!";
		}
		else if (id == EMPTY_PLACE_NAME_DIALOG_ID) {
			dialogTitle = "Empty Place";
			dialogMessage = "Enter atleast one place option!";
		}
		else if (id == EMPTY_DATE_TIME_DIALOG_ID) {
			dialogTitle = "Empty Date&Time";
			dialogMessage = "Enter atleast one date & time option!";
		}
		else {
			return null;
		}

		return new AlertDialog.Builder(CreateNewEventActivity.this)
        .setIcon(R.drawable.alert_dialog_icon)
        .setTitle(dialogTitle)
        .setMessage(dialogMessage)
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked ok so do some stuff */
            	// NOTHING TODO
            }
        })
        .create();
		
	}


	/*
	protected Dialog onCreateDialog(int id) {
		Dialog result = null;

		if(0 == id){
			AlertDialog.Builder builder = new Builder(this);

			builder.setMessage(R.string.alert_1);

			android.content.DialogInterface.OnClickListener noListener = new android.content.DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			};

			builder.setNegativeButton(R.string.ok, noListener);

			result = builder.create();
		}
	};
	 */


	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		//Toast.makeText(getApplicationContext(), "onPrepareDialog", Toast.LENGTH_SHORT).show();

		final Calendar c = Calendar.getInstance();
		current_dialog_id = id;

		switch (id) {
		case TIME1_DIALOG_ID:
		case TIME2_DIALOG_ID:
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			((TimePickerDialog) dialog).updateTime(hour, minute);
			break;

		case DATE1_DIALOG_ID:
		case DATE2_DIALOG_ID:
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			((DatePickerDialog) dialog).updateDate(year, month, day);
			break;
		}
	}    

	private DatePickerDialog.OnDateSetListener mDateSetListener =
		new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			if (current_dialog_id == DATE1_DIALOG_ID) {
				mEtxtDate1.setText(updateDate(year, monthOfYear, dayOfMonth));	
			}
			else if (current_dialog_id == DATE2_DIALOG_ID) {
				mEtxtDate2.setText(updateDate(year, monthOfYear, dayOfMonth));	
			}
			current_dialog_id = -1; // invalid id
		}
	};

	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
		new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (current_dialog_id == TIME1_DIALOG_ID) {
				mEtxtTime1.setText(updateTime(hourOfDay, minute));	
			}
			else if (current_dialog_id == TIME2_DIALOG_ID) {
				mEtxtTime2.setText(updateTime(hourOfDay, minute));	
			}
			current_dialog_id = -1; // invalid id
		}
	};

	private StringBuilder updateDate(int year, int monthOfYear, int dayOfMonth) {
		StringBuilder dateText = new StringBuilder()
		.append(dayOfMonth).append("-")
		.append(monthOfYear + 1).append("-") // Month is 0 based so add 1
		.append(year);

		return dateText;
	}

	private StringBuilder updateTime(int hourOfDay, int minute) {
		StringBuilder timeText = new StringBuilder()
		.append(pad(hourOfDay)).append(":")
		.append(pad(minute));

		return timeText;
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}


	/************* TESTing **********************/
	private void sendInvitationTEST() {
		if (!validateInput())
			return;

		Toast.makeText(getApplicationContext(), constructNewEventSMS(), Toast.LENGTH_SHORT).show();
		//sendSMS("0451225673", constructNewEventSMS());
		//sendViaDefaultSMS("0451225673");
	}

	private void sendSMSTEST(String number, String text) {
		SmsManager sm = SmsManager.getDefault();
		sm.sendTextMessage(number, null, text, null, null);
	}

	private void sendViaDefaultSMSTEST(String number) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
	}


	/************ lifecycle callbacks *****************/
	@Override
	protected void onStart() {
		super.onStart();
		// The activity is about to become visible.
	}
	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").
	}
	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be "paused").
	}
	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
	}



	/********* KeyEvent testing *************/
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			Toast.makeText(getBaseContext(), "Center was clicked",
					Toast.LENGTH_LONG).show();
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			Toast.makeText(getBaseContext(), "Left arrow was clicked",
					Toast.LENGTH_LONG).show();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			Toast.makeText(getBaseContext(), "Right arrow was clicked",
					Toast.LENGTH_LONG).show();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			Toast.makeText(getBaseContext(), "Up arrow was clicked",
					Toast.LENGTH_LONG).show();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			Toast.makeText(getBaseContext(), "Down arrow was clicked",
					Toast.LENGTH_LONG).show();
			break;
		}
		return false;
	}
	 */


} // end of activity