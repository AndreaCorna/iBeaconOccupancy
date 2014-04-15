package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.compare.ServerBeaconManager;
import it.polimi.it.ibeaconoccupancy.compare.ServerBeaconManagerImpl;
import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class RangingService extends Service implements IBeaconConsumer{
	
	public final static String ACTION = "BeaconAction";	//used to identify the message sent with the SendBroacast inside notifyActivity method
	protected static final String TAG = "RangingService";
	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private Collection<IBeacon> oldInformation = null;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    
    
    private final ServerBeaconManager sendManager = new ServerBeaconManagerImpl();
	
    @Override
    public void onCreate() {
        
        super.onCreate();
        iBeaconManager.bind(this);
        Log.d(TAG, "Ranging started");
    }
    @Override
    public void onDestroy() {
    	iBeaconManager.unBind(this);
        super.onDestroy();
        Log.d(TAG, "Ranging finished");
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
        @Override 
        public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
            if (iBeacons.size() > 0) {
            		sendManager.beaconToSend(oldInformation, iBeacons,mBluetoothAdapter.getAddress());
            		Log.d(TAG,"Ranging");
            		this.notifyActivity(iBeacons); 
            		
            }
        }
        /**
         * notify the Mainactivity of the presence of the iBeacons found by the ranging
         * @param iBeacons
         */
        private void notifyActivity(Collection<IBeacon> iBeacons){
        	Intent intent = new Intent();
        	intent.setAction(ACTION);
        	List<String> beaconsInfos = new ArrayList<String>();
 	      for (IBeacon iBeacon : iBeacons){
 	    	  beaconsInfos.add(iBeacon.getProximityUuid()+iBeacon.getMajor()+iBeacon.getMinor()+" "+iBeacon.getAccuracy());
 	    	  
 	      }
 	      intent.putStringArrayListExtra("BeaconInfo",(ArrayList<String>) beaconsInfos);	      
 	      sendBroadcast(intent);
        }

        });
        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
        
       
    
    
    }
    
    

    
    
   
}
