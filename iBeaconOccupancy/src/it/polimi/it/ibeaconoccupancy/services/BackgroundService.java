package it.polimi.it.ibeaconoccupancy.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

public class BackgroundService extends Service implements BootstrapNotifier{

	@SuppressWarnings("unused")
	private static final String TAG = "Background Service";
    @SuppressWarnings("unused")
	private RegionBootstrap regionBootstrap;
    private BluetoothAdapter adapter;
    private static BackgroundService me;
    private Intent monitoring;

    public void onCreate() {
        super.onCreate();
        me = this;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
        	adapter.enable();
        	SystemClock.sleep(5000);
        }
        Region region = new Region("ciao",null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        monitoring = new Intent(this, it.polimi.it.ibeaconoccupancy.services.MonitoringService.class);
		startService(monitoring);
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
	
	public static BackgroundService getInstance(){
		return me;
	}

	public void onDestroy(){
		super.onDestroy();
		stopService(monitoring);
	}
}
