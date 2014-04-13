package it.polimi.it.ibeaconoccupancy.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringService extends Service implements IBeaconConsumer {

	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
	private  HttpHandler httpHand;
	
	@Override
	public void onCreate() {
		iBeaconManager.bind(this);
		Log.e("Status", "Starting monitoring");
		httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeacon");
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		iBeaconManager.unBind(this);
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			
			@Override
			public void didExitRegion(Region arg0) {
				Log.e("Status", "Exit a region");
				httpHand.postOnMonitoringOut();
				
				
				
			}
			
			@Override
			public void didEnterRegion(Region arg0) {
				Log.e("Status", "Enter a region");
				startRanging();
				
			}
			
			@Override
			public void didDetermineStateForRegion(int arg0, Region arg1) {
				// TODO Auto-generated method stub
				
			}
		});

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void startRanging(){
		Intent intent = new Intent(this, RangingService.class);
		startService(intent);
	}
	
	private void stopRanging() {
		Intent intent = new Intent(this, RangingService.class);
		stopService(intent);

	}
	 

}
