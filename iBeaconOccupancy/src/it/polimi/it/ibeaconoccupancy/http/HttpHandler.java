package it.polimi.it.ibeaconoccupancy.http;


import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

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
	
	protected static final String TAG = "HTTP";
	private String url;
	
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
	 * @param status - 1 or 0, depending if I see or not a beacon
	 * @param power - the strength of the signal
	 */
	public void postOnRanging(IBeacon beacon, String idBluetooth, int status, int power){
		int responseCode = 0;
        String id_beacon = beacon.getProximityUuid()+beacon.getMajor()+beacon.getMinor();
        String stringPost = url+"/"+idBluetooth+"/"+id_beacon;
        URL urlPost;
		try {
			urlPost = new URL(stringPost);
		   	HttpURLConnection httpCon = (HttpURLConnection) urlPost.openConnection();
        	httpCon.setDoOutput(true);
        	httpCon.setDoInput(true);
        	
        	httpCon.setRequestMethod("POST");
        	httpCon.setRequestProperty("content-type","application/json; charset=utf-8"); 
          	httpCon.setRequestProperty("Accept", "application/json");
          	
        	
       
            JSONObject jsonObject = new JSONObject();
            
            jsonObject.put("status", status);
            jsonObject.put("power", power);
            
            OutputStreamWriter wr= new OutputStreamWriter(httpCon.getOutputStream());
            wr.write(jsonObject.toString());
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
