package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class MinimalBeaconHandlerImpl implements BeaconHandler,Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -6878023027031829217L;
	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeaconserver");

	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		for (IBeacon iBeacon : newInformation) {
    		httpHand.postOnRanging(iBeacon, MAC, 1,iBeacon.getRssi());
		}
      	
		
	}
    


	@Override
	public void exitingRegion(String idBluetooth) {
		httpHand.postOnMonitoringOut( idBluetooth);
		
	}

	
}
