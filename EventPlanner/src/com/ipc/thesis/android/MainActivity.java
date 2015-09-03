package com.ipc.thesis.android;

import com.ipc.eventplannerservice.IEventPlannerService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity
implements View.OnClickListener {

	private static final String TAG = "MainActivity";
	private IEventPlannerService mEventPlannerService = null;
	private int mCounter = 1; // test counter


	private Button mBtnCreateNewEvent;
	private Button mBtnStartLocalService;
	private Button mBtnStopLocalService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		// set button on-click listener
		mBtnCreateNewEvent = (Button) findViewById(R.id.btnCreateNewEvent);
		mBtnCreateNewEvent.setOnClickListener(this);

		mBtnStartLocalService = (Button) findViewById(R.id.btnStartService);
		mBtnStartLocalService.setOnClickListener(this);

		mBtnStopLocalService = (Button) findViewById(R.id.btnStopService);
		mBtnStopLocalService.setOnClickListener(this);


		Intent intent = new Intent(IEventPlannerService.class.getName());

		// start the service explicitly. otherwise it will only run while the IPC connection is up.
		startService(intent);

		//bindService(intent, serviceConn, 0); // Context.BIND_AUTO_CREATE

		/* // Another way of starting and binding service
		bindService(new Intent(IEventPlannerService.class.getName()),
					serviceConn,
					Context.BIND_AUTO_CREATE);

		 */

		Log.v(TAG, "Activity created");
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");

		try {
			//unbindService(serviceConn);
		} catch (Throwable t) {
			Log.w(TAG, "Failed to unbind from the service", t);
		}

		Log.v(TAG, "Activity destroyed");

		super.onDestroy();
	}

	// implements View.OnClickListener
	@Override
	public void onClick(View v) {
		switch ( v.getId()) {
		case R.id.btnCreateNewEvent:
			createNewEvent();
			break;

		case R.id.btnStartService:
			showMyEventList();
			//bind();
			break;

		case R.id.btnStopService:
			showOthersEventList();
			break;

		default:
			Toast.makeText(getApplicationContext(), "Not implemented!", Toast.LENGTH_SHORT).show();
		}

	}


	private void createNewEvent() {
		Log.v(TAG, "createNewEvent()");

		try {
			Intent intent = new Intent(this, com.ipc.thesis.android.CreateNewEventActivity.class);
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void showMyEventList() {
		Log.v(TAG, "showMyEventList()");

		try {
			Intent intent = new Intent(this, com.ipc.thesis.android.MyEventsListActivity.class);
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showOthersEventList() {
		Log.v(TAG, "showOthersEventList()");

		try {
			Intent intent = new Intent(this, com.ipc.thesis.android.FriendsEventsListActivity.class);
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.v(TAG, "onServiceConnected() called");
			mEventPlannerService = IEventPlannerService.Stub.asInterface(service);

			//String test = mEventPlannerService.retriveNewEvent();
			callService();

		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "onServiceDisconnected() called");
			mEventPlannerService = null;
		}
	};


	private void callService() {
		/*
		try {
			String val = mEventPlannerService.retriveNewEvent();
			Toast.makeText(MainActivity.this, "Value from service is "+val, 
					Toast.LENGTH_SHORT).show();
		} catch (RemoteException ee) {
			Log.e(TAG, ee.getMessage(), ee);
		}
		*/
	}

	private void startLocalService() {
		Log.v(TAG, "Starting service... mCounter = " + mCounter);

		Intent intent = new Intent(MainActivity.this,
				LocalBackgroundService.class);

		intent.putExtra("mCounter", mCounter++);

		startService(intent);
	}

	private void stopLocalService() {
		Log.v(TAG, "Stopping service...");

		if(stopService(new Intent(MainActivity.this, LocalBackgroundService.class))) {
			Log.v(TAG, "stopService was successful");
			Toast.makeText(getApplicationContext(), "stopService is successful", Toast.LENGTH_LONG).show();
		}
		else {
			Log.v(TAG, "stopService was unsuccessful");
			Toast.makeText(getApplicationContext(), "stopService is unsuccessful", Toast.LENGTH_LONG).show();
		}
	}


}
