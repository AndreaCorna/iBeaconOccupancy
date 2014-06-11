package it.polimi.it.ibeaconoccupancy.communication;


import java.io.Serializable;
import java.util.HashMap;


import com.radiusnetworks.ibeacon.IBeacon;


public class BluetoothHandler implements CommunicationHandler,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	@Override
	public void postOnRanging(IBeacon beacon, String idBluetooth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postingOnRanging(HashMap<IBeacon, Double> update,
			String idBluetooth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postOnMonitoringOut(String idBluetooth) {
		// TODO Auto-generated method stub
		
	}
	
}
