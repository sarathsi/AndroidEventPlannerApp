package com.ipc.thesis.android;

import com.ipc.eventplannerservice.IEventPlannerService;

import android.app.Activity;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainView extends TabActivity {
	private static final String TAG = "MainView";
	private IEventPlannerService mEventPlannerService = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        Bundle b = this.getIntent().getExtras();
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost    
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // start the service explicitly. otherwise it will only run while the IPC connection is up.
		intent = new Intent(IEventPlannerService.class.getName());
		startService(intent);
		
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, MyEventsListActivity.class);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("my_events")
        		.setIndicator("My Active Events", res.getDrawable(R.drawable.my_events_1))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, FriendsEventsListActivity.class);
        spec = tabHost.newTabSpec("friends_events")
        		.setIndicator("Friends Active Events", res.getDrawable(R.drawable.friends_events))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, ConfirmedEventListActivity.class);
        spec = tabHost.newTabSpec("confirmed_events")
        		.setIndicator("Confirmed Events", res.getDrawable(R.drawable.confirmed_events))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
        
//	     int totalTabs = tabHost.getTabWidget().getChildCount();
//	     ((RelativeLayout)tabHost.getTabWidget().getChildTabViewAt(totalTabs-1)).removeViewAt(0);
//	     ((TextView)((RelativeLayout)tabHost.getTabWidget().getChildTabViewAt(totalTabs-1)).getChildAt(0)).setHeight(30);
//	     tabHost.getTabWidget().getChildAt(totalTabs-1).getLayoutParams().height = 100;
	     
        tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 80;
        tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 80;
        tabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 80;
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
			//callService();
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
			Toast.makeText(MainView.this, "Value from service is "+val, 
					Toast.LENGTH_SHORT).show();
		} catch (RemoteException ee) {
			Log.e(TAG, ee.getMessage(), ee);
		}
		*/
	}
	


	
	
}
