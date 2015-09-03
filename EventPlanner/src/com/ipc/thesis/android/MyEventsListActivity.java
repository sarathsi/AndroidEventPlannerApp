package com.ipc.thesis.android;

import com.ipc.thesis.android.EventData.EventDataColumns;
//import com.ipc.eventplannerservice.EventData.EventDataColumns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MyEventsListActivity extends ListActivity {

	private static final String TAG = "MyEventsListActivity";
	private static final int DIALOG_YES_NO_DELETE_EVENT = 1;

	private Uri mCurrentEventUri;	

	/**
	 * The columns we are interested in from the database
	 */
	private static final String[] PROJECTION = new String[] {
		EventDataColumns._ID, // 0
		EventDataColumns.EVENT_NAME, // 1
	};

	/** The index of the title column */
	private static final int COLUMN_INDEX_EVENT_NAME = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(EventDataColumns.CONTENT_URI_MY_EVENTS);
		}

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		// Perform a managed query. The Activity will handle closing and requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
				EventDataColumns.DEFAULT_SORT_ORDER);


		// Used to map notes entries from the database to views
		MyEventsCursorAdapter adapter = new MyEventsCursorAdapter(
				this,
				R.layout.eventlist_item, 
				cursor,
				new String[] { EventDataColumns.EVENT_NAME },
				new int[] { R.id.list_item_text }
		);
		setListAdapter(adapter);

		//setListAdapter(new EventsCursorAdapter(this, cursor));

		//setTitle("My Event List");
	}

	public class MyEventsCursorAdapter extends SimpleCursorAdapter {

		private Cursor c;
		private Context context;
		private Bitmap mIcon1;
		
		public MyEventsCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			
			Log.v(TAG, "MyEventsCursorAdapter()");
			
			this.c = c;
			this.context = context;
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
		}

		public View getView(int pos, View inView, ViewGroup parent) {
			Log.v(TAG, "getView()");
			
			View v = inView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.eventlist_item, null);
			}

			Log.v(TAG, "moveToPosition()");
			this.c.moveToPosition(pos);	
			
			String eventName = this.c.getString(this.c.getColumnIndex(EventDataColumns.EVENT_NAME));
			TextView name = (TextView) v.findViewById(R.id.list_item_text);
			name.setText(eventName);
			
			ImageView icon = (ImageView) v.findViewById(R.id.list_item_icon);
			
			Log.v(TAG, "eventName checking");
			// TODO - upper cases check
			if (eventName.contains("dinner") || eventName.contains("lunch") || eventName.contains("food")) {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.food);				
			}
			else if (eventName.contains("movie")) {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.movie);				
			}
			else if (eventName.contains("meeting")) {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.meeting);				
			}
			else if (eventName.contains("dance")) {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.dance);				
			}
			else {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.common_event);
			}
			
			icon.setImageBitmap(mIcon1);
			
			return(v);
		}
	}


	private static class EventsCursorAdapter extends CursorAdapter {

		public EventsCursorAdapter(Context context, Cursor c) {
			super(context, c);

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}

	}
	

	@Override
	protected void onResume() {
		super.onResume();
		setTitle("My Event List");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(TAG, "onListItemClick()");

		Uri eventUri = ContentUris.withAppendedId(getIntent().getData(), id);

		// Launch activity to view/edit the currently selected item
		Intent intent = new Intent(this, com.ipc.thesis.android.MyEventDetailsActivity.class);

		intent.putExtra("uri", eventUri.toString());

		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v(TAG, "onCreateOptionsMenu()");

		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_list_options_menu, menu);

		//this allows other applications to extend our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, MyEventsListActivity.class), null, intent, 0, null);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG, "onOptionsItemSelected()");

		switch (item.getItemId()) {

		case R.id.menu_create_new_event:
			// Launch activity to create a new event
			createNewEvent();
			return true;

		case R.id.menu_help:
			Toast.makeText(getApplicationContext(), "HELP in progress...", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.menu_about:
			Toast.makeText(getApplicationContext(), "ABOUT in progress...", Toast.LENGTH_SHORT).show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		Log.v(TAG, "onCreateContextMenu()");

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}

		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_list_context_menu, menu);

		// Set the context menu header
		menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_EVENT_NAME));

		// Append to the
		// menu items for any other activities that can do stuff with it
		// as well.  This does a query on the system for any activities that
		// implement the ALTERNATIVE_ACTION for our data, adding a menu item
		// for each one that is found.

		Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(), 
				Integer.toString((int) info.id) ));
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, MyEventsListActivity.class), null, intent, 0, null);
	}



	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		mCurrentEventUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

		switch (item.getItemId()) {
		case R.id.context_open:

			// Launch activity to view/edit the currently selected item
			Intent intent = new Intent(this, com.ipc.thesis.android.MyEventDetailsActivity.class);
			intent.putExtra("uri", mCurrentEventUri.toString());
			startActivity(intent);
			return true;

		case R.id.context_delete:
			//getContentResolver().delete(mCurrentEventUri, null, null);
			// item.getTitle()
			showDialog(DIALOG_YES_NO_DELETE_EVENT);
			return true;


		default:
			return super.onContextItemSelected(item);
		}
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		Log.v(TAG, "onCreateDialog(): " + String.valueOf(id));

		switch (id) {
		case DIALOG_YES_NO_DELETE_EVENT:
			Log.v(TAG, "DIALOG_YES_NO_DELETE_EVENT");

			return new AlertDialog.Builder(MyEventsListActivity.this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle("The event will be deleted!")
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked OK so do some stuff */
					getContentResolver().delete(mCurrentEventUri, null, null);
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked Cancel so do some stuff */
				}
			})
			.create();
		}
		return null;
	}


	/*
	 * Private methods
	 */
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

	private void deleteEvents() {
		Log.v(TAG, "deleteEvents()");
		// TODO - show Warning / Confirmation message
		// TODO - delete option should be removed here

		Uri myEventsUri = EventDataColumns.CONTENT_URI_MY_EVENTS;

		// There is no where clause. deletes all records
		getContentResolver().delete(myEventsUri, null, null);
	}


}
