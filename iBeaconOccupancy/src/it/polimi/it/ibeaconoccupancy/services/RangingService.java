package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.compare.BeaconHandler;
import it.polimi.it.ibeaconoccupancy.helper.LogFileHelper;

import java.util.Collection;

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
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

/**
 * 
 * @author andrea
 *
 */
public class RangingService extends Service implements IBeaconConsumer,SensorEventListener{
	
	public final static String ACTION = "BeaconAction";
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
    
	
    @Override
    public void onCreate() {
        
        super.onCreate();
        iBeaconManager.bind(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		Log.d(TAG, "Ranging started");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	sendManager = (BeaconHandler) intent.getSerializableExtra("BeaconHandler");
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
        	Log.d(TAG,"RAnging ");
        	if (iBeacons.size() > 0) {
            	Log.d(TAG,"Beacons detected");
        		String message;
        		for (IBeacon beacon : iBeacons) {
					message = beacon.getProximityUuid()+beacon.getMajor()+beacon.getMinor()+","+beacon.getAccuracy();
					LogFileHelper.writeLogEntry(message);
				}
        		
            		
            	
            }
        }
        

        });
        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
        iBeaconManager.setBackgroundMode(this, true);
        iBeaconManager.setBackgroundScanPeriod(3000);
        iBeaconManager.setForegroundScanPeriod(2000);
		try {
			iBeaconManager.updateScanPeriods();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
       
    
    
    }
    
    

   	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
   	
   	/**
   	 * The method is called when the system grows up a Sensor event. It controls if the event is 
   	 * related to a modification of the position of the device nofitied by the accelerometer. 
   	 * If it's so, it notifies to Ranging Service to send information about beacons.
   	 */
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
		
	
    
    
   
}
