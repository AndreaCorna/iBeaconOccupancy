package it.polimi.it.ibeaconoccupancy.http;


import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * This class implements all method in order to interact with the server in order to manage information 
 * about iBeacons saw by the device.
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class HttpHandler implements Serializable{
	

	private static final long serialVersionUID = 1L;
	protected static final String TAG = "HTTP";
	private String url;
	private final static int UPPER_DISTANCE = 10;
	
	/**
	 * Create an object of the class, setting the base url of the server
	 * @param url - url of the server root
	 */
	public HttpHandler(String url){
		this.url = url;
	}
	
	/**
	 * This method does a post on the server with information related to one beacon.
	 * @param beacon - ibeacon object with all information
	 * @param idBluetooth - MAC identifier of the device
	 */
	public void postOnRanging(IBeacon beacon, String idBluetooth){
		int responseCode = 0;
        String id_beacon = beacon.getProximityUuid()+beacon.getMajor()+beacon.getMinor();
        String stringPost = url+"/"+idBluetooth+"/"+id_beacon;
        URL urlPost;
		try {
			urlPost = new URL(stringPost);
		   	HttpURLConnection httpCon = (HttpURLConnection) urlPost.openConnection();
        	httpCon.setDoOutput(true);
        	
        	httpCon.setRequestMethod("POST");
        	httpCon.setRequestProperty("content-type","application/json; charset=utf-8"); 
          	httpCon.setRequestProperty("Accept", "application/json");
          	
            responseCode = httpCon.getResponseCode();
 
            
        } catch (Exception e) {}
        	Log.d(TAG,"SEND RESPONSE"+responseCode);
    
    }
	
	/**
	 * This method does a post request to the server contains a json with information
	 * related to all beacons
	 * @param update.keySet() - list of beacon 
	 * @param idBluetooth - MAC address of the device
	 */
	public void postingOnRanging(HashMap<IBeacon, Double> update, String idBluetooth){
		int responseCode = 0;
        String stringPost = url+"/"+idBluetooth;
        URL urlPost;
		try {
			urlPost = new URL(stringPost);
		   	HttpURLConnection httpCon = (HttpURLConnection) urlPost.openConnection();
        	httpCon.setDoOutput(true);
        	httpCon.setDoInput(true);
        	
        	httpCon.setRequestMethod("POST");
        	httpCon.setRequestProperty("content-type","application/json; charset=utf-8"); 
          	httpCon.setRequestProperty("Accept", "application/json");
          	
        	
          	JSONArray iBeacons = new JSONArray();
            for (IBeacon iBeacon : update.keySet()) {
            	JSONObject beaconPropertier = new JSONObject();
            	String id_beacon = iBeacon.getProximityUuid()+iBeacon.getMajor()+iBeacon.getMinor();
            	Log.d(TAG,"id "+id_beacon);
            	beaconPropertier.accumulate("id_beacon", id_beacon);
            	beaconPropertier.accumulate("distance", UPPER_DISTANCE - update.get(iBeacon));
            	iBeacons.put(beaconPropertier);
            	
            	
			}
            Log.d(TAG,"json result "+iBeacons.toString());
           
           
            OutputStreamWriter wr= new OutputStreamWriter(httpCon.getOutputStream());
            wr.write(iBeacons.toString());
            wr.flush();
            responseCode = httpCon.getResponseCode();
 
            
        } catch (Exception e) {}
        	Log.d(TAG,"SEND RESPONSE"+responseCode);
    
		
	}
	
	/**
	 * This method does a delete request to server about all information related to
	 * the MAC identifier passed as an argument
	 * @param idBluetooth - MAC identifier of the device
	 */
	public void postOnMonitoringOut( String idBluetooth){
		
        String stringDelete = url+"/"+idBluetooth;
        int responseCode = 0;
        try{
	        URL urlDelete = new URL(stringDelete);
	        HttpURLConnection connection = (HttpURLConnection) urlDelete.openConnection();
	        connection.setRequestMethod("DELETE");
	        responseCode = connection.getResponseCode();
        }catch(Exception e){}
        
        Log.d(TAG,"DELETE RESPONSE "+responseCode);
    
    }
	


}
