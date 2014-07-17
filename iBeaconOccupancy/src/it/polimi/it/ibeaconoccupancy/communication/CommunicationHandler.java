package it.polimi.it.ibeaconoccupancy.communication;

import java.util.HashMap;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * This interface shows the methods used by the application in order to communicate with the central server
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public interface CommunicationHandler {
	
	/**
	 * Send information to the server in using the proximity approach
	 * @param beacon - best beacon
	 * @param idBluetooth - mac of the device adapter
	 */
	public void postOnRanging(IBeacon beacon, String idBluetooth);

	/**
	 * Send information to the server using the machine learning approach
	 * @param update - all the information
	 * @param idBluetooth - mac device
	 */
	public void postingOnRanging(HashMap<IBeacon, Double> update, String idBluetooth);
	
	/**
	 * Send information when you go out from a region.
	 * @param idBluetooth - mac of device
	 */
	public void postOnMonitoringOut( String idBluetooth);

}
