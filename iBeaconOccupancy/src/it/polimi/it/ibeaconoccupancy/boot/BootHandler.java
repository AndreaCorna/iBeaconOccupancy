package it.polimi.it.ibeaconoccupancy.boot;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The class is woken after the boot complete event and starts the background process
 * @extends BroadcastReceiver
 * @author Andrea Corna - Lorenzo Fontana
 * 
 *
 */
public class BootHandler extends BroadcastReceiver {
	 @Override
	 public void onReceive(Context context, Intent intent) {
		 if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			 Intent pushIntent = new Intent(context, it.polimi.it.ibeaconoccupancy.services.BackgroundService.class);
			 context.startService(pushIntent);
		
		 }
	 }
}
