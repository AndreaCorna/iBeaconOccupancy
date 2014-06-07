package it.polimi.it.ibeaconoccupancy;

import java.io.IOException;
import java.util.HashMap;

import com.radiusnetworks.ibeacon.IBeaconManager;

import it.polimi.it.ibeaconoccupancy.R;
import it.polimi.it.ibeaconoccupancy.helper.DataBaseHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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

/**
 * This class implements the main activityy of the application
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class MainActivity extends Activity {
	
	
	private Intent intent;
	protected static final String TAG = "MainActivity";
	OnSharedPreferenceChangeListener listener;
	private String bestBeacon = new String();
	private DataBaseHelper myDbHelper;
	private HashMap<String, String> beaconLocation;



	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		verifyBluetooth();
		
		launchMonitoring();
		loadDataDB();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onPause(){
		super.onPause();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	public void onDestroy(){
		super.onDestroy();
		stopService(intent);
	
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	public void launchLocation(View view) {
		Intent myintent = new Intent(this,LocationActivity.class);
		myintent.putExtra("BestBeacon", bestBeacon);
		myintent.putExtra("beaconLocation", beaconLocation);
		
		startActivity(myintent);
	}
	
	/**
	 * The method verifies that the bluetooth device is enabled and is the device has the bluetooth 4.0 hardware
	 */
	private void verifyBluetooth() {

		try {
			if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");			
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
			            System.exit(0);					
					}					
				});
				builder.show();
			}			
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");			
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
		            System.exit(0);					
				}
				
			});
			builder.show();
			
		}
		
	}	
	
	
	
	
	/**
	 * The method sets the BeaconHandler implementation according to settings and starts Monitorin service
	 * @param logicOnClient - true if the logic is on client side, false otherwise
	 */
	private void launchMonitoring(){
		
		intent = new Intent(this, it.polimi.it.ibeaconoccupancy.services.MonitoringService.class);
		startService(intent);
		Log.d(TAG, "start monitoring ");
		
	}
	
		
	/**
	 * Load the room-beacon associations from a sqlite database and put values in the hashmap locationBeacon
	 */
	private void loadDataDB(){
		beaconLocation = new HashMap<String, String>();
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
		@SuppressWarnings("static-access")
		Cursor cursor = myDb.query(myDbHelper.TABLE_ROOMS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(cursor.isAfterLast()==false){
			String room = cursor.getString(1);
			String beacon  = cursor.getString(2);
			beaconLocation.put(beacon.intern(), room);
			Log.d(TAG, "Inserting in beaconLocation: room "+room+" beacon "+beacon);
			cursor.moveToNext();
		}
		cursor.close();
	 
	}

}
