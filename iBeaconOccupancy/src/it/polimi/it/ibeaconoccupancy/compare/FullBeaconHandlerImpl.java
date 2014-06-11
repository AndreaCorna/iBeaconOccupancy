package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.Constants;
import it.polimi.it.ibeaconoccupancy.communication.HttpHandler;

import java.io.Serializable;
import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class FullBeaconHandlerImpl implements BeaconHandler, Serializable {
	
	private static final long serialVersionUID = -6887364374840188927L;
	protected static final String TAG = "BeaconToSendManager";
	private final HttpHandler httpHand = new HttpHandler(Constants.ADDRESS_LOGIC_ON_CLIENT);
	private Logic appLogic = Logic.getInstance();
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		Log.d(TAG, "sending beaocn to server in a full logic way");
		IBeacon big = appLogic.getBestLocation(newInformation);
		httpHand.postOnRanging(big, MAC);
	}
		
	@Override
	public void exitingRegion(String idBluetooth) {
		httpHand.postOnMonitoringOut(idBluetooth);

		
	}

	
}