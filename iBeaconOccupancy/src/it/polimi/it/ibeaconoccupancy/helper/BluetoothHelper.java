package it.polimi.it.ibeaconoccupancy.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
	private static DiscoverThread discover;
	private static DiscoverThread discover2;
	private static BluetoothHelper instance;
	private static HashSet<BluetoothDevice> devices = new HashSet<BluetoothDevice>();
	private static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
	
	private HashMap<DiscoverThread, ConnectedThread> hashConnected;
	private HashMap<DiscoverThread, ConnectThread> hashConnect;
	private HashMap<DiscoverThread, Boolean> hashlock;
	private HashMap<DiscoverThread, BluetoothDevice> hashdevices;
	private HashMap<DiscoverThread, Boolean> hashKeep;

	private synchronized void showDevices(){
		Log.d(TAG,"################  DEVICES   ###################");
		for (BluetoothDevice bluetoothDevice : devices) {
			Log.d(TAG,"| BluetoothDevice list name"+bluetoothDevice.getName()+" mac "+ bluetoothDevice.getAddress()+" |");		
		}
		Log.d(TAG,"#########################################");
	}
	
	private synchronized void showHashConnected(){
		Log.d(TAG,"\\\\\\\\\\\\  HASH CONNECTED   \\\\\\\\\\\\\\");
		for (DiscoverThread discoverThread : hashConnected.keySet()) {
			Log.d(TAG,"| DiscoverThread "+discoverThread+" ConnectedThread "+hashConnected.get(discoverThread)+"  |");		
		}
		Log.d(TAG,"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
	}
	private synchronized void showHashConnect(){
		Log.d(TAG,"-----------------  HASH CONNECT   ----------------");
		for (DiscoverThread discoverThread : hashConnect.keySet()) {
			Log.d(TAG,"| DiscoverThread "+discoverThread+" ConnectThread "+hashConnect.get(discoverThread)+"  |");		
		}
		Log.d(TAG,"----------------- ----------- ----------------");
	}
	
	private synchronized void showHashDevices(){
		Log.d(TAG,"|||||||||||||||| HASH DEVICES   |||||||||||||");
		for (DiscoverThread discoverThread : hashdevices.keySet()) {
			Log.d(TAG,"| DiscoverThread "+discoverThread+" ConnectThread "+hashdevices.get(discoverThread)+"  |");		
		}
		Log.d(TAG,"|||||||||||||||||||||||||||||||||||||||||");
	}
	
	private BluetoothHelper() {
		hashConnect = new HashMap<DiscoverThread, ConnectThread>();
		hashConnected = new HashMap<DiscoverThread, ConnectedThread>();
		hashlock = new HashMap<DiscoverThread, Boolean>();
		hashdevices = new HashMap<DiscoverThread, BluetoothDevice>();
		hashKeep = new HashMap<DiscoverThread, Boolean>();

		
		if (discover==null){
			Log.d(TAG, "instantiating first time");
			discover = new DiscoverThread();
			discover.start();
			discover2 = new DiscoverThread();
			discover2.start();
		}
		discoveredDevices = new ArrayList<String>();
		startDiscovery();
		
	}
	
	public static BluetoothHelper getInstance(){
		
		if(instance == null){
			instance = new BluetoothHelper();
		}
		return instance;
	}
	
	public void startDiscovery() {  // If we're already discovering, stop it
		
		mBluetoothAdapter.startLeScan(callback);
	}
	
	private LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice arg0, int arg1, byte[] arg2) {
			Log.d(TAG,"Lescan "+arg0.getAddress());
			Log.d(TAG,"Lescan "+arg0.getName());
			if(arg0.getName().contains("rasp") || arg0.getName().contains("andrea")){
				synchronized (devices) {
					devices.add(arg0);
				}
	    	}
			
		}
	};
	
	
	
	  /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device,DiscoverThread discoverThread) {
    	if (hashConnect.get(discoverThread) != null) {hashConnect.get(discoverThread).cancel(); hashConnect.put(discoverThread,null);}
    	// Cancel any thread currently running a connection
    	if (hashConnected.get(discoverThread) != null) {hashConnected.get(discoverThread).cancel(); hashConnected.put(discoverThread,null);}	    // Start the thread to connect with the given device
	    ConnectThread mConnectThread = new ConnectThread(device,discoverThread);
	    
	    hashConnect.put(discoverThread, mConnectThread);
	    hashConnect.get(discoverThread).start();
	}
    
    public void restartKeepAlive(){
    	synchronized(hashKeep.get(discover)){
    		hashKeep.get(discover).notifyAll();
    	}
    	
    	synchronized(hashKeep.get(discover2)){
    		hashKeep.get(discover2).notifyAll();
    	}
    }
    
   
    
    private class DiscoverThread extends Thread{
    	private HashSet<BluetoothDevice> addresses = new HashSet<BluetoothDevice>();
    	
    	public DiscoverThread(){
    		hashlock.put(this, true);
    		hashKeep.put(this, true);
    	}
    
    	
    	
    	
    	@SuppressWarnings("unchecked")
		public void run(){
    		while(true){
    			Log.d(TAG,"inside loop");
    			showDevices();
    			try {
					sleep(3000);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
    			
	    		synchronized (devices) {
					addresses =(HashSet<BluetoothDevice>) devices.clone(); //copy list of current dicovered devices
				}
	    		
	    		
	    		for (BluetoothDevice device : addresses) {
	    			Log.d(TAG,"discover thread "+device.getName());
	    			showDevices();
		    		showHashDevices();
		    		if (!checkCorrectDevice(device)){
		    			continue;
		    		}
		    		
		    		
		    		connect(device,this);
		    		synchronized(hashlock.get(this)) {
						try {
							hashlock.get(this).wait();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		    		}

		    		//check if connection was successful
		    		if (hashdevices.get(this)==null){
		    			Log.d(TAG, "address not reachable");
		    			continue;
		    		}
		    		else{
		    			Log.d(TAG, "connected");
		    			try {
							keepAlive();

						} catch (IOException e) {
							Log.d(TAG, "disconnected from current device ");
							hashdevices.remove(this);
							continue;
						}
		    		}
	    		
	    		}
    		}
	    		
    		
    	}
     	private Boolean	checkCorrectDevice(BluetoothDevice device){
    		if(device.getName() == null){
    			Log.d(TAG, "device is null");
    			return false;
    		}
    		for (DiscoverThread  discoverThread : hashdevices.keySet()) {
    			BluetoothDevice bluetoothDevice;
    			//I don't consider the device linked to this thread I have only to check that doesn't exist ANOTHER discoverthread connected to the same device
    			//so i continue to the next cycle 
    			if(discoverThread!=this){
    				bluetoothDevice=hashdevices.get(discoverThread);
    			}
    			else {
					continue;
				}
    			Log.d(TAG," CHECKING CORRECTENESS device to be compared "+device);

    			Log.d(TAG," CHECKING CORRECTENESS hashdevices: "+bluetoothDevice);
				if (bluetoothDevice!=null && bluetoothDevice.equals(device)){
		    		Log.d(TAG,"already connected device "+device.getName());

					return false;
				}
			}
    		Log.d(TAG, "CHECKING CORRECTENESS RETURNONG TRUE_----------");

    		
    		//ho passato tutti i check
    		return true;
    	}
    	
    	
    	private void keepAlive() throws IOException{
    		while(true){
    				showHashConnected();
    				showDevices();
    				showHashConnect();
    				showHashDevices();
    			synchronized(hashKeep.get(this)){
    				try {
    					hashKeep.get(this).wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
				Log.d(TAG,"keeping alive");
				hashConnected.get(this).write("Hello".getBytes());
				try {
					sleep(500);
				} catch (InterruptedException e) {
						//removing this thread from connected devices
						hashdevices.remove(this);
					//attenzione concurrent modyfication
					
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
    		}
    	}
    }   
    
   
    
	
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    private final DiscoverThread discoverThread;
	 
	    public ConnectThread(BluetoothDevice device,DiscoverThread discoverThread) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	    	this.discoverThread = discoverThread;
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
	        	hashdevices.put(discoverThread, null);
	        	connectException.printStackTrace();
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) {
		        	Log.d(TAG,"cannot close socket");
		        	closeException.printStackTrace();

	            }
	            
	            //notifying discover thread which is waiting on hashlock 
	            synchronized(hashlock.get(discoverThread)) {
	            	hashlock.get(discoverThread).notifyAll();
	            }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	     // Start the connected thread
            connected(mmSocket, mmDevice,discoverThread);
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
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,DiscoverThread discoverThread) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
      
        // Cancel any thread currently running a connection
    	if (hashConnected.get(discoverThread) != null) {hashConnected.get(discoverThread).cancel(); hashConnected.put(discoverThread,null);}	    // Start the thread to connect with the given device
     // if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Start the thread to manage the connection and perform transmissions
    	
    	ConnectedThread mConnectedThread = new ConnectedThread(socket,discoverThread);
	    
	    hashConnected.put(discoverThread, mConnectedThread);
	    hashConnected.get(discoverThread).start();
       
        
       
        // Send the name of the connected device back to the UI Activity
        hashdevices.put(discoverThread,device);
        
        synchronized(hashlock.get(discoverThread)) {
        	hashlock.get(discoverThread).notifyAll();
        }
        
    }
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
			Log.d(TAG, "trying to write ");

    	
        	for (ConnectedThread connectedThread : hashConnected.values()) {
        		//
        		Log.d(TAG, "in connetd hash values: "+connectedThread);
        		if (connectedThread==null){
            		Log.d(TAG, "connected thread null");

        			continue;
        		}
        		
        		try{
    	        	connectedThread.write(out);
    	        	Log.d(TAG, "successfully sent message bluetooth");
    	        	break;
    	        }catch (Exception e){
    	        	Log.d(TAG, "Bad luck sending message through bluetooth");
    	        	DiscoverThread discoverThread = connectedThread.getReferredDiscoverThread();
    	        	Log.d(TAG, "removing device bluetooth from active list "+discoverThread);
    	        	Log.d(TAG, "removing device bluetooth from active list "+hashdevices.get(discoverThread));
    	        	
    	        	hashdevices.remove(discoverThread);
    	        	
    				
    	        }
    	      
				
			}	        
        	
        
	        
       
    }
    
    
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private  final BluetoothSocket mmSocket;
        private  final OutputStream mmOutStream;
        private final DiscoverThread discoverThread;
        public ConnectedThread(BluetoothSocket socket,DiscoverThread discoverThread) {
            Log.d(TAG, "create ConnectedThread");
            this.discoverThread = discoverThread;
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
    
       public DiscoverThread getReferredDiscoverThread() {
    	   return this.discoverThread;
		
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
