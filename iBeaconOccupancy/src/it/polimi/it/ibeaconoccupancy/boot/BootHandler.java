package it.polimi.it.ibeaconoccupancy.boot;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The class is woken after the boot complete event and starts the background process of the application
 * @extends BroadcastReceiver
 * @see BackgroundService.class
 * @author Andrea Corna - Lorenzo Fontana
 * 
 *
 */
public class BootHandler extends BroadcastReceiver {
	
	/**
	 * The method verifies that the event received is the BOOT COMPLETED action and
	 * ,if it's true, starts the Background service
	 */
	 @Override
	 public void onReceive(Context context, Intent intent) {
		 if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			 Intent pushIntent = new Intent(context, it.polimi.it.ibeaconoccupancy.services.BackgroundService.class);
			 context.startService(pushIntent);
		
		 }
	 }
}
