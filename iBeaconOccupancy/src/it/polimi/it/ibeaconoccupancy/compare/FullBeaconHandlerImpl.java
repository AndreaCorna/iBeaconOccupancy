package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class FullBeaconHandlerImpl extends BeaconHandler {
	
	
	@Override
	public void beaconToSend(Collection<IBeacon> oldInformation,
			Collection<IBeacon> newInformation, String MAC) {
		Log.d(TAG, "sending beaocn to server in a full logic way");
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
		httpHand.postOnMonitoringOut(idBluetooth);

		
	}

	
}