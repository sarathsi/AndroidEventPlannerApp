package com.ipc.eventplannerservice;

import android.net.Uri;
import android.provider.BaseColumns;

public final class EventData {

    public static final String AUTHORITY = "com.ipc.eventdata.provider.EventData";

	static final String MSG_TYPE_NEWEVENT = "N";
	static final String MSG_TYPE_ANSWER = "A";
	static final String MSG_TYPE_RESULT = "R";
    
    // This class cannot be instantiated
    private EventData() {}
    
    /**
     * Notes table
     */
    public static final class EventDataColumns implements BaseColumns {
        // This class cannot be instantiated
        private EventDataColumns() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI_MY_EVENTS = Uri.parse("content://" + AUTHORITY + "/my_events");

        public static final Uri CONTENT_URI_FRIENDS_EVENTS = Uri.parse("content://"+ AUTHORITY + "/friends_events");
        
        public static final Uri CONTENT_URI_CONFIRMED_EVENTS = Uri.parse("content://"+ AUTHORITY + "/confirmed_events");

        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of events.
         */
        public static final String CONTENT_TYPE = "ipc.thesis.cursor.dir/ipc.thesis.event";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single event.
         */
        public static final String CONTENT_ITEM_TYPE = "ipc.thesis.cursor.item/ipc.thesis.event";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";


        // Table columns
        public static final String EVENT_TYPE = "event_type";
        public static final String TIMESTAMP = "timestamp";
        
        public static final String EVENT_NAME = "event_name";
        
        public static final String PLACE1 = "place1";
        public static final String PLACE2 = "place2";
        public static final String DATE1 = "date1";
        public static final String DATE2 = "date2";
        public static final String TIME1 = "time1";
        public static final String TIME2 = "time2";
        
        public static final String CONTACT_NAMES = "contact_names";
        public static final String PHONE_NUMBERS = "phone_numbers";
        public static final String EVENT_FROM = "event_from";

        public static final String EVENT_ANSWER = "event_answer"; // "Y" or "N"
        public static final String PLACE1_VOTES = "place1_votes";
        public static final String PLACE2_VOTES = "place2_votes";
        public static final String TIME1_VOTES = "time1_votes";
        public static final String TIME2_VOTES = "time2_votes";
        
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
    }
	
	
}
