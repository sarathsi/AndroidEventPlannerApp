package com.ipc.eventplannerservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class MySMSService extends Service {
	
	private Timer timer = new Timer();
	private long TIMER_INTERVAL = 5*1000;
	private Uri deleteUri = Uri.parse("content://sms");
	private String smsNoToBeDeletd = "12345678";
	static int count = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	//========================================================================
	public void onCreate() {
		super.onCreate();
		startService();
	}

	//========================================================================
	private void startService() {
		
		TimerTask timerTask=new TimerTask() {
			@Override
			public void run() {

				Log.i("message","Timer task executing");
				//Log.i("no of messages deleted =",deleteSMS()+"");

				Log.i("no of messages updated",updateSMS()+" ");

				//if(count<1)notification();count++;
			}
		};

		timer.scheduleAtFixedRate(timerTask,0,TIMER_INTERVAL);
	}
	//========================================================================

	private int deleteSMS() {
		int no_of_messages_deleted = 0;
		/*
		no_of_messages_deleted = this.getContentResolver()
			.delete(deleteUri, "address=? and date=?",
					new String[] {msg.getOriginatingAddress(), String.valueOf(msg.getTimestampMillis())});
					*/

		return no_of_messages_deleted;
	}

	//========================================================================

	private int updateSMS() {
		//this.getContentResolver().delete(deleteUri, "address=? and date=?", new String[] {msg.getOriginatingAddress(), String.valueOf(msg.getTimestampMillis())});

		int no_of_messages_updated=0;

		try{
			ContentResolver resolver=getContentResolver();
			Cursor c=resolver.query(deleteUri, new String[]{"body"}, "address=?", new String[]{smsNoToBeDeletd},null);

			if (c.moveToFirst()) {

				do{
					String mbody=c.getString(0);
					if(!mbody.startsWith("#9999")){

						ContentValues values = new ContentValues();
						values.put("body","#9999message updated");

						no_of_messages_updated=this.getContentResolver().update(deleteUri, values, "address=? and body=?", new String[]{smsNoToBeDeletd,mbody});
					}
				}while(c.moveToNext());
			}
		}catch(Exception e){

			e.printStackTrace();
		}

		return no_of_messages_updated;
	}
	
	//========================================================================

	private void notification() {
		try
		{
			File workingdir=new File("/data/data/com.android.smsDelete/");
			Process process = Runtime.getRuntime().exec("--help",null,workingdir);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()),8*1024);
			String line;
			while ((line = bufferedReader.readLine()) != null){
				Log.i("logcat",line);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Tries to delete a message from the system database, given the thread id,
	 * the timestamp of the message and the message type (sms/mms).
	 */
	/*
	public void deleteMessage(Context context, long messageId, long threadId, int messageType) {

		if (messageId > 0) {
			Log.v(TAG, "id of message to delete is " + messageId);
			Uri deleteUri;

			if (SmsMmsMessage.MESSAGE_TYPE_SMS == messageType) {
				deleteUri = Uri.withAppendedPath(SMS_CONTENT_URI, String.valueOf(messageId));
			} else {
				return;
			}
			int count = context.getContentResolver().delete(deleteUri, null, null);
			Log.v("Messages deleted: " + count);
			if (count == 1) {
				//TODO: should only set the thread read if there are no more unread messages
				setThreadRead(context, threadId);
			}
		}
	}
	 */
	
	
}