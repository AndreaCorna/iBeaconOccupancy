package it.polimi.it.ibeaconoccupancy.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

public class BackgroundService extends Service implements BootstrapNotifier{

	private static final String TAG = "AndroidProximityReferenceApplication";
    private RegionBootstrap regionBootstrap;
    private BluetoothAdapter adapter;


    public void onCreate() {
        super.onCreate();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
        	adapter.enable();
        	SystemClock.sleep(5000);
        }
        Region region = new Region("ciao",null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        Intent intent = new Intent(this, it.polimi.it.ibeaconoccupancy.services.MonitoringService.class);
		startService(intent);
    }
	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didEnterRegion(Region arg0) {
		
		
	}

	@Override
	public void didExitRegion(Region arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
