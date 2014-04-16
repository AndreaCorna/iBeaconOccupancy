package it.polimi.it.ibeaconoccupancy.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;


public class HttpHandler {
	
	protected static final String TAG = "HTTP";
	private String url;
	
	public HttpHandler(String url){
		this.url = url;
	}
	
	
	public void postOnRanging(IBeacon beacon, String idBluetooth, int status, int power){
		int responseCode = 0;
        String id_beacon = beacon.getProximityUuid()+beacon.getMajor()+beacon.getMinor();
        String stringPost = url+"/"+idBluetooth+"/"+id_beacon+"/";
        URL urlPost;
		try {
			urlPost = new URL(stringPost);
		   	HttpURLConnection httpCon = (HttpURLConnection) urlPost.openConnection();
        	httpCon.setDoOutput(true);
        	
        	httpCon.setRequestMethod("POST");
        	httpCon.setRequestProperty("Content-Type", "application/json; charset=utf8");
        	
       
            JSONObject jsonObject = new JSONObject();
            
            jsonObject.put("status", status);
            jsonObject.put("power", power);
            
            OutputStreamWriter wr= new OutputStreamWriter(httpCon.getOutputStream());
            wr.write(jsonObject.toString());
            responseCode = httpCon.getResponseCode();
 
            
        } catch (Exception e) {}
        	Log.d(TAG,"SEND RESPONSE"+responseCode);
    
    }
	
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
