package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.LocationActivity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;

public class TestService extends Service{
	
	private TimerRequestAnswer requestAnswer;
	private Timer timerTask;
	private Vibrator notifier;
	
	public void onCreate(){
		notifier = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		super.onCreate();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	Intent myintentIntent = new Intent(this,LocationActivity.class);
		myintentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		timerTask = new Timer();
		requestAnswer = new TimerRequestAnswer(myintentIntent, notifier);
		timerTask.schedule(requestAnswer, 6000, 1800000);
		
    	return super.onStartCommand(intent, flags, startId);
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onDestroy(){
		timerTask.cancel();
		super.onDestroy();
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
			startActivity(answerActivity);
			
			
		}
		
	}

}
