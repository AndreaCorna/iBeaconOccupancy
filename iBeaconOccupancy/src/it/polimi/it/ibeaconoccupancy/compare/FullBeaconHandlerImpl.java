package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class FullBeaconHandlerImpl implements BeaconHandler, Serializable {
	
	private static final long serialVersionUID = -6887364374840188927L;
	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeacon");
	private IBeacon bestBeacon = null;
	private boolean lostBeacon = false;
	private boolean changed = false;
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		Log.d(TAG, "sending beaocn to server in a full logic way");
		IBeacon big = getBestLocation(newInformation);
		httpHand.postOnRanging(big, MAC, 1,big.getRssi());
		
    }

	public IBeacon getBestLocation(Collection<IBeacon> newInformation){
		IBeacon big = null;
		if(bestBeacon != null){
			boolean found = false;
			big = newInformation.iterator().next();
			for (IBeacon iBeacon : newInformation) {
				if(!found && iBeacon.equals(bestBeacon))
					found = true;	
				if(iBeacon.getRssi() > big.getRssi()){
					big = iBeacon;
				}
			}
			if(found){
				if(bestBeacon.equals(big))
					bestBeacon = big;
				else if(changed){
					bestBeacon = big;
					changed = false;
				}else{
					big = bestBeacon;
					changed = true;
				}
					
				lostBeacon = false;
			}else if(!lostBeacon){
				big = bestBeacon;
				lostBeacon = true;
			}else{
				bestBeacon = big;
				lostBeacon = false;
			}
			
		}else{
			big = newInformation.iterator().next();
			for (IBeacon iBeacon : newInformation) {
				if(iBeacon.getRssi() > big.getRssi()){
					big = iBeacon;
				}
			}
			bestBeacon = big;
		}
		Log.d(TAG, " best beacon "+big.getProximityUuid()+big.getMajor()+big.getMinor());
		return big;
	}
    
    @SuppressWarnings({ "null", "unused" })
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