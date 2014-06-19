package it.polimi.it.ibeaconoccupancy.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothHelper implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private static final String TAG="BluetoothHelpers";
	ArrayList<String> discoveredDevices;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private static DiscoverThread discover;
	private static BluetoothHelper instance;
	private static HashSet<BluetoothDevice> devices = new HashSet<BluetoothDevice>();
	private static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
	private Boolean lock;
	private  BluetoothDevice current;
	
	private BluetoothHelper() {
		if (discover==null){
			Log.d(TAG, "instantiating first time");
			discover = new DiscoverThread();
			discover.start();
		}
		discoveredDevices = new ArrayList<String>();
		lock = Boolean.valueOf(true);
	}
	
	public static BluetoothHelper getInstance(){
		
		if(instance == null){
			instance = new BluetoothHelper();
		}
		return instance;
	}
	
	public void startDiscovery() {  // If we're already discovering, stop it
		
        if (mBluetoothAdapter.isDiscovering()) {
        	mBluetoothAdapter.cancelDiscovery();
        }
		if (mBluetoothAdapter.startDiscovery()){
			Log.d(TAG, "discovering bluetooth");
			devices = new HashSet<BluetoothDevice>();
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
    	if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
    	// Cancel any thread currently running a connection
	    if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
	    // Start the thread to connect with the given device
	    mConnectThread = new ConnectThread(device);
	    mConnectThread.start();
	}
    
   
    
    private class DiscoverThread extends Thread{
    	private HashSet<BluetoothDevice> addresses = new HashSet<BluetoothDevice>();
    	private LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
			
			@Override
			public void onLeScan(BluetoothDevice arg0, int arg1, byte[] arg2) {
				Log.d(TAG,"Lescan "+arg0.getName());
				if(arg0.getName().contains("rasp") || arg0.getName().contains("andrea")){
					synchronized (devices) {
						devices.add(arg0);
					}
		    	}
				
			}
		};
    	
    	public DiscoverThread() {
    		//
    		//luetoothAdapter.getDefaultAdapter().startDiscovery();
    		BluetoothAdapter.getDefaultAdapter().startLeScan(callback);
		}
    	
    	
    	public void run(){
    		while(true){
    			/*try {
    				startDiscovery();
					sleep(8000);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}*/
	    		synchronized (devices) {
					addresses =(HashSet<BluetoothDevice>) devices.clone(); //copy list of current dicovered devices
				}
	    		
	    		
	    		for (BluetoothDevice device : addresses) {
		    		Log.d(TAG,"inside loop");
		    		if(device.getName() == null){
		    			Log.d(TAG, "device is null");
		    			continue;
		    		}
		    		Log.d(TAG,"discover thread"+device.getName());
		    		
		    		connect(device);
		    		synchronized(lock) {
						try {
							lock.wait();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		    		}
		    		Log.d(TAG, "over ");	
		    		if (current==null){
		    			Log.d(TAG, "address not reachable");
		    			continue;
		    		}
		    		else{
		    			Log.d(TAG, "connected");
		    			try {
							keepAlive();
						} catch (IOException e) {
							Log.d(TAG, "disconnected from current device ");
							continue;
						}
		    		}
	    		
	    		}
    		}
	    		
    		
    	}
    	
    	private void keepAlive() throws IOException{
    		while(true){
    				Log.d(TAG,"keeping alive");
					mConnectedThread.write("Hello".getBytes());
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
    		}
    	}
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
	            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
	        } catch (IOException e) {
	        	Log.d(TAG,"Error creating socket");
	        }
	        mmSocket = tmp;
	        Log.d(TAG, "in ConnectThread socket"+mmSocket);
	    }
	 
	    public void run() {
	    	Log.d(TAG, "in run of connectThread");
	        // Cancel discovery because it will slow down the connection
	        //stopDiscovery();;
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	        	Log.d(TAG,"cannot open connection");
	        	current = null;
	        	connectException.printStackTrace();
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) {
		        	Log.d(TAG,"cannot close socket");
		        	closeException.printStackTrace();

	            }
	            synchronized(lock) {
	            	lock.notifyAll();
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
        current = device;
        
        synchronized(lock) {
        	lock.notifyAll();
        }
        
    }
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
    	
        
	        try{
	        	mConnectedThread.write(out);
	        	Log.d(TAG, "successfully sent message bluetooth");
	        }catch (Exception e){
	        	Log.d(TAG, "Bad luck sending message through bluetooth");
				
	        }
	      
	        
       
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
         * @throws IOException 
         */
        public void write(byte[] buffer) throws IOException {
            mmOutStream.write(buffer);
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
/*
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"-------received bluetooth devices--------");
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        	// Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    		if(device.getName().contains("rasp") || device.getName().contains("andrea")){
			synchronized (devices) {
				devices.add(device);
			}
    		}
            for (BluetoothDevice dev : devices) {
            	Log.d(TAG,"found devices"+dev.getAddress());
            }
       }
		
	}
	

	public void stopDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
        	Log.d(TAG, "cancel discovering");
        	mBluetoothAdapter.cancelDiscovery();
        }
	}*/
}
