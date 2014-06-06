package it.polimi.it.ibeaconoccupancy.training;

import it.polimi.it.ibeaconoccupancy.Constants;
import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

public class Logic {
	
	private static Logic instance;
	private static final String TAG ="Logic";
	private IBeacon bestBeacon = null;
	HashMap<IBeacon, Double> beaconProximity = new HashMap<IBeacon, Double>();
	ConcurrentHashMap<IBeacon, Boolean> beaconStatus = new ConcurrentHashMap<IBeacon, Boolean>();
	private HttpHandler http;

	
	private Logic(){
		http = new HttpHandler(Constants.ADDRESS_TRAINING_LEARNING);
	}
	
	public static Logic getInstance(){
		if(instance == null){
			instance = new Logic();
		}
		return instance;
	}
	
	public void training(String answer, String MAC){
		http.postForTraining(beaconProximity, answer, MAC);
	}
	
	
	/**
	 * The method analyzes the new ibeacons and compares them with previous ones in order to determine
	 * the best beacon seen by the device. First calls the private method updateStatusBeacon, then choose
	 * the best beacon
	 * @param newInformation - list of new beacons
	 * @return best beacon considering the past
	 */
	public void updateInformation(Collection<IBeacon> newInformation){
		Double coefficent = 0.8;
		updateStatusBeacon(newInformation);
		for (IBeacon iBeacon : newInformation) {
		
			Double current_value = beaconProximity.get(iBeacon);
			if (current_value ==null){
				current_value = iBeacon.getAccuracy();
			}
			//Log.d(TAG, getUUIDMaiorMinor(iBeacon)+" current value"+current_value+" accuracy:"+iBeacon.getAccuracy());
			Double new_value = current_value*coefficent+(1-coefficent)*iBeacon.getAccuracy();
			if(new_value <= Constants.UPPER_DISTANCE){
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
		bestBeacon = minEntry.getKey();		
		
	}
	
	public IBeacon getBestLocation(){
		return bestBeacon;

	}
	
	public 	HashMap<IBeacon, Double> getHashMap(){
		return this.beaconProximity;
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
	

}



