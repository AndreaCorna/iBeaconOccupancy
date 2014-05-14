package it.polimi.it.ibeaconoccupancy;


import com.radiusnetworks.ibeacon.IBeaconManager;


import it.polimi.it.ibeaconoccupancy.compare.FullBeaconHandlerImpl;
import it.polimi.it.ibeaconoccupancy.compare.MinimalBeaconHandlerImpl;
import it.polimi.it.ibeaconoccupancy.services.BackgroundService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	private SharedPreferences prefs;
	OnSharedPreferenceChangeListener listener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		verifyBluetooth();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		registerPreferenceListener();
		
		if(isBackGroundRunning()){
			BackgroundService.getInstance().stopSelf();
		}
		/*if(isMonitoringRunning()){
			MonitoringService.getInstance().stopSelf();
		}*/
		//while(isBackGroundRunning());
		boolean logicOnClient = prefs.getBoolean("pref_logic", true);
		launchMonitoring(logicOnClient);
		
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
	 * Preference listener to handle the different ways we send  informations to the server
	 */
	private void registerPreferenceListener()
	{
	    listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
	    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

	    	
	    Log.d(TAG,"LISTENING! - Pref changed for: " + key + " pref: " +
	    prefs.getBoolean(key, false));
	    
	    boolean logicOnClient = prefs.getBoolean(key, false);
	    stopService(intent);
	    launchMonitoring(logicOnClient);
	    }
	    };

	    prefs.registerOnSharedPreferenceChangeListener(listener);
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

		startService(intent);
		Log.d(TAG, "start monitoring from launch");

		
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
	
	

}
