package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.compare.BeaconHandler;
import it.polimi.it.ibeaconoccupancy.compare.ProximityHandlerImpl;
import it.polimi.it.ibeaconoccupancy.compare.MachineLearningHandlerImpl;
import it.polimi.it.ibeaconoccupancy.helper.SaveBattery;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringService extends Service implements IBeaconConsumer {

	protected static final String TAG = "MonitoringService";
	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private  BeaconHandler sendManager;
	private Intent ranging;
	private static MonitoringService me;
	@SuppressWarnings("unused")
	private SaveBattery save;
	private SharedPreferences prefs;

	
	@Override
	public void onCreate() {
		me = this;
        super.onCreate();
        iBeaconManager.bind(this);
        Log.d(TAG, "Starting monitoring");
        
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean logicOnClient = prefs.getBoolean("pref_logic", true);
		boolean sendWithBluetooth = prefs.getBoolean("pref_bluetooth", true);
        Log.d(TAG, "onStartCommand monitoring");
        if(logicOnClient){
        	sendManager = new ProximityHandlerImpl(sendWithBluetooth);
        }else{
        	sendManager = new MachineLearningHandlerImpl(sendWithBluetooth);
        }
		 
		ranging= new Intent(this,it.polimi.it.ibeaconoccupancy.services.RangingService.class);
		ranging.putExtra("BeaconHandler", sendManager);
		save = new SaveBattery();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		iBeaconManager.unBind(this);
		Log.d(TAG, "Destoying monitor service");
		stopRanging();
		//BluetoothHelper.getInstance().stopDiscovery();
		super.onDestroy();
	}
	

	
	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			
			@Override
			public void didExitRegion(Region arg0) {
				Log.d(TAG, "Exit a region");
				sendManager.exitingRegion(mBluetoothAdapter.getAddress());
				stopRanging();
				
				
			}
			
			@Override
			public void didEnterRegion(Region arg0) {
				mBluetoothAdapter.startDiscovery();
				Log.d(TAG, "Enter a region");
				startRanging();
			}
			
			@Override
			public void didDetermineStateForRegion(int arg0, Region arg1) {
				// TODO Auto-generated method stub
				
			}
		});
		iBeaconManager.setBackgroundMode(this, true);
		iBeaconManager.setBackgroundScanPeriod(2000);
		iBeaconManager.setBackgroundBetweenScanPeriod(500);
		try {
			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId",null, null, null));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void startRanging(){
		
		startService(ranging);
	}
	
	private void stopRanging() {
		
		stopService(ranging);

	}
	 
	public static MonitoringService getInstance(){
		return me;
	}

}
