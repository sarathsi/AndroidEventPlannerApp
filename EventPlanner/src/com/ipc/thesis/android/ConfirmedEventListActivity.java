package com.ipc.thesis.android;

import com.ipc.thesis.android.EventData.EventDataColumns;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ConfirmedEventListActivity extends ListActivity {

	private static final String TAG = "ConfirmedEventListActivity";
	
	private static final String[] PROJECTION = new String[] {
		EventDataColumns._ID, // 0
		EventDataColumns.EVENT_NAME, // 1
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(EventDataColumns.CONTENT_URI_CONFIRMED_EVENTS);
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
	
    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Event Planner: Confirmed Events");
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(TAG, "onListItemClick()");
		
		Uri eventUri = ContentUris.withAppendedId(getIntent().getData(), id);
		Log.v(TAG, "Starting Confirmed Event Activity with " + eventUri.toString());

		// Launch activity to view/edit the currently selected item
		Intent intent = new Intent(this, com.ipc.thesis.android.ConfirmedEventDetailsActivity.class);

		intent.putExtra("uri", eventUri.toString());

		try {
			startActivity(intent);
			
		} catch (Exception e) {
			Log.e(TAG, "Exception: ", e);
			return;
		}
		
	}
	
	
	
}
