package it.polimi.it.ibeaconoccupancy;

import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.powersave.BackgroundPowerSaver;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;

import android.app.Application;

public class SaveBattery  extends Application implements BootstrapNotifier {
    private BackgroundPowerSaver backgroundPowerSaver;

    public void onCreate() {
        super.onCreate();
        // Simply constructing this class and holding a reference to it in your custom Application class
        // enables auto battery saving of about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didEnterRegion(Region arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didExitRegion(Region arg0) {
		// TODO Auto-generated method stub
		
	}
}