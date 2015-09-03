package com.ipc.thesis.android;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class Utils {

	public static String arrayListToString(ArrayList<String> arrayList, String separator) {
		StringBuffer result = new StringBuffer();

		if (arrayList.size() > 0) {
			result.append(arrayList.get(0));
			for (int i = 1; i < arrayList.size(); i++) {
				result.append(separator);
				result.append(arrayList.get(i));
			}
		}

		return result.toString();
	}
	
	public static String arrayToString(String[] a, String separator) {
		StringBuffer result = new StringBuffer();

		if (a.length > 0) {
			result.append(a[0]);
			for (int i=1; i<a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}

		return result.toString();
	}

	// split the string into string array
	private static String REGEX = ":"; // separator
	private static String INPUT = "one:two:three:four:five"; // input

	public static void splitStringToArray(String input, String separator) {
		Pattern p = Pattern.compile(separator);
		String[] items = p.split(input);
		
		for (int i = 0; i < items.length; i++) {
			System.out.println(items[i]);
		}
	}
	
	public static void printStringArray(String[] array)
	{
		int size = array.length;
		for (int i=0; i<size; i++)
		{
			System.out.println(array[i]);
		}
	}

	
    
    
/******** TODO List *********/
// Trace Logging method
//
//
	
	/*
	// AIDL - IPC
	Three steps to creating and using remote methods in Android:
		1. Define the interface in the AIDL.
		2. Implement the interface. That is, write methods that match the signatures in the interface and that perform the operations you want in the program that provides the desired services.
		3. Invoke the methods where you want to use them.
		
		
		
		*/

	/*
	<service android:name="EventPlannerService" android:process=":remote">
	<intent-filter>
		<action android:name="com.ipc.service.IEventPlannerService" />
	</intent-filter>
</service>
*/

	/*
	http://developer.android.com/reference/android/content/Context.html#BIND_AUTO_CREATE
	
	int 	BIND_ABOVE_CLIENT 	Flag for bindService(Intent, ServiceConnection, int): indicates that the client application binding to this service considers the service to be more important than the app itself.
	int 	BIND_ADJUST_WITH_ACTIVITY 	Flag for bindService(Intent, ServiceConnection, int): If binding from an activity, allow the target service's process importance to be raised based on whether the activity is visible to the user, regardless whether another flag is used to reduce the amount that the client process's overall importance is used to impact it.
	int 	BIND_ALLOW_OOM_MANAGEMENT 	Flag for bindService(Intent, ServiceConnection, int): allow the process hosting the bound service to go through its normal memory management.
	int 	BIND_AUTO_CREATE 	Flag for bindService(Intent, ServiceConnection, int): automatically create the service as long as the binding exists.
	int 	BIND_DEBUG_UNBIND 	Flag for bindService(Intent, ServiceConnection, int): include debugging help for mismatched calls to unbind.
	int 	BIND_IMPORTANT 	Flag for bindService(Intent, ServiceConnection, int): this service is very important to the client, so should be brought to the foreground process level when the client is.
	int 	BIND_NOT_FOREGROUND 	Flag for bindService(Intent, ServiceConnection, int): don't allow this binding to raise the target service's process to the foreground scheduling priority.
	
	*/

	
	
	/* 
	<?xml version="1.0" encoding="utf-8"?>

	<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
	    android:orientation="vertical"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent">

	    <LinearLayout
	        android:orientation="vertical"
	        android:layout_width="match_parent"
	        android:layout_height="wrap_content">
	    
	        <Button android:id="@+id/button"
	            android:text="@string/controls_1_save"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"/>
	    
	        <EditText android:id="@+id/edit"
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"/>
	            
	        <CheckBox android:id="@+id/check1"
	            android:paddingBottom="24sp"
		        android:paddingTop="24sp"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"
	            android:text="@string/controls_1_checkbox_1" />
	    
	        <CheckBox android:id="@+id/check2"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"
	            android:text="@string/controls_1_checkbox_2" />
	    
	        <RadioGroup
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:orientation="vertical">
	    
	            <RadioButton android:id="@+id/radio1"
	                android:layout_width="wrap_content"
	                android:layout_height="wrap_content"
	                android:text="@string/controls_1_radiobutton_1" />
	    
	            <RadioButton android:id="@+id/radio2"
	                android:layout_width="wrap_content"
	                android:layout_height="wrap_content"
	                android:text="@string/controls_1_radiobutton_2" />
	    
	        </RadioGroup>
	    
	        <CheckBox android:id="@+id/star"
	            style="?android:attr/starStyle"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"
	            android:text="@string/controls_1_star" />
	                            
	        <ToggleButton android:id="@+id/toggle1"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content" />
	   
	        <ToggleButton android:id="@+id/toggle2"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content" />
	             
	        <Spinner android:id="@+id/spinner1"
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:drawSelectorOnTop="true"
	        />

	        <TextView
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:layout_marginTop="5dip"
	            android:text="@string/textColorPrimary"
	            android:textAppearance="?android:attr/textAppearanceLarge"
	            android:focusable="true"
	        />

	        <TextView
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:layout_marginTop="5dip"
	            android:text="@string/textColorSecondary"
	            android:textAppearance="?android:attr/textAppearanceLarge"
	            android:textColor="?android:attr/textColorSecondary"
	            android:focusable="true"
	        />

	        <TextView
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:layout_marginTop="5dip"
	            android:text="@string/textColorTertiary"
	            android:textAppearance="?android:attr/textAppearanceLarge"
	            android:textColor="?android:attr/textColorTertiary"
	            android:focusable="true"
	        />

	        <TextView
	            style="?android:attr/listSeparatorTextViewStyle"
	            android:text="@string/listSeparatorTextViewStyle"
	            android:layout_marginTop="5dip"
	        />
	        
	    </LinearLayout>

	</ScrollView>

	    */
	
}
