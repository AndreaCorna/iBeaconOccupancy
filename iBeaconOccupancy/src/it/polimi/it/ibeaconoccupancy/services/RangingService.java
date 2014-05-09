package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.LocationActivity;
import it.polimi.it.ibeaconoccupancy.compare.BeaconHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class RangingService extends Service implements IBeaconConsumer,SensorEventListener{
	
	public final static String ACTION = "BeaconAction";	//used to identify the message sent with the SendBroacast inside notifyActivity method
	protected static final String TAG = "RangingService";
	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    
    private  BeaconHandler sendManager;
    private SensorManager mSensorManager; 
    private Sensor mAccelerometer;
    private boolean isMoving = false;
    private float last_x,last_y,last_z;
    private static final int SHAKE_THRESHOLD = 300;
    private long lastUpdate;
    
    private TimerRequestAnswer requestAnswer;
	private Timer timerTask;
	private Vibrator notifier;
    
	
    @Override
    public void onCreate() {
        
        super.onCreate();
        iBeaconManager.bind(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //iBeaconManager.setBackgroundMode(this, true);
		//iBeaconManager.setBackgroundScanPeriod(3000);
        notifier = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		Log.d(TAG, "Ranging started");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	sendManager = (BeaconHandler) intent.getSerializableExtra("BeaconHandler");
    	
    	Intent myintentIntent = new Intent(this,LocationActivity.class);
		myintentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		timerTask = new Timer();
		requestAnswer = new TimerRequestAnswer(myintentIntent, notifier);
		timerTask.schedule(requestAnswer, 6000, 60000);
		
    	return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
    	iBeaconManager.unBind(this);
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "Ranging finished");
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void restore(){
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
        @Override 
        public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
            if (iBeacons.size() > 0) {
            	if(true){
            		sendManager.beaconToSend(iBeacons,mBluetoothAdapter.getAddress());
            		Log.d(TAG,"Ranging");
            		this.notifyActivity(iBeacons); 
            		
            		isMoving = false;
            		restore();
            		
            	}
            }
        }
        /**
         * notify the Mainactivity of the presence of the iBeacons found by the ranging and which is the one with the strongest power
         * @param iBeacons
         */
        private void notifyActivity(Collection<IBeacon> iBeacons){
        	Intent intent = new Intent();
        	intent.setAction(ACTION);
        	List<String> beaconsInfos = new ArrayList<String>();
        	IBeacon strongerBeacon = null;
        	for (IBeacon iBeacon : iBeacons){
        		if (strongerBeacon==null || strongerBeacon.getRssi()<iBeacon.getRssi()){
        			strongerBeacon = iBeacon;
        		}
        		beaconsInfos.add(iBeacon.getProximityUuid()+iBeacon.getMajor()+iBeacon.getMinor());
 	    	  
 	    	  
 	      }
 	      intent.putStringArrayListExtra("BeaconInfo",(ArrayList<String>) beaconsInfos);
 	      if (strongerBeacon !=null){
 	    	  Log.d(TAG, "notifyingLocationActivity "+strongerBeacon.getProximityUuid()+strongerBeacon.getMajor()+strongerBeacon.getMinor());
 	    	  intent.putExtra("StrongerBeacon", ""+strongerBeacon.getProximityUuid()+strongerBeacon.getMajor()+strongerBeacon.getMinor());

 	      }
 	      else {
 	    	 Log.d(TAG, "notifyingLocationActivity nullStronger Beacon");
 	    	 intent.putExtra("StrongerBeacon","");
		}
 	      sendBroadcast(intent);
        }

        });
        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
        
       
    
    
    }
    
    

   	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		 Sensor mySensor = event.sensor;
		 if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	        float x = event.values[0];
	        float y = event.values[1];
	        float z = event.values[2];
	        long curTime = System.currentTimeMillis();
	        
	        if ((curTime - lastUpdate) > 100) {
	            long diffTime = (curTime - lastUpdate);
	            lastUpdate = curTime;
	        
	            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
	            if (speed > SHAKE_THRESHOLD) {
		            Log.d("HTTP", "listener");
					isMoving = true;
		            last_x = x;
		            last_y = y;
		            last_z = z;
		        }
	        }
	    }
			
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
