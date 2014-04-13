package it.polimi.it.ibeaconoccupancy.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	
	
	public void postOnRanging(IBeacon beacon, String idBluetooth, int status){
    	InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = "";
            JSONObject jsonObject = new JSONObject();
            String id_beacon = beacon.getProximityUuid()+beacon.getMajor()+beacon.getMinor();
            jsonObject.accumulate("id_beacon", id_beacon);
            jsonObject.accumulate("id_device", idBluetooth);
            jsonObject.accumulate("status", status);
 
            json = jsonObject.toString();
            StringEntity se = new StringEntity(json);
 
            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
 
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        	Log.d(TAG,result);
    
    }
	
	public void postOnMonitoringOut( String idBluetooth){
    	InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url+"/exitregion");
            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("id_device", idBluetooth);
   
 
            json = jsonObject.toString();
            StringEntity se = new StringEntity(json);
 
            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
 
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        	Log.d(TAG,result);
    
    }
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }

}
