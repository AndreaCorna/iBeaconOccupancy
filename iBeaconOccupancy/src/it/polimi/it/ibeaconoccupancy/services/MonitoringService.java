package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
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
	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private  HttpHandler httpHand;
	private Intent ranging;
	
	@Override
	public void onCreate() {
        super.onCreate();
		iBeaconManager.bind(this);
		Log.d(TAG, "Starting monitoring");
		httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeacon");
		ranging= new Intent(this,it.polimi.it.ibeaconoccupancy.services.RangingService.class);
	}
	
	@Override
	public void onDestroy() {
		iBeaconManager.unBind(this);
		super.onDestroy();
	}
	

	
	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			
			@Override
			public void didExitRegion(Region arg0) {
				Log.d(TAG, "Exit a region");
				httpHand.postOnMonitoringOut(mBluetoothAdapter.getAddress());
				stopRanging();
				
				
			}
			
			@Override
			public void didEnterRegion(Region arg0) {
				Log.d(TAG, "Enter a region");
				startRanging();
				
			}
			
			@Override
			public void didDetermineStateForRegion(int arg0, Region arg1) {
				// TODO Auto-generated method stub
				
			}
		});
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
	 

}
