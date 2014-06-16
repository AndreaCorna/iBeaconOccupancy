package it.polimi.it.ibeaconoccupancy.compare;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

public class Logic implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG ="Logic";
	private IBeacon bestBeacon = null;
	HashMap<IBeacon, Double> beaconProximity = new HashMap<IBeacon, Double>();
	ConcurrentHashMap<IBeacon, Boolean> beaconStatus = new ConcurrentHashMap<IBeacon, Boolean>();
	private static Logic instance;
	
	private Logic(){
		
	}
	
	public static Logic getInstance(){
		if(instance == null){
			instance = new Logic();
		}
		return instance;
	}
	
	
	
	/**
	 * The method analyzes the new ibeacons and compares them with previous ones in order to determine
	 * the best beacon seen by the device. First calls the private method updateStatusBeacon, then choose
	 * the best beacon
	 * @param newInformation - list of new beacons
	 * @return best beacon considering the past
	 */
	private void updateInformation(Collection<IBeacon> newInformation){
		bestBeacon = newInformation.iterator().next();
		for(IBeacon ibeacon : newInformation){
			if(ibeacon.getAccuracy() < bestBeacon.getAccuracy()){
				bestBeacon = ibeacon;
			}
		}
		Log.d(TAG,"Best beacon in logic"+bestBeacon.getProximityUuid()+bestBeacon.getMajor()+bestBeacon.getMinor());
		
	}
	
	public IBeacon getBestLocation(Collection<IBeacon> newInformation){
		updateInformation(newInformation);
		return bestBeacon;

	}
	
	public IBeacon getBestLocation(){
		return bestBeacon;
	}

}
