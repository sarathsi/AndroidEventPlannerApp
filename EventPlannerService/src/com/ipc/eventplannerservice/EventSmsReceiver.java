package com.ipc.eventplannerservice;

import java.util.regex.Pattern;

import com.ipc.eventplannerservice.EventData.EventDataColumns;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Toast;
import android.telephony.TelephonyManager;

public class EventSmsReceiver extends BroadcastReceiver {

	private static final String TAG = "EventSmsReceiver";
	private static final String FIELD_SEPARATOR = "#";
	private static final String DATA_SEPARATOR = ";";

	private static final int  EVENT_TYPE_INDEX = 1;
	private static final int  TIMESTAMP_INDEX = 2;
	private static final int  NAME_INDEX = 3;
	private static final int  P1_INDEX = 4;
	private static final int  P2_INDEX = 5;
	private static final int  T1_INDEX = 6;
	private static final int  T2_INDEX = 7;
	private static final int  CONTACT_NAMES_INDEX = 8;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive()");

		// get the SMS message passed in
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";

		if (bundle != null) {
			// retrieve SMS message received
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];

			for (int i=0; i<msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				str += "SMS from " + msgs[i].getOriginatingAddress();
				str += " :";
				str += msgs[i].getMessageBody().toString();
				str += "\n";

				processSMS(context,
						msgs[i].getMessageBody().toString(),
						msgs[i].getOriginatingAddress());

			}

			Log.v(TAG, str);
			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
		}
		else {
			Log.v(TAG, "SMS is NULL");
			Toast.makeText(context, "SMS is NULL", Toast.LENGTH_SHORT).show();
		}
	}


	private void processSMS(Context context, String message, String receivedFrom) {
		Log.v(TAG, "processSMS()");

		// get the values from SMS string
		Pattern p = Pattern.compile(FIELD_SEPARATOR);
		String[] items = p.split(message);

		for (int i = 0; i < items.length; i++) {
			Log.v(TAG, "processSMS() Items: " + items[i]);
		}

		if (!items[0].equals("EPSMS")) {
			Log.v(TAG, "processSMS() NOT EPSMS");
			return;
		}
		Toast.makeText(context, "Received EventPlanner SMS", Toast.LENGTH_SHORT).show();

		/*
		 * Example received NEW event SMS:
		 * EPSMS#N#291020111631#dinner#tut#home#29-11-2011 01:06#29-11-2011 01:06#Sarath Singapati;Harika Pacchabatla
		 */

		if (items[EVENT_TYPE_INDEX].equals("N")) {
			Log.v(TAG, "processSMS() NEW EVENT SMS");

			// event sms is received from a friend
			updateNewEvent(context, items, receivedFrom, EventDataColumns.CONTENT_URI_FRIENDS_EVENTS);			
		}
		else if (items[EVENT_TYPE_INDEX].equals("A")) {
			Log.v(TAG, "processSMS() EVENT ANSWER SMS");

			updateEventAnswer(context, items, receivedFrom);
		}

		else if (items[EVENT_TYPE_INDEX].equals("R")) {
			Log.v(TAG, "processSMS() EVENT RESULT SMS");
			Toast.makeText(context, "processSMS() EVENT RESULT SMS", Toast.LENGTH_SHORT).show();
			
			updateEventResult(context, items, receivedFrom);
		}
		else {
			Log.v(TAG, "processSMS() NOT new event SMS");
			
		}

	}

	/************ Updating event result : IN PROGRESS ****************/
	
	private void updateEventResult(Context context, String[] items, String receivedFrom) {
		for (int i = 0; i < items.length; i++) {
			Log.v(TAG, "updateEventResult() Items: " + items[i]);
		}
		
		String timestamp = items[TIMESTAMP_INDEX];

		mPlace1 = mPlace2 = mTime1 = mTime2 = mContactNames = "";
		// Important
		getConfirmationEventDetails(context, timestamp);

		Log.v(TAG, "updateEventResult() " + mPlace1 + " " + mPlace2 + " " + mTime1 + " " + mTime2 + " " + mContactNames);
		
		
		// Now, Insert the new record into "Confirmed events" table
		updateConfirmationInDatabase(context, items, receivedFrom);
		

		// Delete the record from "Friends Events" table
        try {
        	context.getContentResolver().delete(
        			EventDataColumns.CONTENT_URI_FRIENDS_EVENTS,
        			EventDataColumns.TIMESTAMP + "=" + timestamp,
        			null);
        	
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
        
	}
	
	private void updateConfirmationInDatabase(Context context, String[] items, String receivedFrom) {
		Log.v(TAG, "updateConfirmationInDatabase()");
		
		ContentValues values = new ContentValues();

		values.put(EventDataColumns.EVENT_TYPE, "R"); // Result / Confirmation
		values.put(EventDataColumns.TIMESTAMP, items[TIMESTAMP_INDEX]);
		values.put(EventDataColumns.EVENT_NAME, items[3]);

		values.put(EventDataColumns.PLACE1, mPlace1);
		values.put(EventDataColumns.PLACE2, mPlace2);
		values.put(EventDataColumns.DATE1, mTime1);
		values.put(EventDataColumns.DATE2, mTime2);
		values.put(EventDataColumns.TIME1, mTime1);
		values.put(EventDataColumns.TIME2, mTime2);

		values.put(EventDataColumns.CONTACT_NAMES, mContactNames); // TODO
		values.put(EventDataColumns.PHONE_NUMBERS, "");
		values.put(EventDataColumns.EVENT_FROM, receivedFrom);
		
		values.put(EventDataColumns.EVENT_ANSWER, items[4]);
		
		values.put(EventDataColumns.PLACE1_VOTES, 0);
		values.put(EventDataColumns.PLACE2_VOTES, 0);
		values.put(EventDataColumns.TIME1_VOTES, 0);
		values.put(EventDataColumns.TIME2_VOTES, 0);
		
		if (items[5].equals("1")) {
			values.put(EventDataColumns.PLACE1_VOTES, 1);
		}
		else if (items[5].equals("2")) {
			values.put(EventDataColumns.PLACE2_VOTES, 1);
		}
		
		if (items[6].equals("1")) {
			values.put(EventDataColumns.TIME1_VOTES, 1);
		}
		else if (items[6].equals("2")) {
			values.put(EventDataColumns.TIME2_VOTES, 1);
		}

		values.put(EventDataColumns.CREATED_DATE, System.currentTimeMillis());
		values.put(EventDataColumns.MODIFIED_DATE, System.currentTimeMillis());
		
		// Insert the record into "Confirmed Events" table
		try {
			Log.v(TAG, "getContentResolver().insert");
			Uri uri = context.getContentResolver().insert(EventDataColumns.CONTENT_URI_CONFIRMED_EVENTS, values);
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		}
		
	}
	
	
	private int getChoice(String value) {
		Log.v(TAG, "getChoice()");
		if (value.equals("1") || value.equals("2") )
			return 1;
		
		return 0;
	}
	
	
	private String mPlace1;
	private String mPlace2;
	private String mTime1;
	private String mTime2;
	private String mContactNames;
	
	private static final String[] CONFIRMATION_PROJECTION = new String[] {
		EventDataColumns._ID, // 0
		EventDataColumns.PLACE1, // 1
		EventDataColumns.PLACE2, // 2
		EventDataColumns.DATE1, // 3
		EventDataColumns.DATE2, // 4
		EventDataColumns.CONTACT_NAMES, // 5
	};
	
	public void getConfirmationEventDetails(Context context, String timestamp) {
		Log.v(TAG, "getConfirmationEventDetails()");
		
		mCursor = context.getContentResolver().query(
				EventDataColumns.CONTENT_URI_FRIENDS_EVENTS,
				CONFIRMATION_PROJECTION,
				EventDataColumns.TIMESTAMP + "=" + timestamp,
				null,
				null);

		if (mCursor == null) {
			Log.w(TAG, "mCursor is NULL");
			return;
		}

		// Requery in case something changed while paused
		mCursor.requery();
		// Make sure we are at the one and only row in the cursor.
		mCursor.moveToFirst();
		
		mPlace1 = mCursor.getString(1);
		mPlace2 = mCursor.getString(2);
		mTime1 = mCursor.getString(3);
		mTime2 = mCursor.getString(4);
		mContactNames = mCursor.getString(5);
	}
	
	
	
	/************ Updating event answer : DONE ****************/
	
	private void updateEventAnswer(Context context, String[] items, String receivedFrom) {
		for (int i = 0; i < items.length; i++) {
			Log.v(TAG, "updateEventAnswer() Items: " + items[i]);
		}

		String timestamp = items[TIMESTAMP_INDEX];
		String answer = items[4];
		String placeChoice = items[5];
		String timeChoice = items[6];

		p1Votes = p2Votes = t1Votes = t2Votes = 0;
		// IMPORTANT
		getCurrentVotes(context, timestamp);
		
		Log.v(TAG, "p1 votes: " + p1Votes);
		Log.v(TAG, "p2 votes: " + p2Votes);
		Log.v(TAG, "t1 votes: " + t1Votes);
		Log.v(TAG, "t2 votes: " + t2Votes);
		

		ContentValues values = new ContentValues();
		//values.put(EventDataColumns.EVENT_NAME, items[NAME_INDEX] + " Updated");
		// TODO - no need to update answer field 
		// OR store answer for each contact based on receiver ph number
		values.put(EventDataColumns.EVENT_ANSWER, "");


		Pattern p = Pattern.compile(DATA_SEPARATOR);

		// update place votes
		String[] votes = p.split(placeChoice);
		Log.v(TAG, "place votes length: " + votes.length);
		
		for (int i = 0; i < votes.length; i++) {
			Log.v(TAG, "place votes: " + votes[i]);

			int n = Integer.parseInt(votes[i]);
			if(n == 1) {
				values.put(EventDataColumns.PLACE1_VOTES, p1Votes+1);
			}
			if(n == 2) {
				values.put(EventDataColumns.PLACE2_VOTES, p2Votes+1);
			}
		}

		// update time votes
		votes = p.split(timeChoice);
		Log.v(TAG, "time votes length: " + votes.length);
		for (int i = 0; i < votes.length; i++) {
			Log.v(TAG, "time votes: " + votes[i]);

			int n = Integer.parseInt(votes[i]);
			if(n == 1) {
				values.put(EventDataColumns.TIME1_VOTES, t1Votes+1);
			}
			if(n == 2) {
				values.put(EventDataColumns.TIME2_VOTES, t2Votes+1);
			}
		}
		
		// Commit all of changes to persistent storage: CONTENT_URI_MY_EVENTS
		try {
			Log.v(TAG, "updateEventAnswer() getContentResolver().update() timestamp: " + timestamp);

			context.getContentResolver().update(
					EventDataColumns.CONTENT_URI_MY_EVENTS,
					values,
					EventDataColumns.TIMESTAMP + "=" + timestamp,
					null);

		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		}

	}


	private Cursor mCursor;
	private int p1Votes;
	private int p2Votes;
	private int t1Votes;
	private int t2Votes;
	
	private static final String[] PROJECTION = new String[] {
		EventDataColumns._ID, // 0
		EventDataColumns.PLACE1_VOTES, // 1
		EventDataColumns.PLACE2_VOTES, // 2
		EventDataColumns.TIME1_VOTES, // 3
		EventDataColumns.TIME2_VOTES, // 4
	};

	public void getCurrentVotes(Context context, String timestamp) {
		Log.v(TAG, "getCurrentVotes()");
		
		mCursor = context.getContentResolver().query(
				EventDataColumns.CONTENT_URI_MY_EVENTS,
				PROJECTION,
				EventDataColumns.TIMESTAMP + "=" + timestamp,
				null,
				null);

		if (mCursor == null) {
			Log.w(TAG, "mCursor is NULL");
			return;
		}

		// Requery in case something changed while paused
		mCursor.requery();
		// Make sure we are at the one and only row in the cursor.
		mCursor.moveToFirst();
		
		p1Votes = mCursor.getInt(1);
		p2Votes = mCursor.getInt(2);
		t1Votes = mCursor.getInt(3);
		t2Votes = mCursor.getInt(4);
		
	}


	
	
	/************ Updating new event: DONE ****************/
	
	private void updateNewEvent(Context context, String[] items, String receivedFrom, Uri contentUri) {
		for (int i = 0; i < items.length; i++) {
			Log.v(TAG, "updateDatabase() Items: " + items[i]);
		}

		// Get out updates into the provider.
		ContentValues values = new ContentValues();

		values.put(EventDataColumns.EVENT_TYPE, items[EVENT_TYPE_INDEX]);
		values.put(EventDataColumns.TIMESTAMP, items[TIMESTAMP_INDEX]);

		values.put(EventDataColumns.EVENT_NAME, items[NAME_INDEX]);
		values.put(EventDataColumns.PLACE1, items[P1_INDEX]);
		values.put(EventDataColumns.PLACE2, items[P2_INDEX]);
		values.put(EventDataColumns.DATE1, items[T1_INDEX]); // TODO - handle date and time
		values.put(EventDataColumns.DATE2, items[T2_INDEX]);
		values.put(EventDataColumns.TIME1, items[T1_INDEX]);
		values.put(EventDataColumns.TIME2, items[T2_INDEX]);

		values.put(EventDataColumns.CONTACT_NAMES, items[CONTACT_NAMES_INDEX] +  ";" + receivedFrom); // TODO
		values.put(EventDataColumns.PHONE_NUMBERS, "");
		values.put(EventDataColumns.EVENT_FROM, receivedFrom);

		values.put(EventDataColumns.EVENT_ANSWER, ""); // Y / N
		values.put(EventDataColumns.PLACE1_VOTES, 0);
		values.put(EventDataColumns.PLACE2_VOTES, 0);
		values.put(EventDataColumns.TIME1_VOTES, 0);
		values.put(EventDataColumns.TIME2_VOTES, 0);

		values.put(EventDataColumns.CREATED_DATE, System.currentTimeMillis());
		values.put(EventDataColumns.MODIFIED_DATE, System.currentTimeMillis());

		// Commit all of changes to persistent storage.
		try {
			Log.v(TAG, "getContentResolver().insert");
			Uri uri = context.getContentResolver().insert(contentUri, values);
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		}
	}




	/******************* TODO - Deleting SMS from inbox - last ************************/

	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

	private long getThreadId(Context context) {
		Log.v(TAG, "getThreadId()");
		long threadId = 0;

		String SMS_READ_COLUMN = "read";
		String WHERE_CONDITION = "";  //SMS_READ_COLUMN + " = 0";
		String SORT_ORDER = "date DESC";
		int count = 0;

		Log.v(TAG, "getThreadId() SMS_INBOX_CONTENT_URI");
		Cursor cursor = context.getContentResolver().query(
				SMS_INBOX_CONTENT_URI,
				new String[] { "_id", "thread_id", "address", "person", "date", "body" },
				WHERE_CONDITION,
				null,
				SORT_ORDER);

		if (cursor != null) {
			Log.v(TAG, "getThreadId() cursor != null");
			try {
				count = cursor.getCount();
				if (count > 0) {
					Log.v(TAG, "getThreadId() count > 0");

					cursor.moveToFirst();
					//cursor.moveToLast();

					long messageId = cursor.getLong(0);
					threadId = cursor.getLong(1);
					String address = cursor.getString(2);
					long contactId = cursor.getLong(3);
					String contactId_string = String.valueOf(contactId);
					long timestamp = cursor.getLong(4);

					String body = cursor.getString(5);

					String str = "getThreadId(): body: " + body;
					str += "\nthreadId: " + String.valueOf(threadId);

					Log.v(TAG, str);        			
					Toast.makeText(context, str, Toast.LENGTH_LONG).show();

				}
			} finally {
				cursor.close();
			}
		}

		return threadId;
	}


}
