package it.polimi.it.ibeaconoccupancy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.radiusnetworks.ibeacon.IBeaconManager;

import it.polimi.it.ibeaconoccupancy.R;
import it.polimi.it.ibeaconoccupancy.compare.FullBeaconHandlerImpl;
import it.polimi.it.ibeaconoccupancy.compare.MinimalBeaconHandlerImpl;
import it.polimi.it.ibeaconoccupancy.helper.DataBaseHelper;
import it.polimi.it.ibeaconoccupancy.helper.SettingsActivity;
import it.polimi.it.ibeaconoccupancy.services.BackgroundService;
import it.polimi.it.ibeaconoccupancy.services.RangingService;
import it.polimi.it.ibeaconoccupancy.services.TestService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * This class implements the main activityy of the application
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class MainActivity extends Activity {
	
	
	private Intent intent;
	private Intent testService;
	protected static final String TAG = "MainActivity";
	private SharedPreferences prefs;
	OnSharedPreferenceChangeListener listener;
	private BeaconReceiver receiver;
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
		receiver = new BeaconReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RangingService.ACTION);
		registerReceiver(receiver, intentFilter);
		verifyBluetooth();
		
		receiver = new BeaconReceiver();

		registerReceiver(receiver, intentFilter);
		
		if(isBackGroundRunning()){
			BackgroundService.getInstance().stopSelf();
		}
		
		
		launchMonitoring(true);
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
		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			 Intent i = new Intent(this, SettingsActivity.class);
			 startActivity(i);			 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onDestroy(){
		super.onDestroy();
		stopService(intent);
		unregisterReceiver(receiver);
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
	
	public void startTestService(View view){
		testService = new Intent(this, it.polimi.it.ibeaconoccupancy.services.TestService.class);
		testService.putExtra("BestBeacon", bestBeacon);
		testService.putExtra("beaconLocation", beaconLocation);
		startService(testService);
	}
	
	public void stopTestService(View view) {
		if (isTestRunning()){
			
			if(testService != null)
				stopService(testService);
			else
				TestService.getInstance().stopSelf();
		}
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
	private void launchMonitoring(boolean logicOnClient){
		Log.d(TAG, "launching monitoring "+logicOnClient);
		
		intent = new Intent(this, it.polimi.it.ibeaconoccupancy.services.MonitoringService.class);

		if (logicOnClient){
			intent.putExtra("BeaconHandler", new FullBeaconHandlerImpl());
			
		}
		else{
			intent.putExtra("BeaconHandler", new MinimalBeaconHandlerImpl());
		}
		
		Log.d(TAG, "intent monitoring ");
		Log.d(TAG, "extra monitoring ");
		startService(intent);
		Log.d(TAG, "start monitoring ");

		
	}
	
	/**
	 * The method controls is the Monitoring service is already active
	 * @return true if is active, otherwise false
	 */
	private boolean isTestRunning() {
		  ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		  for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		    if (TestService.class.getName().equals(service.service.getClassName())) {
		    	return true;
		    }
		  }
		  return false;
	}
	
	/**
	 * The method controls is the Background service is already active
	 * @return true if is active, otherwise false
	 */
	private boolean isBackGroundRunning() {
		  ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		  for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		    if (BackgroundService.class.getName().equals(service.service.getClassName())) {
		    	return true;
		    }
		  }
		  return false;
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


	/**
	 * Class which receive the message sent by the RangingService(information about the beacons in range) and set the bestBeacon attribute 
	 *
	 */
	private class BeaconReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {		  
			if (intent.getExtras().getBoolean("exitRegion")){
				bestBeacon=null;
			}
			else {
				ArrayList<String> beacons = intent.getStringArrayListExtra("BeaconInfo");
				String strongerBeacon = intent.getExtras().getString("StrongerBeacon");
				bestBeacon = strongerBeacon;
				Log.d(TAG, "on Receive strong beacon "+bestBeacon);
				TextView textView = (TextView) findViewById(R.id.my_room_text);

				if(strongerBeacon!=null){
					String room = beaconLocation.get(strongerBeacon);
					Log.d(TAG, "on Receive room "+room);
					Log.d(TAG, "in beacon locationtable"+ beaconLocation.get("e2c56db5-dffb-48d2-b060-d0f5a71096e000"));
					textView.setText(room);	
				}
				else {
					textView.setText("Nessuna");	

				}
				
			}
			
			
			
			 
		}
	}
	
	
	
	

}
