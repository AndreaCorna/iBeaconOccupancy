package it.polimi.it.ibeaconoccupancy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

public class BackgroundService extends Service implements BootstrapNotifier{

	private static final String TAG = "AndroidProximityReferenceApplication";
    private RegionBootstrap regionBootstrap;


    public void onCreate() {
        super.onCreate();
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
