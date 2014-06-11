package it.polimi.it.ibeaconoccupancy.communication;


import it.polimi.it.ibeaconoccupancy.helper.BluetoothHelper;
import it.polimi.it.ibeaconoccupancy.helper.Constants;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class BluetoothHandler implements CommunicationHandler,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG = "BluetoothHandler";
	
	@Override
	public void postOnRanging(IBeacon beacon, String idBluetooth) {
		JSONObject obj = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject beaconData = new JSONObject();
        String id_beacon = beacon.getProximityUuid()+beacon.getMajor()+beacon.getMinor();
        BluetoothHelper helper = BluetoothHelper.getInstance();
		try {
			beaconData.put("device",idBluetooth);
			beaconData.put("beacon", id_beacon);
			data.put(beaconData);
			obj.put("type","client");
			obj.put("method","post");
			obj.put("data",data);
			Log.d(TAG,obj.toString());
			//helper.write(obj.toString().getBytes());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void postingOnRanging(HashMap<IBeacon, Double> update,String idBluetooth) {
		JSONObject obj = new JSONObject();
		JSONArray data = addBeaconInformation(update);
		BluetoothHelper helper = BluetoothHelper.getInstance();

		try {
			obj.put("type","server");
			obj.put("method","post");
			obj.put("data", data);
			obj.put("device", idBluetooth);
			Log.d(TAG,obj.toString());
			//helper.write(obj.toString().getBytes());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void postOnMonitoringOut(String idBluetooth) {
		JSONObject obj = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject beaconData = new JSONObject();
        String id_beacon = "empty";
        BluetoothHelper helper = BluetoothHelper.getInstance();

		try {
			beaconData.put("device",idBluetooth);
			beaconData.put("beacon", id_beacon);
			data.put(beaconData);
			obj.put("type","client");
			obj.put("method","delete");
			obj.put("data",data);
			Log.d(TAG,obj.toString());
			
			//helper.write(obj.toString().getBytes());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private JSONArray addBeaconInformation(HashMap<IBeacon, Double> info){
		JSONArray data = new JSONArray();
		for (IBeacon ibeacon : info.keySet()) {
			JSONObject objBeacon = new JSONObject();
	        String id_beacon = ibeacon.getProximityUuid()+ibeacon.getMajor()+ibeacon.getMinor();

			try {
				objBeacon.put("beacon", id_beacon);
				objBeacon.put("accuracy", Constants.UPPER_DISTANCE - info.get(ibeacon));
				data.put(objBeacon);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}
	
}
