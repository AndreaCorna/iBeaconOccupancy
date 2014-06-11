package it.polimi.it.ibeaconoccupancy.communication;

import java.util.HashMap;

import com.radiusnetworks.ibeacon.IBeacon;

public interface CommunicationHandler {
	
	public void postOnRanging(IBeacon beacon, String idBluetooth);

	public void postingOnRanging(HashMap<IBeacon, Double> update, String idBluetooth);
	
	public void postOnMonitoringOut( String idBluetooth);

}
