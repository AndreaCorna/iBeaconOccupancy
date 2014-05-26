package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.io.Serializable;
import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * The class implements the version of application with logic on server. The posts to the server of this version are done to the 
 * address <ip_of_server>/ibeaconserver
 * @author Andrea Corna - Lorenzo Fontana
 */
public class MinimalBeaconHandlerImpl implements BeaconHandler,Serializable {
	

	private static final long serialVersionUID = -6878023027031829217L;
	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler("http://ibeacon.no-ip.org/ibeaconserver");

	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		httpHand.postingOnRanging(newInformation, MAC);
	}
    


	@Override
	public void exitingRegion(String idBluetooth) {
		httpHand.postOnMonitoringOut( idBluetooth);
		
	}

	
}
