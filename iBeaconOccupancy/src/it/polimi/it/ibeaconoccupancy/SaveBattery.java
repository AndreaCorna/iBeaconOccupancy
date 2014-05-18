package it.polimi.it.ibeaconoccupancy;

import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.powersave.BackgroundPowerSaver;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;

import android.app.Application;

/**
 * This class implements methods in order to reduce power consumption.
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class SaveBattery  extends Application implements BootstrapNotifier {
    @SuppressWarnings("unused")
	private BackgroundPowerSaver backgroundPowerSaver;

    /**
     * The method create one object BackgroundPowerSaver, in order to reduce power.
     * @see com.radiusnetworks.proximity.ibeacon.powersave.BackgroundPowerSaver
     */
    public void onCreate() {
        super.onCreate();
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		
	}

	@Override
	public void didEnterRegion(Region arg0) {
		
	}

	@Override
	public void didExitRegion(Region arg0) {
	
	}
}