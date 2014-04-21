package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class MinimalBeaconHandlerImpl implements BeaconHandler,Serializable {
	

	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler("http://192.168.0.152/ibeaconserver");

	@Override
	public void beaconToSend(Collection<IBeacon> oldInformation,
			Collection<IBeacon> newInformation, String MAC) {
		for (IBeacon iBeacon : newInformation) {
    		httpHand.postOnRanging(iBeacon, MAC, 1,iBeacon.getRssi());
		}
      	
		
	}
    
    @SuppressWarnings("null")
	private void deleteFromOld(Collection<IBeacon> oldBeacons, Collection<IBeacon> newBeacons){
    	Collection<IBeacon> toDelete = null;
    	boolean found = false;
    	for (IBeacon old : oldBeacons) {
    		found = false;
    		for (IBeacon iBeacon : newBeacons) {
    			if(old.getProximityUuid().equals(iBeacon.getProximityUuid()) &&
						old.getMajor() == iBeacon.getMajor() && old.getMinor() == iBeacon.getMinor()){
    				found = true;
    				break;
    			}
			}
    		Log.d(TAG,""+found);
    		if(!found){
    			toDelete.add(old);
    		}
		}
    	oldBeacons.clear();
    	oldBeacons = toDelete;
    }

	@Override
	public void exitingRegion(String idBluetooth) {
		httpHand.postOnMonitoringOut( idBluetooth);
		
	}

	
}
