package it.polimi.it.ibeaconoccupancy.compare;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.radiusnetworks.ibeacon.IBeacon;

public class Logic implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	
		
	public IBeacon getBestLocation(Collection<IBeacon> newInformation){
		Iterator<IBeacon> iterator = newInformation.iterator();
		bestBeacon = iterator.next();
		while(iterator.hasNext()){
			IBeacon beacon = iterator.next();
			if (bestBeacon.getAccuracy() > beacon.getAccuracy()){
				bestBeacon = beacon;
			}
		}
		return bestBeacon;

	}

}
