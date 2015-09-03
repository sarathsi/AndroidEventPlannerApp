package com.ipc.eventplannerservice;

import java.util.HashMap;

import com.ipc.eventplannerservice.EventData.EventDataColumns;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class EventDataProvider extends ContentProvider {

	private static final String TAG = "EventDataProvider";

	private static final String DATABASE_NAME = "eventplanner.db";
	private static final int DATABASE_VERSION = 1;

	// table names
	private static final String EVENTS_BY_ME_TABLE_NAME = "events_by_me";
	private static final String EVENTS_BY_FRIENDS_TABLE_NAME = "events_by_friends";
	private static final String EVENTS_CONFIRMED_TABLE_NAME = "events_confirmed";

	// projection 
	private static HashMap<String, String> sEventsProjectionMap;

	private static final int MY_EVENTS = 1;
	private static final int MY_EVENT_ID = 2;

	private static final int FRIENDS_EVENTS = 3;
	private static final int FRIENDS_EVENT_ID = 4;

	private static final int CONFIRMED_EVENTS = 5;
	private static final int CONFIRMED_EVENT_ID = 6;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "DatabaseHelper onCreate()");

			// Table to store events sent by self
			createTable(db, EVENTS_BY_ME_TABLE_NAME);

			// Table to store events sent by friends
			createTable(db, EVENTS_BY_FRIENDS_TABLE_NAME);

			// table to store confirmed / closed events
			createTable(db, EVENTS_CONFIRMED_TABLE_NAME);
		}

		private void createTable(SQLiteDatabase db, String tableName) {
			Log.v(TAG, "DatabaseHelper createTable(): " + tableName);

			db.execSQL("CREATE TABLE " + tableName + " ("
					+ EventDataColumns._ID + " INTEGER PRIMARY KEY,"
					+ EventDataColumns.EVENT_TYPE + " TEXT,"
					+ EventDataColumns.TIMESTAMP + " TEXT,"

					+ EventDataColumns.EVENT_NAME + " TEXT,"
					+ EventDataColumns.PLACE1 + " TEXT,"
					+ EventDataColumns.PLACE2 + " TEXT,"
					+ EventDataColumns.DATE1 + " TEXT,"
					+ EventDataColumns.DATE2 + " TEXT,"
					+ EventDataColumns.TIME1 + " TEXT,"
					+ EventDataColumns.TIME2 + " TEXT,"
					+ EventDataColumns.CONTACT_NAMES + " TEXT,"
					+ EventDataColumns.PHONE_NUMBERS + " TEXT,"
					+ EventDataColumns.EVENT_FROM + " TEXT,"
					
					+ EventDataColumns.EVENT_ANSWER + " TEXT,"
					+ EventDataColumns.PLACE1_VOTES + " INTEGER,"
					+ EventDataColumns.PLACE2_VOTES + " INTEGER,"
					+ EventDataColumns.TIME1_VOTES + " INTEGER,"
					+ EventDataColumns.TIME2_VOTES + " INTEGER,"

					+ EventDataColumns.CREATED_DATE + " INTEGER,"
					+ EventDataColumns.MODIFIED_DATE + " INTEGER"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + EVENTS_BY_ME_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + EVENTS_BY_FRIENDS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + EVENTS_CONFIRMED_TABLE_NAME);

			onCreate(db);
		}
	}

	// A handle for the new DatabaseHelper class is assigned to the mDbHelper class variable,
	// which is used by the rest of the content provider to manipulate the database.
	private DatabaseHelper mDbHelper;

	@Override
	public boolean onCreate() {
		Log.v(TAG, "onCreate()");
		mDbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		Log.v(TAG, "getType()" + uri);

		switch (sUriMatcher.match(uri)) {
		case MY_EVENTS:
		case FRIENDS_EVENTS:
		case CONFIRMED_EVENTS:
			return EventDataColumns.CONTENT_TYPE;

		case MY_EVENT_ID:
		case FRIENDS_EVENT_ID:
		case CONFIRMED_EVENT_ID:
			return EventDataColumns.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Log.v(TAG, "insert()" + uri.toString());

		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		// set created & modified dates
		Long now = Long.valueOf(System.currentTimeMillis());

		// Make sure that the fields are all set
		if (values.containsKey(EventDataColumns.CREATED_DATE) == false) {
			values.put(EventDataColumns.CREATED_DATE, now);
		}

		if (values.containsKey(EventDataColumns.MODIFIED_DATE) == false) {
			values.put(EventDataColumns.MODIFIED_DATE, now);
		}
		//////////////////////////////////

		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		long rowId;
		switch (sUriMatcher.match(uri)) {
		case MY_EVENTS:
			rowId = db.insert(
					EVENTS_BY_ME_TABLE_NAME,		// table
					EventDataColumns.EVENT_NAME,	// nullColumnHack
					values); 						// values
			if (rowId > 0) {
				Uri eventUri = ContentUris.withAppendedId(EventDataColumns.CONTENT_URI_MY_EVENTS, rowId);
				getContext().getContentResolver().notifyChange(eventUri, null);
				return eventUri;
			}
			break;

		case FRIENDS_EVENTS:
			rowId = db.insert(
					EVENTS_BY_FRIENDS_TABLE_NAME,	// table
					EventDataColumns.EVENT_NAME,	// nullColumnHack
					values); 						// values
			if (rowId > 0) {
				Uri eventUri = ContentUris.withAppendedId(EventDataColumns.CONTENT_URI_FRIENDS_EVENTS, rowId);
				getContext().getContentResolver().notifyChange(eventUri, null);
				return eventUri;
			}
			break;

		case CONFIRMED_EVENTS:
			rowId = db.insert(
					EVENTS_CONFIRMED_TABLE_NAME,	// table
					EventDataColumns.EVENT_NAME,	// nullColumnHack
					values); 						// values
			if (rowId > 0) {
				Uri eventUri = ContentUris.withAppendedId(EventDataColumns.CONTENT_URI_CONFIRMED_EVENTS, rowId);
				getContext().getContentResolver().notifyChange(eventUri, null);
				return eventUri;
			}
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.v(TAG, "query()" + uri.toString());

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();


		switch (sUriMatcher.match(uri)) {
		case MY_EVENTS:
			qb.setTables(EVENTS_BY_ME_TABLE_NAME);
			qb.setProjectionMap(sEventsProjectionMap);
			break;

		case MY_EVENT_ID:
			qb.setTables(EVENTS_BY_ME_TABLE_NAME);
			qb.setProjectionMap(sEventsProjectionMap);
			qb.appendWhere(EventDataColumns._ID + "=" + uri.getPathSegments().get(1));
			break;

		case FRIENDS_EVENTS:
			qb.setTables(EVENTS_BY_FRIENDS_TABLE_NAME);
			qb.setProjectionMap(sEventsProjectionMap);
			break;

		case FRIENDS_EVENT_ID:
			qb.setTables(EVENTS_BY_FRIENDS_TABLE_NAME);
			qb.setProjectionMap(sEventsProjectionMap);
			qb.appendWhere(EventDataColumns._ID + "=" + uri.getPathSegments().get(1));
			break;            


		case CONFIRMED_EVENTS:
			qb.setTables(EVENTS_CONFIRMED_TABLE_NAME);
			qb.setProjectionMap(sEventsProjectionMap);
			break;

		case CONFIRMED_EVENT_ID:
			qb.setTables(EVENTS_CONFIRMED_TABLE_NAME);
			qb.setProjectionMap(sEventsProjectionMap);
			qb.appendWhere(EventDataColumns._ID + "=" + uri.getPathSegments().get(1));
			break;
			
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = EventDataColumns.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		
		Log.v(TAG, "update()");
		
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        String eventId;
        
        switch (sUriMatcher.match(uri)) {
        case MY_EVENTS:
            count = db.update(EVENTS_BY_ME_TABLE_NAME, values, where, whereArgs);
            break;

        case MY_EVENT_ID:
            eventId = uri.getPathSegments().get(1);
            count = db.update(EVENTS_BY_ME_TABLE_NAME, values, EventDataColumns._ID + "=" + eventId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        case FRIENDS_EVENTS:
            count = db.update(EVENTS_BY_FRIENDS_TABLE_NAME, values, where, whereArgs);
            break;

        case FRIENDS_EVENT_ID:
            eventId = uri.getPathSegments().get(1);
            count = db.update(EVENTS_BY_FRIENDS_TABLE_NAME, values, EventDataColumns._ID + "=" + eventId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
	}

	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.v(TAG, "delete()");
		int count = 0;
		String eventId;

		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri)) {
		case MY_EVENTS:
			count = db.delete(EVENTS_BY_ME_TABLE_NAME, selection, selectionArgs);
			break;

		case MY_EVENT_ID:
			eventId = uri.getPathSegments().get(1);
			count = db.delete(EVENTS_BY_ME_TABLE_NAME, EventDataColumns._ID + "=" + eventId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		case FRIENDS_EVENTS:
			count = db.delete(EVENTS_BY_FRIENDS_TABLE_NAME, selection, selectionArgs);
			break;
			
		case FRIENDS_EVENT_ID:
			eventId = uri.getPathSegments().get(1);
			count = db.delete(EVENTS_BY_FRIENDS_TABLE_NAME, EventDataColumns._ID + "=" + eventId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		case CONFIRMED_EVENTS:
			count = db.delete(EVENTS_CONFIRMED_TABLE_NAME, selection, selectionArgs);
			break;

		case CONFIRMED_EVENT_ID:
			eventId = uri.getPathSegments().get(1);
			count = db.delete(EVENTS_CONFIRMED_TABLE_NAME, EventDataColumns._ID + "=" + eventId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		Log.v(TAG, "delete() count" + count);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(EventData.AUTHORITY, "my_events", MY_EVENTS);
		sUriMatcher.addURI(EventData.AUTHORITY, "my_events/#", MY_EVENT_ID);

		sUriMatcher.addURI(EventData.AUTHORITY, "friends_events", FRIENDS_EVENTS);
		sUriMatcher.addURI(EventData.AUTHORITY, "friends_events/#", FRIENDS_EVENT_ID);

		sUriMatcher.addURI(EventData.AUTHORITY, "confirmed_events", CONFIRMED_EVENTS);
		sUriMatcher.addURI(EventData.AUTHORITY, "confirmed_events/#", CONFIRMED_EVENT_ID);

		/* the content provider column names and the database column names are identical, 
		 * so the sEventsProjectionMap is not required.  ==> TODO
		 */
		sEventsProjectionMap = new HashMap<String, String>();

		sEventsProjectionMap.put(EventDataColumns._ID, EventDataColumns._ID);
		sEventsProjectionMap.put(EventDataColumns.EVENT_TYPE, EventDataColumns.EVENT_TYPE);
		sEventsProjectionMap.put(EventDataColumns.TIMESTAMP, EventDataColumns.TIMESTAMP);

		// Event data fields
		sEventsProjectionMap.put(EventDataColumns.EVENT_NAME, EventDataColumns.EVENT_NAME);
		sEventsProjectionMap.put(EventDataColumns.PLACE1, EventDataColumns.PLACE1);
		sEventsProjectionMap.put(EventDataColumns.PLACE2, EventDataColumns.PLACE2);
		sEventsProjectionMap.put(EventDataColumns.DATE1, EventDataColumns.DATE1);
		sEventsProjectionMap.put(EventDataColumns.DATE2, EventDataColumns.DATE2);
		sEventsProjectionMap.put(EventDataColumns.TIME1, EventDataColumns.TIME1);
		sEventsProjectionMap.put(EventDataColumns.TIME2, EventDataColumns.TIME2);

		// Event participants
		sEventsProjectionMap.put(EventDataColumns.CONTACT_NAMES, EventDataColumns.CONTACT_NAMES);
		sEventsProjectionMap.put(EventDataColumns.PHONE_NUMBERS, EventDataColumns.PHONE_NUMBERS);
		sEventsProjectionMap.put(EventDataColumns.EVENT_FROM, EventDataColumns.EVENT_FROM);

		// Event voting and answer fields
		sEventsProjectionMap.put(EventDataColumns.EVENT_ANSWER, EventDataColumns.EVENT_ANSWER);
		sEventsProjectionMap.put(EventDataColumns.PLACE1_VOTES, EventDataColumns.PLACE1_VOTES);
		sEventsProjectionMap.put(EventDataColumns.PLACE2_VOTES, EventDataColumns.PLACE2_VOTES);
		sEventsProjectionMap.put(EventDataColumns.TIME1_VOTES, EventDataColumns.TIME1_VOTES);
		sEventsProjectionMap.put(EventDataColumns.TIME2_VOTES, EventDataColumns.TIME2_VOTES);
        
		sEventsProjectionMap.put(EventDataColumns.CREATED_DATE, EventDataColumns.CREATED_DATE);
		sEventsProjectionMap.put(EventDataColumns.MODIFIED_DATE, EventDataColumns.MODIFIED_DATE);
	}

}
