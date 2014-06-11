package it.polimi.it.ibeaconoccupancy.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.radiusnetworks.ibeacon.IBeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothHandler extends Thread implements CommunicationHandler{
	
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private static final String TAG="BluetoothHandler";
	ArrayList<String> discoveredDevices;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	
	public BluetoothHandler() {
		discoveredDevices = new ArrayList<String>();
		
	}
	
	public void startDiscovery() {  // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
        	mBluetoothAdapter.cancelDiscovery();
        }

		if (mBluetoothAdapter.startDiscovery()){
			Log.d(TAG, "discovering bluetooth");
		}
		else {
			Log.d(TAG, "problem in discovering bluetooth");
		}
	}
	
	  /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
       
        
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
    
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
       
    }
	
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"));
	        } catch (IOException e) {
	        	Log.d(TAG,"Error creating socket");
	        }
	        mmSocket = tmp;
	        Log.d(TAG, "in ConnectThread socket"+mmSocket);
	    }
	 
	    public void run() {
	    	Log.d(TAG, "in run of connectThread");
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	        	Log.d(TAG,"cannot open connection");
	        	connectException.printStackTrace();
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) {
		        	Log.d(TAG,"cannot close socket");
		        	closeException.printStackTrace();

	            }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	     // Start the connected thread
            connected(mmSocket, mmDevice);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	 /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
      
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
     // if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        
      
        // Send the name of the connected device back to the UI Activity
        
        
    }
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private  final BluetoothSocket mmSocket;
        private  final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
        
            mmSocket = socket;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmOutStream = tmpOut;
            
          
           
           
        }
    
       
        
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
               
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    public void cancel(){
    	mConnectedThread.cancel();
    	mConnectedThread=null;
    	mConnectThread=null;
    }

	@Override
	public void postOnRanging(IBeacon beacon, String idBluetooth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postingOnRanging(HashMap<IBeacon, Double> update,
			String idBluetooth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postOnMonitoringOut(String idBluetooth) {
		// TODO Auto-generated method stub
		
	}
	
}
