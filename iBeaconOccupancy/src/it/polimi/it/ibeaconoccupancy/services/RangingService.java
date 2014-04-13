package it.polimi.it.ibeaconoccupancy.services;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

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
	
	protected static final String TAG = "RangingService";
	private final IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private Collection<IBeacon> oldInformation = null;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final HttpHandler httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeacon");
	
    @Override
    public void onStart(Intent intent, int startId) {
        
        super.onStart(intent, startId);
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
            		compareInformation(iBeacons);
            		Log.d(TAG,"WE LOVE SANTA CLAUS");
            }
        }

        });
        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    
    private void compareInformation(Collection<IBeacon> newInformation){
    	for (IBeacon iBeacon : newInformation) {
    		httpHand.postOnRanging(iBeacon, mBluetoothAdapter.getAddress(), 1);
    
		}
    	if(oldInformation != null){
	    	oldInformation.removeAll(newInformation);
	    	if(oldInformation.size()>0){
		    	for (IBeacon iBeacon : oldInformation) {
		    		httpHand.postOnRanging(iBeacon, mBluetoothAdapter.getAddress(), 0);
				}
		    }
    	}
	    oldInformation = newInformation;
    	
    }

   
}
