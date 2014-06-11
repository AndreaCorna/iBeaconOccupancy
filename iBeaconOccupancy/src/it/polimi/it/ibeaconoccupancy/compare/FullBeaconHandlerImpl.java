package it.polimi.it.ibeaconoccupancy.compare;

import it.polimi.it.ibeaconoccupancy.communication.BluetoothHandler;
import it.polimi.it.ibeaconoccupancy.communication.CommunicationHandler;
import it.polimi.it.ibeaconoccupancy.communication.HttpHandler;
import it.polimi.it.ibeaconoccupancy.helper.Constants;

import java.io.Serializable;
import java.util.Collection;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class FullBeaconHandlerImpl implements BeaconHandler, Serializable {
	
	private static final long serialVersionUID = -6887364374840188927L;
	protected static final String TAG = "BeaconToSendManager";
	private CommunicationHandler communication;
	private Logic appLogic = Logic.getInstance();
	
	public FullBeaconHandlerImpl(boolean bluetoothSender){
		if(bluetoothSender){
			Log.d(TAG,"create bluetooth handler");
			communication = new BluetoothHandler();
		}else{
			Log.d(TAG,"create http handler");

			communication = new HttpHandler(Constants.ADDRESS_LOGIC_ON_CLIENT);
		}
		
	}
	
	@Override
	public void beaconToSend(Collection<IBeacon> newInformation, String MAC) {
		Log.d(TAG, "sending beaocn to server in a full logic way");
		IBeacon big = appLogic.getBestLocation(newInformation);
		communication.postOnRanging(big, MAC);
	}
		
	@Override
	public void exitingRegion(String idBluetooth) {
		communication.postOnMonitoringOut(idBluetooth);

		
	}

	
}