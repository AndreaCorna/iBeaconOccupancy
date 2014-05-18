package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.SaveBattery;
import it.polimi.it.ibeaconoccupancy.compare.BeaconHandler;
import it.polimi.it.ibeaconoccupancy.compare.FullBeaconHandlerImpl;
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
    private  BeaconHandler sendManager = new FullBeaconHandlerImpl();
	private Intent ranging;
	private static MonitoringService me;
	@SuppressWarnings("unused")
	private SaveBattery save;
	
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
        if(intent != null){
        if((BeaconHandler) intent.getSerializableExtra("BeaconHandler") != null)
        	sendManager = (BeaconHandler) intent.getSerializableExtra("BeaconHandler");		
		if (intent.getSerializableExtra("BeaconHandler")==null) {
			Log.d(TAG,"fewjgirtngirt");
			sendManager = new FullBeaconHandlerImpl();
		}}
		 Log.d(TAG, "sendManager monitoring");

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
			iBeaconManager.updateScanPeriods();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
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
