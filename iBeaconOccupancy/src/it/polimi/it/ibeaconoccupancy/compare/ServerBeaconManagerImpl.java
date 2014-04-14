package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconDataNotifier;

public class ServerBeaconManagerImpl implements ServerBeaconManager {
	
	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeacon");

	@Override
	public void beaconToSend(Collection<IBeacon> oldInformation,
			Collection<IBeacon> newInformation, String MAC) {
		for (IBeacon iBeacon : newInformation) {
    		httpHand.postOnRanging(iBeacon, MAC, 1,iBeacon.getRssi());
		}
    	if(oldInformation != null){
    		deleteFromOld(oldInformation,newInformation);
	    	if(oldInformation.size()>0){
	    		
		    	for (IBeacon iBeacon : oldInformation) {
		    		httpHand.postOnRanging(iBeacon, MAC, 0,0);
				}
		    }
    	}
	    oldInformation = newInformation;
    	
		
	}
    
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

	
}
