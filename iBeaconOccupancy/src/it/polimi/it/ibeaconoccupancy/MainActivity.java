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
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * This class implements the main activity of the application.
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class MainActivity extends Activity {
	
	
	private Intent monitoringIntent;
	protected static final String TAG = "MainActivity";
	private SharedPreferences prefs;
	OnSharedPreferenceChangeListener listener;

	/**
	 * Method called on creation of the activity. In this we set up all preferences and settings.
	 */
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
		boolean logicOnClient = prefs.getBoolean("pref_logic", true);
		boolean sendWithBluetooth = prefs.getBoolean("pref_bluetooth", true);
		launchMonitoring(logicOnClient, sendWithBluetooth);
		
	
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
	
	/**
	 * Method called when the activity is destroyed, it stops also the monitoring activity launched before.
	 */
	public void onDestroy(){
		super.onDestroy();
		//unregisterReceiver(mReceiver);
		stopService(monitoringIntent);
		
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
				Intent enableBtIntent = new Intent(
	             BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBtIntent, 12);
	
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		

	        if(resultCode != RESULT_OK){

	        	finish();
	          
			}
	        SystemClock.sleep(3000);
	   
	}//onActivityResult
	

	
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
		boolean sendWithBluetooth = prefs.getBoolean("pref_bluetooth", true);

	    stopService(monitoringIntent);
	    launchMonitoring(logicOnClient, sendWithBluetooth);
	    }
	    };

	    prefs.registerOnSharedPreferenceChangeListener(listener);
	}
	
	/**
	 * The method sets the BeaconHandler implementation according to settings and starts Monitorin service
	 * @param logicOnClient - true if the logic is on client side, false otherwise
	 */
	private void launchMonitoring(boolean logicOnClient, boolean sendWithBluetooth){
		Log.d(TAG, "launching monitoring "+logicOnClient);
		
		monitoringIntent = new Intent(this, it.polimi.it.ibeaconoccupancy.services.MonitoringService.class);

		if (logicOnClient){
			monitoringIntent.putExtra("BeaconHandler", new FullBeaconHandlerImpl(sendWithBluetooth));
			
		}
		else{
			monitoringIntent.putExtra("BeaconHandler", new MinimalBeaconHandlerImpl(sendWithBluetooth));
		}

		startService(monitoringIntent);
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
	
	
	
	
	/*// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	Log.d(TAG,"-------received bluetooth devices--------");
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            
				// Add the name and address to an array adapter to show in a ListView
	         
	            devices.add(device);
	            for (BluetoothDevice dev : devices) {
	            	Log.d(TAG,"found devices"+dev.getName()+" "+dev);
	            	
	            	
					
				}
	           
	           
	            
	        }
	    }
	};*/


}
