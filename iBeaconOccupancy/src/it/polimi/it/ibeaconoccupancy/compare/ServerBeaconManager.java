package it.polimi.it.ibeaconoccupancy.compare;

import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;


public interface ServerBeaconManager{
	
	public void beaconToSend(Collection<IBeacon> oldInformation, Collection<IBeacon> newInformation, String MAC);

}