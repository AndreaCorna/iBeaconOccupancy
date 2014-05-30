package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.Constants;
import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class FullBeaconHandlerImpl implements BeaconHandler, Serializable {
	
	private static final long serialVersionUID = -6887364374840188927L;
	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler(Constants.ADDRESS_LOGIC_ON_CLIENT);
	private IBeacon bestBeacon = null;
	private boolean lostBeacon = false;
	private boolean changed = false;
	HashMap<IBeacon, Double> beaconProximity = new HashMap<IBeacon, Double>();
	
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		Log.d(TAG, "sending beaocn to server in a full logic way");
		IBeacon big = getBestLocation(newInformation);
		httpHand.postOnRanging(big, MAC, 1,big.getRssi());
		
		
    }
	
	
	
	public IBeacon getBestLocation(Collection<IBeacon> newInformation){
		Double coefficent = 0.8;
		boolean found = false;
		//checking if in new beacons there is the old best one
		for (IBeacon iBeacon : newInformation) {
			//Log.d(TAG, "Old"+bestBeacon+"new "+iBeacon);
			if (iBeacon.equals(bestBeacon)){
				
				found=true;
			}
		}
		//old best beacon found in newInformatio
		if (found){
			//Log.d(TAG, "Old best beacon found");
			lostBeacon=false;
		}		
		
		//not found old best beacon in newInformation and already having lost it before
		if (!found && lostBeacon) {
			Log.d(TAG, "Removing old best beacon ");
			try {
				beaconProximity.remove(bestBeacon);
				lostBeacon=false;
			} catch (Exception e) {
				Log.d(TAG, "first run");
			}
			
		}
		//not found but first time I have missed it
		if (!found && !lostBeacon) {
			Log.d(TAG, "Lost the best beacon");
			lostBeacon=true;
		}
		for (IBeacon iBeacon : newInformation) {
			Log.d(TAG,"minor: "+iBeacon.getMinor()+" accuracy: "+iBeacon.getAccuracy());
		}
		
		for (IBeacon iBeacon : newInformation) {
		
			Double current_value = beaconProximity.get(iBeacon);
			if (current_value ==null){
				current_value = iBeacon.getAccuracy();
			}
			//Log.d(TAG, getUUIDMaiorMinor(iBeacon)+" current value"+current_value+" accuracy:"+iBeacon.getAccuracy());
			Double new_value = current_value*coefficent+(1-coefficent)*iBeacon.getAccuracy();
			beaconProximity.put(iBeacon, new_value);
			//Log.d(TAG, "updated hashmap "+getUUIDMaiorMinor(iBeacon)+" "+new_value);
		}
		Log.d(TAG,"---------------------------------------------");
		for (IBeacon iBeacon : beaconProximity.keySet()) {
			
			Log.d(TAG,"hashmap: "+iBeacon.getMinor()+" accuracy "+beaconProximity.get(iBeacon));
		}
		
		Map.Entry<IBeacon, Double> minEntry = null;

		for (Map.Entry<IBeacon, Double> entry : beaconProximity.entrySet())
		{
		    if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
		    {
		    	minEntry = entry;
		    }
		}
		
		Log.d(TAG,"Best Beacon"+minEntry.getKey().getMinor());
		if(bestBeacon != null && !bestBeacon.equals(minEntry))
			lostBeacon = false;
		bestBeacon = minEntry.getKey();
		return minEntry.getKey();
		
		
	}
	
	private String getUUIDMaiorMinor(IBeacon bestBeacon){
		return ""+bestBeacon.getMinor();
	}

	public IBeacon getBestLocationV1(Collection<IBeacon> newInformation){
		IBeacon big = null;
		if(bestBeacon != null){
			boolean found = false;
			big = newInformation.iterator().next();
			Log.d(TAG," start search in beacon");
			for (IBeacon iBeacon : newInformation) {
				if(!found && iBeacon.equals(bestBeacon))
					found = true;
				Log.d(TAG,"beacon "+iBeacon.getProximityUuid()+iBeacon.getMajor()+iBeacon.getMinor()+" Accuracy "+iBeacon.getAccuracy()+ "\n best "+bestBeacon.getAccuracy()+" "+bestBeacon.getProximityUuid()+bestBeacon.getMajor()+bestBeacon.getMinor());
				if(iBeacon.getAccuracy() <big.getAccuracy()){
					big = iBeacon;
				}
			}
			Log.d(TAG,"best Beacon "+big.getProximityUuid()+big.getMajor()+big.getMinor());
			Log.d(TAG,"found "+found);
			if(found){
				if(bestBeacon.equals(big)){
					Log.d(TAG,"best prima �� uguale a best adesso");
					bestBeacon = big;
				}else if(changed){
					Log.d(TAG,"best prima �� cambiato per la seconda volta");
					bestBeacon = big;
					changed = false;
				}else{
					Log.d(TAG,"best prima �� cambiato per la prima volta");
					big = bestBeacon;
					changed = true;
				}
				
				lostBeacon = false;
			}else if(!lostBeacon){
				Log.d(TAG,"ho perso il beacon per la prima volta");
				big = bestBeacon;
				lostBeacon = true;
			}else{
				Log.d(TAG,"ho perso il beacon per la seconda volta");
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