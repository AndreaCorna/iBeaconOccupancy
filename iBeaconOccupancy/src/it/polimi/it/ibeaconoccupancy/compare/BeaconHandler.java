package it.polimi.it.ibeaconoccupancy.compare;


import java.io.Serializable;
import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * This interface defines all methods used to make http request to the server
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public interface  BeaconHandler extends Serializable{
	/**
	 * Method which will handle the list of the current beacons which will be send to the server 
	 * @param newInformation	list of beacons of the current scan
	 * @param MAC				MAC of the device bluetooth 
	 */
	public void beaconToSend( Collection<IBeacon> newInformation, String MAC);
	
	/**
	 * Method which will communicate to the server the exit of the device from the monitored region 
	 * @param idBluetooth MAC of the device bluetooth
	 */
	public void exitingRegion(String idBluetooth);

}



