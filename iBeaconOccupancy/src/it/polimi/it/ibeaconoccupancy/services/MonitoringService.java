package it.polimi.it.ibeaconoccupancy.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringService extends Service implements IBeaconConsumer {

	protected static final String TAG = "MonitoringService";
	public static final String ACTION = "MonitoringAction";

	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
	private Intent ranging;
	private static MonitoringService me;

	
	
	@Override
	public void onCreate() {
		me = this;
        super.onCreate();
        iBeaconManager.bind(this);
        Log.d(TAG, "Starting monitoring");
        
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand monitoring");
        
		 Log.d(TAG, "sendManager monitoring");

		ranging= new Intent(this,it.polimi.it.ibeaconoccupancy.services.RangingService.class);
		//iBeaconManager.setBackgroundScanPeriod(3000);
	
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		iBeaconManager.unBind(this);
		Log.d(TAG, "Destoying monitor service");
		stopRanging();
		super.onDestroy();
	}
	
	private void notifyLocationActivity(){
    	Intent intent = new Intent(); 
    	intent.setAction(ACTION);
		intent.putExtra("exitRegion",true);
		sendBroadcast(intent);

	}
	
	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			
			@Override
			public void didExitRegion(Region arg0) {
				Log.d(TAG, "Exit a region");
				stopRanging();
				notifyLocationActivity();
	
			}
			
		
			
			@Override
			public void didEnterRegion(Region arg0) {
				Log.d(TAG, "Enter a region");
				startRanging();
				
			}
			
			@Override
			public void didDetermineStateForRegion(int arg0, Region arg1) {
				
			}
		});
		iBeaconManager.setBackgroundMode(this, true);
		iBeaconManager.setBackgroundScanPeriod(2000);
		iBeaconManager.setBackgroundBetweenScanPeriod(500);
		try {
			iBeaconManager.updateScanPeriods();
			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId",null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
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
