package it.polimi.it.ibeaconoccupancy;

import it.polimi.it.ibeaconoccupancy.helper.DataBaseHelper;
import it.polimi.it.ibeaconoccupancy.services.RangingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.radiusnetworks.ibeacon.IBeaconManager;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

public class LocationActivity extends Activity {
	
	protected static final String TAG = "LocationActivity";
	private BeaconReceiver receiver;
	private HashMap<String, String> beaconLocation;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		receiver = new BeaconReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RangingService.ACTION);
		registerReceiver(receiver, intentFilter);
		
		loadBeaconLocationInfo();
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		TextView textView = (TextView) findViewById(R.id.location_question);
		textView.setText("Where are you?");
		loadDataDB();
     
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_location,
					container, false);
			return rootView;
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/**
	 * Class which handle the message send by the RangingService(information about the beacons in range)
	 *
	 */
	private class BeaconReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {		  

			ArrayList<String> beacons = intent.getStringArrayListExtra("BeaconInfo");
			String strongerBeacon = intent.getExtras().getString("StrongerBeacon");
			String location = beaconLocation.get(strongerBeacon);
			TextView textView = (TextView) findViewById(R.id.location_question);
			
			
			Log.d(TAG, "received location "+strongerBeacon);
			if (location!=null) {
				textView.setText("You are next to "+location);
			}
			
			 
		}
	}
	
	private void loadDataDB(){
		DataBaseHelper myDbHelper = new DataBaseHelper(LocationActivity.this);
		myDbHelper = new DataBaseHelper(this);

		try {

			myDbHelper.createDataBase();
			Log.d(TAG, "DB created");
		} catch (IOException ioe) {

			throw new Error("Unable to create database");
		}

		try {
			myDbHelper.openDataBase();
			Log.d(TAG, "DB opened");
		}catch(SQLException sqle){
			throw sqle;
		}
		SQLiteDatabase myDb = myDbHelper.getReadableDatabase();
		Cursor cursor = myDb.query(myDbHelper.TABLE_ROOMS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(cursor.isAfterLast()==false){
			String room = cursor.getString(1);
			String beacon  = cursor.getString(2);
			beaconLocation.put(beacon, room);
			Log.d(TAG, "Inserting in beaconLocation: room "+room+" beacon "+beacon);
			cursor.moveToNext();
		}
		
 
	}
	
	private void loadBeaconLocationInfo(){
		beaconLocation = new HashMap<String, String>();
		beaconLocation.put("e2c56db5-dffb-48d2-b060-d0f5a71096e000", "PC Andrea");
		beaconLocation.put("e2c56db5-dffb-48d2-b060-d0f5a71096e0035", "Raspberry Fons");
	}
	

}
