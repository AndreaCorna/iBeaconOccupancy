package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.communication.BluetoothHandler;
import it.polimi.it.ibeaconoccupancy.communication.CommunicationHandler;
import it.polimi.it.ibeaconoccupancy.communication.HttpHandler;
import it.polimi.it.ibeaconoccupancy.helper.Constants;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * The class implements the version of application with logic on server. The posts to the server of this version are done to the 
 * address <ip_of_server>/ibeaconserver
 * @author Andrea Corna - Lorenzo Fontana
 * @see BeaconHandler.java
 * @see Serializable
 */
public class MinimalBeaconHandlerImpl implements BeaconHandler,Serializable {
	

	private static final long serialVersionUID = -6878023027031829217L;
	protected static final String TAG = "BeaconToSendManager";
	private CommunicationHandler communication;
	private Logic appLogic = Logic.getInstance();

	
	public MinimalBeaconHandlerImpl(boolean bluetoothSender){

		if(bluetoothSender){
			Log.d(TAG,"create bluetooth handler");

			communication = new BluetoothHandler();
		}else{
			Log.d(TAG,"create http handler");

			communication = new HttpHandler(Constants.ADDRESS_LOGIC_ON_SERVER);
		}
	}
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		HashMap<IBeacon, Double> update = appLogic.getHashMap(newInformation);
		if (!update.keySet().isEmpty()){
			Log.d(TAG, "sending beaocn to server in a minimal logic way");
			communication.postingOnRanging(update, MAC);	
		}
		else{
			Log.d(TAG, "sending out of region in  minimallogic way");
			communication.postOnMonitoringOut(MAC);
		}
	}
    


	@Override
	public void exitingRegion(String idBluetooth) {
		communication.postOnMonitoringOut( idBluetooth);
		
	}

	
}
