package com.ipc.eventplannerservice;

import java.util.regex.Pattern;

import com.ipc.eventplannerservice.EventData.EventDataColumns;
import com.ipc.eventplannerservice.IEventPlannerService;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import android.content.BroadcastReceiver;

public class EventPlannerService extends Service {

	private static final String TAG = "EventPlannerService";

	private String[] mPhoneNumbers;
	private int mNoOfContacts;

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "onBind()");
		
//		if(IEventPlannerService.class.getName().equals(intent.getAction())) {
//			return new EventPlannerServiceImpl();
//		}
//		return null;
		
		return new EventPlannerServiceImpl();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(TAG, "onUnbind()");
		return false;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate()");
		super.onCreate();
		//Toast.makeText(getApplicationContext(), "EventPlannerService Created", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		Log.v(TAG, "onLowMemory()");
		
	}

	/*
    Called by the system every time a client explicitly starts the service 
    by calling startService(Intent), providing the arguments it supplied and 
    a unique integer token representing the start request. 
    Do not call this method directly.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand()");

		super.onStartCommand(intent, flags, startId);

		Toast.makeText(getApplicationContext(), "EventPlannerService Started", Toast.LENGTH_SHORT).show();

		/*
        For backwards compatibility, the default implementation calls onStart(Intent, int) 
        and returns either START_STICKY or START_STICKY_COMPATIBILITY.
		 */ 
		// START_NOT_STICKY - service to continue running until it is explicitly stopped
		return START_NOT_STICKY;
	}

	public void onStop() {
		Log.v(TAG, "onStop()");
	}
	


	//////// http://developer.android.com/guide/developing/tools/aidl.html

	public class EventPlannerServiceImpl extends IEventPlannerService.Stub
	{
		private static final String FIELD_SEPARATOR = "#";

		// Sending a new event to friends
		public int sendNewEvent(String phoneNumbers, String message) 
				throws RemoteException {
			Log.v(TAG, "sendNewEvent()" + "message: " + message);

			splitStringToArray(phoneNumbers, ";");
			mNoOfContacts = mPhoneNumbers.length;
			try {
				for (int i = 0; i < mPhoneNumbers.length; i++) {
					sendSMS(mPhoneNumbers[i], message);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}

			// insert the new event to database
			// get the values from SMS string
			Pattern p = Pattern.compile(FIELD_SEPARATOR);
			String[] items = p.split(message);

			for (int i = 0; i < items.length; i++) {
				Log.v(TAG, "Items: " + items[i]);
			}

			/*
			 * Example SMS:
			 * EPSMS#N#291020111631#dinner#tut#home#29-11-2011 01:06#29-11-2011 01:06#Sarath Singapati;Harika Pacchabatla
			 */
			int EVENT_TYPE_INDEX = 1;
			int TIMESTAMP_INDEX = 2;
			int NAME_INDEX = 3;
			int P1_INDEX = 4;
			int P2_INDEX = 5;
			int T1_INDEX = 6;
			int T2_INDEX = 7;
			int CONTACT_NAMES_INDEX = 8;

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

			values.put(EventDataColumns.CONTACT_NAMES, items[CONTACT_NAMES_INDEX] + ";My self"); // TODO
			values.put(EventDataColumns.PHONE_NUMBERS, phoneNumbers); // friends phone numbers
			values.put(EventDataColumns.EVENT_FROM, "self");

			values.put(EventDataColumns.EVENT_ANSWER, ""); // Y / N
			values.put(EventDataColumns.PLACE1_VOTES, 0);
			values.put(EventDataColumns.PLACE2_VOTES, 0);
			values.put(EventDataColumns.TIME1_VOTES, 0);
			values.put(EventDataColumns.TIME2_VOTES, 0);

			values.put(EventDataColumns.CREATED_DATE, System.currentTimeMillis());
			values.put(EventDataColumns.MODIFIED_DATE, System.currentTimeMillis());


			// Commit all of changes to persistent storage. When the update completes
			// the content provider will notify the cursor of the change, which will
			// cause the UI to be updated.
			try {
				Log.v(TAG, "getContentResolver().insert");
				Uri uri = getContentResolver().insert(EventDataColumns.CONTENT_URI_MY_EVENTS, values);
			} catch (NullPointerException e) {
				Log.e(TAG, e.getMessage());
				return 0;
			}

			return 1;
		}

		@Override
		public int sendEventReply(String phoneNumber, String eventReplySMS)
		throws RemoteException {

			Log.v(TAG, "sendEventReply() " + phoneNumber + "  " + eventReplySMS);

			try {
				sendSMS(phoneNumber, eventReplySMS);
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
			
			return 1;
		}

		@Override
		public int sendEventConfirmation(String phoneNumberList,
				String eventConfirmationSMS) throws RemoteException {

			Log.v(TAG, "sendEventConfirmation() " + phoneNumberList + "  " + eventConfirmationSMS);

			String[] phoneNumbers;
			Pattern p = Pattern.compile(";");
			phoneNumbers = p.split(phoneNumberList);

			for (int i = 0; i < phoneNumbers.length; i++) {
				Log.v(TAG, "sendEventConfirmation(): " + phoneNumbers[i]);
			}

			try {
				for (int i = 0; i < phoneNumbers.length; i++) {
					sendSMS(phoneNumbers[i], eventConfirmationSMS);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}

			return 1;
		}

	} // End of EventPlannerServiceImpl


	public void splitStringToArray(String input, String separator) {
		Pattern p = Pattern.compile(separator);
		mPhoneNumbers = p.split(input);

		for (int i = 0; i < mPhoneNumbers.length; i++) {
			Log.v(TAG, mPhoneNumbers[i]);
		}
	}

	private void sendSMS(String phoneNumber, String message)
	{        
		Log.v(TAG, "sendSMS() " + phoneNumber + "  " + message);

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
				new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		// event will fire when SMS has been sent
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				Log.v(TAG, "SENT BroadcastReceiver - onReceive()");

				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					Log.v(TAG, "SENT BroadcastReceiver - onReceive() - SMS sent");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.v(TAG, "SENT BroadcastReceiver - onReceive() - Generic failure");
					Toast.makeText(getBaseContext(), "Generic failure", 
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service", 
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU", 
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off", 
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// event will fire when an SMS is delivered successfully
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				Log.v(TAG, "DELIVERED BroadcastReceiver - onReceive()");
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					Log.v(TAG, "DELIVERED BroadcastReceiver - onReceive() - SMS delivered");
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered", 
							Toast.LENGTH_SHORT).show();
					break;                        
				}
			}
		}, new IntentFilter(DELIVERED));


		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(
				phoneNumber,	// destinationAddress
				null,			// scAddress - service center address or null to use the current default SMSC
				message,		// body of the message
				sentPI,			// sentIntent
				deliveredPI);	// deliveryIntent
	}  




	/**********************************************************************/


	/*
	//PendingIntent:
	A description of an Intent and target action to perform with it. 
	Instances of this class are created with 
	getActivity(Context, int, Intent, int), 
	getBroadcast(Context, int, Intent, int), 
	getService (Context context, int requestCode, Intent intent, int flags)

	the returned object can be handed to other applications so that they can perform 
	the action you described on your behalf at a later time. 
	 */


}
