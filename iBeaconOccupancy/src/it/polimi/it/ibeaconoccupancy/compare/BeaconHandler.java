package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;


public interface  BeaconHandler extends Serializable{
	/**
	 * Method which will handle the list of the current beacons which will be send to the server 
	 * @param newInformation	list of beacons of the current scan
	 * @param MAC				MAC of the device bluetooth 
	 */
	public void beaconToSend( Collection<IBeacon> newInformation, String MAC);
	/**
	 * Method which will communicate the exit of the device from the monitored region to the server
	 * @param idBluetooth MAC of the device bluetooth
	 */
	public void exitingRegion(String idBluetooth);

}



