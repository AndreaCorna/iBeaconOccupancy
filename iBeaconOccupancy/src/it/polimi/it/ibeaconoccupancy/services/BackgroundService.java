package it.polimi.it.ibeaconoccupancy.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

/**
 * This class implements a service launched after boot completed event that waits for entering in a 
 * ibeacon region and launches the monitoring service.
 * @author Andrea Corna - Lorenzo Fontana
 * 
 *
 */
public class BackgroundService extends Service implements BootstrapNotifier{

	private static final String TAG = "Background Service";
    @SuppressWarnings("unused")
	private RegionBootstrap regionBootstrap;
    private BluetoothAdapter adapter;
    private static BackgroundService me;
    private Intent monitoring;

    /**
     * The method enables the bluetooth adapter if it's not enabled yet and launches the background scan service
     * in order to capture beacon regions.
     */
    public void onCreate() {
        super.onCreate();
        me = this;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
        	adapter.enable();
        	SystemClock.sleep(6000);
        }
        Region region = new Region("ciao",null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        monitoring = new Intent(this, it.polimi.it.ibeaconoccupancy.services.MonitoringService.class);
        Log.d(TAG,"Create background");
		
    }
	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Starts the monitoring services
	 * @see MonitoringService.java
	 */
	@Override
	public void didEnterRegion(Region arg0) {
		startService(monitoring);
		
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

	/**
	 * Stops the monitoring service launched at boot completed and exits.
	 */
	public void onDestroy(){
		super.onDestroy();
		stopService(monitoring);
	}
}
