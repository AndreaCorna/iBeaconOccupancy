package it.polimi.it.ibeaconoccupancy.helper;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Environment;
import android.util.Log;

public class LogFileHelper {
	
	private static final String TAG = "LogFileHelper";
	
	private static final String IBEACON_DIR = "Ibeacon";
	private static final String IBEACON_LOG_FILE = "LogFile.txt";
	
	public static Boolean writeLogEntry(String logEntry){
		// Setup folder and dir
		File folder = new File(Environment.getExternalStorageDirectory() + 
								"/" + IBEACON_DIR);	
		if(!folder.exists())
			folder.mkdir();
		
		File logFile = new File(Environment.getExternalStorageDirectory() + 
									"/" + IBEACON_DIR, 
									IBEACON_LOG_FILE);
		
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			
			FileOutputStream out = new FileOutputStream(logFile, true);
			out.write(logEntry.getBytes());
			Log.d(TAG, "Writing to file "+logEntry);
			out.close();
			return true;
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException when accessing the file: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.e(TAG, "IOException when writing to file: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public static String readFileContent(){
		// Setup folder and dir
		File folder = new File(Environment.getExternalStorageDirectory() + 
								"/" + IBEACON_DIR);	
		if(!folder.exists())
			return "No file found";
		
		File logFile = new File(Environment.getExternalStorageDirectory() + 
									"/" + IBEACON_DIR, 
									IBEACON_LOG_FILE);
		if(!logFile.exists())
			return "No file found";
		else{
			try{
				InputStream inputStream = new FileInputStream(logFile);
				 
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();
				     
				while((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
					stringBuilder.append("\n");
				}
				     
				inputStream.close();
				return stringBuilder.toString();
				
			}catch(FileNotFoundException e){
				Log.e(TAG, "FileNotFoundException when reading from file: " + e.getMessage());
				e.printStackTrace();
				return "FileNotFoundException when reading from file: " + e.getMessage();
			} catch (IOException e) {
				Log.e(TAG, "IOException when reading from file: " + e.getMessage());
				e.printStackTrace();
				return "IOException when reading from file: " + e.getMessage();
			}
		}
	}
	
	public static Boolean removeLogFile(){
		// Setup folder and dir
		File folder = new File(Environment.getExternalStorageDirectory() + 
								"/" + IBEACON_DIR);	
		if(!folder.exists())
			return true;
		
		File logFile = new File(Environment.getExternalStorageDirectory() + 
									"/" + IBEACON_DIR, 
									IBEACON_LOG_FILE);
		
		if(!logFile.exists())
			return true;
		else{
			return logFile.delete();
		}
	}
	
	public static File getLogFile(){
		// Setup folder and dir
		File folder = new File(Environment.getExternalStorageDirectory() + 
								"/" + IBEACON_DIR);	
		if(!folder.exists())
			folder.mkdir();
		
		File logFile = new File(Environment.getExternalStorageDirectory() + 
									"/" + IBEACON_DIR, 
									IBEACON_LOG_FILE);
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			return logFile;
		} catch (IOException e) {
			Log.e(TAG, "IOException when writing to file: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static boolean renameFile() {
		// Setup folder and dir
		File folder = new File(Environment.getExternalStorageDirectory() + 
								"/" + IBEACON_DIR);	
		if(!folder.exists())
			return false;
		
		File logFile = new File(Environment.getExternalStorageDirectory() + 
									"/" + IBEACON_DIR, 
									IBEACON_LOG_FILE);
		
		long timestamp = System.currentTimeMillis();
		File newLogFile = new File(Environment.getExternalStorageDirectory() + 
									"/" + IBEACON_DIR, 
									timestamp + "-" + IBEACON_LOG_FILE);
		
		if(!logFile.exists())
			return false;
		else
			return logFile.renameTo(newLogFile);
	}
}
