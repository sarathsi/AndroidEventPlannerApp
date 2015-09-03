package com.ipc.thesis.android;

import com.ipc.thesis.android.EventData.EventDataColumns;

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
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsEventsListActivity extends ListActivity {

	private static final String TAG = "FriendsEventsListActivity";
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
			intent.setData(EventDataColumns.CONTENT_URI_FRIENDS_EVENTS);
		}

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		// Perform a managed query. The Activity will handle closing and requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
				EventDataColumns.DEFAULT_SORT_ORDER);

		// Used to map notes entries from the database to views
		EventsCursorAdapter adapter = new EventsCursorAdapter(
				this,
				R.layout.eventlist_item,
				cursor,
				new String[] { EventDataColumns.EVENT_NAME },
				new int[] { R.id.list_item_text }
		);
		setListAdapter(adapter);
		
	}

	
	public class EventsCursorAdapter extends SimpleCursorAdapter {

		private Cursor c;
		private Context context;
		private Bitmap mIcon1;
		
		public EventsCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			
			Log.v(TAG, "EventsCursorAdapter()");
			
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

			this.c.moveToPosition(pos);	
			
			String eventName = this.c.getString(this.c.getColumnIndex(EventDataColumns.EVENT_NAME));
			TextView name = (TextView) v.findViewById(R.id.list_item_text);
			name.setText(eventName);
			
			ImageView icon = (ImageView) v.findViewById(R.id.list_item_icon);
			
			// TODO - 
			if (eventName.contains("dinner")) {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.events_5);				
			}
			else if (eventName.contains("movie")) {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.events_6);				
			}
			else {
				mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
			}
			
			icon.setImageBitmap(mIcon1);
			
			return(v);
		}
	}
	
	
    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Events received from friends");
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(TAG, "onListItemClick()");
		
		Uri eventUri = ContentUris.withAppendedId(getIntent().getData(), id);
		Log.v(TAG, "Starting Answer Event Activity with " + eventUri.toString());

		// Launch activity to view/edit the currently selected item
		Intent intent = new Intent(this, com.ipc.thesis.android.EventVotingActivity.class);

		intent.putExtra("uri", eventUri.toString());

		try {
			startActivity(intent);
			
		} catch (Exception e) {
			Log.e(TAG, "Exception: ", e);
			return;
		}
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
				new ComponentName(this, FriendsEventsListActivity.class), null, intent, 0, null);

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
				new ComponentName(this, FriendsEventsListActivity.class), null, intent, 0, null);
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
			Intent intent = new Intent(this, com.ipc.thesis.android.EventVotingActivity.class);
			intent.putExtra("uri", mCurrentEventUri.toString());
			startActivity(intent);
			return true;

		case R.id.context_delete:
			//getContentResolver().delete(eventUri, null, null);
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

			return new AlertDialog.Builder(FriendsEventsListActivity.this)
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

		Uri myEventsUri = EventDataColumns.CONTENT_URI_FRIENDS_EVENTS;

		// There is no where clause. deletes all records
		getContentResolver().delete(myEventsUri, null, null);
	}


}
