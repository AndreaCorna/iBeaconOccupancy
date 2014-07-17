package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.communication.BluetoothHandler;
import it.polimi.it.ibeaconoccupancy.communication.CommunicationHandler;
import it.polimi.it.ibeaconoccupancy.communication.HttpHandler;
import it.polimi.it.ibeaconoccupancy.helper.Constants;

import java.io.Serializable;
import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * The class implements the version of application with the proximity approach. The posts to the server of this version are done to the 
 * address <ip_of_server>/ibeacon
 * @author Andrea Corna - Lorenzo Fontana
 * @see BeaconHandler.java
 * @see Serializable
 */
public class ProximityHandlerImpl implements BeaconHandler, Serializable {
	
	private static final long serialVersionUID = -6887364374840188927L;
	protected static final String TAG = "BeaconToSendManager";
	private CommunicationHandler communication;
	private Logic appLogic = Logic.getInstance();
	
	public ProximityHandlerImpl(boolean bluetoothSender){
		if(bluetoothSender){
			Log.d(TAG,"create bluetooth handler");
			communication = new BluetoothHandler();
		}else{
			Log.d(TAG,"create http handler");

			communication = new HttpHandler(Constants.ADDRESS_LOGIC_PROXIMITY);
		}
		
	}
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		
		IBeacon big = appLogic.getBestLocation(newInformation);
		if (big != null){
			Log.d(TAG, "sending beacon to server in a full logic way");
			communication.postOnRanging(big, MAC);
		}
		else{
			Log.d(TAG, "sending out of region fullbeacon way");
			communication.postOnMonitoringOut(MAC);
		}
			
	}
		
	@Override
	public void exitingRegion(String idBluetooth) {
		communication.postOnMonitoringOut(idBluetooth);

		
	}

	
}