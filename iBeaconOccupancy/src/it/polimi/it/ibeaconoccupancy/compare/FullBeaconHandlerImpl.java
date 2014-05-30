package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.Constants;
import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	ConcurrentHashMap<IBeacon, Boolean> beaconStatus = new ConcurrentHashMap<IBeacon, Boolean>();
	private final static int LIMIT_RANGE = 10;
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		Log.d(TAG, "sending beaocn to server in a full logic way");
		IBeacon big = getBestLocation(newInformation);
		httpHand.postOnRanging(big, MAC, 1,big.getRssi());
		
		
    }
		
	/**
	 * The method analyzes the new ibeacons and compares them with previous ones in order to determine
	 * the best beacon seen by the device. First calls the private method updateStatusBeacon, then choose
	 * the best beacon
	 * @param newInformation - list of new beacons
	 * @return best beacon considering the past
	 */
	public IBeacon getBestLocation(Collection<IBeacon> newInformation){
		Double coefficent = 0.8;
		updateStatusBeacon(newInformation);
		for (IBeacon iBeacon : newInformation) {
		
			Double current_value = beaconProximity.get(iBeacon);
			if (current_value ==null){
				current_value = iBeacon.getAccuracy();
			}
			//Log.d(TAG, getUUIDMaiorMinor(iBeacon)+" current value"+current_value+" accuracy:"+iBeacon.getAccuracy());
			Double new_value = current_value*coefficent+(1-coefficent)*iBeacon.getAccuracy();
			if(new_value <= LIMIT_RANGE){
				Log.d(TAG,"in limit "+new_value+ " ibeacon "+iBeacon.getMinor());
				beaconProximity.put(iBeacon, new_value);
			}else{
				Log.d(TAG,"over limit "+new_value+ " ibeacon "+iBeacon.getMinor());
				beaconProximity.remove(iBeacon);
				beaconStatus.remove(iBeacon);
			}
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
	
	private void updateStatusBeacon(Collection<IBeacon> newInformation){
		Iterator<IBeacon> iterator = beaconStatus.keySet().iterator();
		
		while(iterator.hasNext()){
			IBeacon ibeacon = iterator.next();
			//control if the beacon is in new information
			if(!newInformation.contains(ibeacon)){
				//first time lost beacon
				if(!beaconStatus.get(ibeacon).booleanValue()){
					Log.d(TAG,"first time lost beacon "+ibeacon.getMinor());
					beaconStatus.put(ibeacon, Boolean.valueOf(true));
				}
				else{
					Log.d(TAG,"second time lost beacon "+ibeacon.getMinor()+" remove it");
					beaconProximity.remove(ibeacon);
					beaconStatus.remove(ibeacon);
				}
			}else{
				Log.d(TAG,"beacon already present "+ibeacon.getMinor());
				beaconStatus.put(ibeacon,Boolean.valueOf(false));
			}
		}
	
		for (IBeacon iBeacon : newInformation) {
			if(!beaconStatus.containsKey(iBeacon)){
				Log.d(TAG,"add new beacon in status"+iBeacon.getMinor());
				beaconStatus.put(iBeacon, Boolean.valueOf(false));
			}
		}
	
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