package com.ipc.thesis.android;

import java.util.ArrayList;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ipc.eventplannerservice.IEventPlannerService;
import com.ipc.thesis.android.R;

public class SelectContactsActivity extends Activity implements
		View.OnClickListener {

	private static final String TAG = "SelectContactsActivity";
	private static final String FIELD_SEPARATOR = "#";
	private static final String DATA_SEPARATOR = ";";

	private IEventPlannerService mEventPlannerService = null;

	// activity result codes
	private static final int PICK_CONTACT = 1001;

	private EditText mEtxtContactList;
	private Button mBtnOpenContacts;
	private Button mBtnClearContacts;
	private Button mBtnSendInvitation;

	private String mSmsString;
	ArrayList<String> mContactNameList;
	ArrayList<String> mPhoneNumberList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_contacts);

		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			mSmsString = extras.getString("sms");
		}

		// Get pointers to the widgets defined in layout file
		// (create_newevent.xml)
		mEtxtContactList = (EditText) findViewById(R.id.eTxtContactList);

		mBtnOpenContacts = (Button) findViewById(R.id.btnOpenContacts);
		mBtnOpenContacts.setOnClickListener(this);

		mBtnClearContacts = (Button) findViewById(R.id.btnClearContacts);
		mBtnClearContacts.setOnClickListener(this);

		mBtnSendInvitation = (Button) findViewById(R.id.btnSendInvitation);
		mBtnSendInvitation.setOnClickListener(this);

		// TODO - modify this to array of Contact
		mContactNameList = new ArrayList<String>();
		mPhoneNumberList = new ArrayList<String>();

		Toast.makeText(getApplicationContext(), mSmsString, Toast.LENGTH_SHORT)
				.show();

		bind(); // Important
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");
		unbind();
		super.onDestroy();
	}

	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btnOpenContacts:
			openContacts();
			break;

		case R.id.btnClearContacts:
			clearContacts();
			break;

		case R.id.btnSendInvitation:
			sendInvitation();
			break;

		default:
			Toast.makeText(getApplicationContext(), "Not implemented!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void bind() {
		Log.v(TAG, "bind()");
		bindService(new Intent(IEventPlannerService.class.getName()),
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
			mEventPlannerService = IEventPlannerService.Stub
					.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "onServiceDisconnected() called");
			mEventPlannerService = null;
		}
	};

	// Opens contacts app and selects single contact
	private void openContacts() {
		Log.v(TAG, "openContacts");

		try {
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO - remove duplicate numbers
	private void clearContacts() {
		Log.v(TAG, "clearContacts");
		mEtxtContactList.setText("");
		mContactNameList.clear();
		mPhoneNumberList.clear();
	}

	private void sendInvitation() {
		Log.v(TAG, "sendInvitation");

		if (mSmsString.length() == 0) {
			Toast.makeText(getApplicationContext(), "Event is empty!",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (mPhoneNumberList.isEmpty()) {
			Toast.makeText(getApplicationContext(), "Select some contacts!",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Append Contact names also to the new event SMS
		mSmsString = mSmsString + FIELD_SEPARATOR;
		if (mContactNameList.size() > 0) {
			mSmsString = mSmsString + mContactNameList.get(0);
			for (int i = 1; i < mContactNameList.size(); i++) {
				mSmsString = mSmsString + DATA_SEPARATOR
						+ mContactNameList.get(i);
			}
		}

		String phListStr = Utils.arrayListToString(mPhoneNumberList, ";");
		// Toast.makeText(getApplicationContext(), phListStr,
		// Toast.LENGTH_SHORT).show();

		// IPC call - IMPORTANT
		try {
			int result = mEventPlannerService.sendNewEvent(phListStr, mSmsString);
		} catch (DeadObjectException ee) {
			Toast.makeText(getApplicationContext(), "DeadObjectException!",
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, ee.getMessage(), ee);
		} catch (RemoteException ee) {
			Toast.makeText(getApplicationContext(), "RemoteException!",
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, ee.getMessage(), ee);
		}

		// finish the activity
		finish();
	}

	private void sendSMS(String number, String text) {
		// SmsManager sm = SmsManager.getDefault();
		// sm.sendTextMessage(number, null, text, null, null);

		Log.v(TAG, "sendSMS");

		// IPC call
		try {
			mEventPlannerService.sendNewEvent(number, text);
		} catch (RemoteException ee) {
			Log.e(TAG, ee.getMessage(), ee);
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {

		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {

				Uri contactDataUri = data.getData();
				Cursor contactDataCursor = managedQuery(contactDataUri, null,
						null, null, null);

				if (contactDataCursor.moveToFirst()) {
					// long _ID
					// string CONTACT_STATUS
					// string LOOKUP_KEY
					// string DISPLAY_NAME_PRIMARY

					long contactID = contactDataCursor
							.getLong(contactDataCursor
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String name = contactDataCursor
							.getString(contactDataCursor
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

					Toast.makeText(getApplicationContext(), name,
							Toast.LENGTH_SHORT).show(); // String.valueOf(contactID)
					mContactNameList.add(name);
					String ph = getPhoneNumberForContact(contactID);
					mPhoneNumberList.add(ph);

					// printArrayList(mContactNameList);
					// printArrayList(mPhoneNumberList);

					// update ContactList EditText
					String str = mEtxtContactList.getText().toString();
					str = str + "\n" + name + " (" + ph + ")";
					mEtxtContactList.setText(str);
				}
				contactDataCursor.close();
			}
			break;

		default:
			break;
		} // end of switch
	}

	public String getPhoneNumberForContact(long contactId) {

		// ContactsContract.CommonDataKinds.Phone
		final String[] projection = new String[] { Phone.NUMBER, Phone.TYPE, };

		final Cursor phoneCursor = managedQuery(Phone.CONTENT_URI, projection,
				Data.CONTACT_ID + "=?", // ContactsContract.Data
				new String[] { String.valueOf(contactId) }, null);

		String phNumber = "";

		if (phoneCursor.moveToFirst()) {
			final int contactNumberColumnIndex = phoneCursor
					.getColumnIndex(Phone.NUMBER);
			// final int contactTypeColumnIndex =
			// phoneCursor.getColumnIndex(Phone.TYPE);

			phNumber = phoneCursor.getString(contactNumberColumnIndex);

			/*
			 * // read all available phone numbers while(!phone.isAfterLast()) {
			 * final String number = phone.getString(contactNumberColumnIndex);
			 * final int type = phone.getInt(contactTypeColumnIndex);
			 * 
			 * // TODO - do something here. Store the ph numbers or show ph
			 * number picker if more than one.
			 * 
			 * Toast.makeText(getApplicationContext(), number,
			 * Toast.LENGTH_SHORT).show(); phone.moveToNext(); }
			 */
		}

		phoneCursor.close();
		return phNumber;
	}

	private void printArrayList(ArrayList<String> arrayList) {
		for (String str : arrayList)
			Log.v(TAG, str);
	}

	// For Testing
	private void openContacts2() {
		/*
		 * Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		 * intent.setType("content://contacts/people/*");
		 * startActivityForResult(intent,PICK_CONTACT);
		 */

		/*
		 * Intent intent = new Intent(Intent.ACTION_PICK,
		 * ContactsContract.Contacts.CONTENT_URI);
		 * startActivityForResult(intent, PICK_CONTACT);
		 */

		/*
		 * Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		 * intent.setType("image/*"); intent.putExtra("crop", "true");
		 * intent.putExtra("aspectX", 1); intent.putExtra("aspectY", 1);
		 * intent.putExtra("scale", true);
		 * 
		 * String tempUri; intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
		 * intent.putExtra("outputFormat",
		 * Bitmap.CompressFormat.JPEG.toString());
		 * 
		 * startActivityForResult(intent, PICK_CONTACT);
		 */

		/*
		 * Intent sendIntent = new Intent(Intent.ACTION_PICK);
		 * sendIntent.setType("application/people");
		 * startActivity(Intent.createChooser(sendIntent, null));
		 */

		/*
		 * // Let user select (multiple) from a list of contacts with email
		 * addresses //Intent i = new Intent(Intent.ACTION_GET_CONTENT,
		 * ContactsContract.Contacts.CONTENT_URI); //content://contacts/people/
		 * Intent i = new Intent(Intent.ACTION_GET_CONTENT,
		 * ContactsContract.Contacts.CONTENT_URI); startActivityForResult(i,
		 * PICK_CONTACT);
		 */

		/*
		 * @SuppressWarnings("deprecation") Intent i = new
		 * Intent(Intent.ACTION_GET_CONTENT, People.CONTENT_URI);
		 * startActivityForResult(Intent.createChooser(i, ""), PICK_CONTACT);
		 */

		/*
		 * Intent intent = new Intent(Intent.ACTION_PICK);
		 * intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
		 * startActivityForResult(intent, PICK_CONTACT);
		 */
	}

}
