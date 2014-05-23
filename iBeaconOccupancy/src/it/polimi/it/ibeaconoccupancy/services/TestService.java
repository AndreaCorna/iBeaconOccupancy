package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.LocationActivity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class TestService extends Service{
	
	private TimerRequestAnswer requestAnswer;
	private Timer timerTask;
	private Vibrator notifier;
	private String bestBeacon;
	private BeaconReceiver receiver;
	protected static final String TAG = "TestService";
	private HashMap<String, String> beaconLocation;
	private static TestService me;
	
	
	public void onCreate(){
		notifier = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		me = this;
		super.onCreate();
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	Intent myintentIntent = new Intent(this,LocationActivity.class);
		myintentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		timerTask = new Timer();
		requestAnswer = new TimerRequestAnswer(myintentIntent, notifier);
		timerTask.schedule(requestAnswer, 6000, 1800000);
		bestBeacon = (String)intent.getSerializableExtra("BestBeacon");
		beaconLocation = (HashMap<String, String>) intent.getSerializableExtra("beaconLocation");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RangingService.ACTION);
		intentFilter.addAction(MonitoringService.ACTION);
		receiver =  new BeaconReceiver();
		registerReceiver(receiver, intentFilter);
		
    	return super.onStartCommand(intent, flags, startId);
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onDestroy(){
		timerTask.cancel();
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	public static TestService getInstance(){
		return me;
	}
	
	/**
	 * This class implements a timertask that create a new activity in order to request to the user to 
	 * give an answer related to seen beacon.
	 * @author andrea
	 *
	 */
	private class TimerRequestAnswer extends TimerTask{
		Intent answerActivity;
		Vibrator notifier;
		
		public TimerRequestAnswer(Intent answerActivity, Vibrator notifier){
			this.answerActivity = answerActivity;
			this.notifier = notifier;
		}
		@Override
		public void run() {
			long[] pattern = {0, 500, 300, 500};
			notifier.vibrate(pattern,-1);
			answerActivity.putExtra("BestBeacon", bestBeacon);
			answerActivity.putExtra("beaconLocation", beaconLocation);
			startActivity(answerActivity);
			
			
		}
		
	}
	
	/**
	 * Class which receive the message sent by the RangingService(information about the beacons in range) and set the bestBeacon attribute 
	 *
	 */
	private class BeaconReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {		  
			if (intent.getExtras().getBoolean("exitRegion")){
				bestBeacon=null;
			}
			else {
				@SuppressWarnings("unused")
				ArrayList<String> beacons = intent.getStringArrayListExtra("BeaconInfo");
				String strongerBeacon = intent.getExtras().getString("StrongerBeacon");
				bestBeacon = strongerBeacon;
				Log.d(TAG, "on Receive strong beacon "+bestBeacon);
				
			}
			
			
			
			 
		}
	}
	
	

	

}
