package it.polimi.it.ibeaconoccupancy;

import it.polimi.it.ibeaconoccupancy.helper.DataBaseHelper;
import it.polimi.it.ibeaconoccupancy.services.RangingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
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
		
		
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		loadDataDB();
		setupLayout();
     
	}
	
	private void setupLayout() {
		TextView textView = (TextView) findViewById(R.id.question);
		textView.setText("Where are you?");
		Button asw1 = (Button) findViewById(R.id.answer1);
		Button asw2 = (Button) findViewById(R.id.answer2);
		Button asw3 = (Button) findViewById(R.id.answer3);
		Button asw4 = (Button) findViewById(R.id.answer4);
		
		ArrayList<String> rooms = new ArrayList<String>(beaconLocation.values());
		Collections.shuffle(rooms);
		
		setButtonText(asw1,0, rooms);
		setButtonText(asw2,1,rooms);
		setButtonText(asw3,2,rooms);
		asw4.setText("Nessuna");

		
	}
	private void setButtonText(Button but, int  index, ArrayList<String> rooms){
		try {
			but.setText(rooms.get(index));
		} catch (IndexOutOfBoundsException e) {
			but.setVisibility(View.GONE);
			Log.d(TAG, "Ci devono essere almeno 3 entry ne database");
		}
		
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
			
			
			 
		}
	}
	
	private void loadDataDB(){
		beaconLocation = new HashMap<String, String>();
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
	
	
	

}
