package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;


public abstract class BeaconHandler{
	
	protected static final String TAG = "BeaconToSendManager";
	protected final HttpHandler httpHand = new HttpHandler("http://192.168.0.152/ibeacon");

	/**
	 * Method which will handle the list of the current beacons which will be send to the server 
	 * @param oldInformation	list of beacons of the previous scan
	 * @param newInformation	list of beacons of the current scan
	 * @param MAC				MAC of the device bluetooth 
	 */
	abstract void beaconToSend(Collection<IBeacon> oldInformation, Collection<IBeacon> newInformation, String MAC);
	/**
	 * Method which will communicate the exit of the device from the monitored region to the server
	 * @param idBluetooth MAC of the device bluetooth
	 */
	abstract void exitingRegion(String idBluetooth);

}
