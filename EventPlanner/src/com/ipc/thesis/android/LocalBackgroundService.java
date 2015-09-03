package com.ipc.thesis.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocalBackgroundService extends Service {

	private static final String TAG = "LocalBackgroundService";
	
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind()");
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate()");
        Toast.makeText(getApplicationContext(), "LocalBackgroundService Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy()
    {
        Log.v(TAG, "onDestroy()");
        Toast.makeText(getApplicationContext(), "LocalBackgroundService Destroyed", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }

    
    /*
    Called by the system every time a client explicitly starts the service 
    by calling startService(Intent), providing the arguments it supplied and 
    a unique integer token representing the start request. 
    Do not call this method directly.
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        int counter = intent.getExtras().getInt("counter");
        Log.v(TAG, "onStartCommand(), counter = " + counter +
        		", startId = " + startId);

        Toast.makeText(getApplicationContext(), "LocalBackgroundService Started", Toast.LENGTH_LONG).show();


        /*
        For backwards compatibility, the default implementation calls onStart(Intent, int) 
        and returns either START_STICKY or START_STICKY_COMPATIBILITY.
        */ 
        // START_NOT_STICKY - service to continue running until it is explicitly stopped
        return START_NOT_STICKY;
    }
    
}
