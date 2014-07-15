package it.polimi.it.ibeaconoccupancy.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class implements the bluetooth communication with the server and scans the air in order
 * to find devices to connect with.
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class BluetoothHelper extends BroadcastReceiver implements Serializable{
	
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

	/**
	 * This constructor is public because Android needs it in order to 
	 * implements a Broadcast Receiver. You have to use the method
	 * getInstance() in order to have always the same instance of the class.
	 */
	public BluetoothHelper() {
		hashConnect = new HashMap<DiscoverThread, ConnectThread>();
		hashConnected = new HashMap<DiscoverThread, ConnectedThread>();
		hashlock = new HashMap<DiscoverThread, Boolean>();
		hashdevices = new HashMap<DiscoverThread, BluetoothDevice>();
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
	
	/**
	 * Method that creates the singleton of the class.
	 * @return instance of BluetoothHelper
	 */
	public static BluetoothHelper getInstance(){
		
		if(instance == null){
			instance = new BluetoothHelper();
		}
		return instance;
	}
	
	/**
	 * The method starts the bluetooth discovery.
	 */
	public void startDiscovery() { 
		mBluetoothAdapter.startDiscovery();
	}
	
	
	/**
	 * Start the connectThread in order to try to connect to the device
	 * @param device - device which we are trying to connect to
	 * @param discoverThread - the discover thread that manages the connection
	 */
    public synchronized void connect(BluetoothDevice device,DiscoverThread discoverThread) {
    	if (hashConnect.get(discoverThread) != null) {hashConnect.get(discoverThread).cancel(); hashConnect.put(discoverThread,null);}
    	// Cancel any thread currently running a connection
    	if (hashConnected.get(discoverThread) != null) {hashConnected.get(discoverThread).cancel(); hashConnected.put(discoverThread,null);}	    // Start the thread to connect with the given device
	    ConnectThread mConnectThread = new ConnectThread(device,discoverThread);
	    
	    hashConnect.put(discoverThread, mConnectThread);
	    hashConnect.get(discoverThread).start();
	
    }
    
    /**
     * The method creates a thread that manages the communication with the server after the connection is
     * established
     * @param socket - socket open during the initialisation phase
     * @param device - the device which we are connected to
     * @param discoverThread - the thread that manages the connection
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,DiscoverThread discoverThread) {
        Log.d(TAG, "connected");
      
    	if (hashConnected.get(discoverThread) != null) {hashConnected.get(discoverThread).cancel(); hashConnected.put(discoverThread,null);}	    // Start the thread to connect with the given device
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
    * The method writes the bytes pass as parameter in the first
    * connection that is able to do this
    * @param out - bytes to send
    */
    public void write(byte[] out) {
			Log.d(TAG, "trying to write ");
			for (ConnectedThread connectedThread : hashConnected.values()) {
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
    
  
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"-------received bluetooth devices--------");
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        	stopDiscovery();
        	// Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    		if(device.getName() != null && (device.getName().contains(Constants.NAME_IBEACON_TRANSMITTER))){
				synchronized (devices) {
					devices.add(device);
				}
    		}
            for (BluetoothDevice dev : devices) {
            	Log.d(TAG,"found devices"+dev.getAddress());
            }
            
       }
       startDiscovery();
		
	}
	
	/**
	 * The method stops discovering, if initialised before.
	 */
	public void stopDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
        	Log.d(TAG, "cancel discovering");
        	mBluetoothAdapter.cancelDiscovery();
        }
	}
    
    /**
     * This class implements a thread that tries to connect as soon a device in present
     * in the bluetooth list of the device.  
     * @author Andrea Corna - Lorenzo Fontana
     *
     */
    private class DiscoverThread extends Thread{
    	private HashSet<BluetoothDevice> addresses = new HashSet<BluetoothDevice>();
    	
    	/**
    	 * Default constructor
    	 */
    	public DiscoverThread(){
    		hashlock.put(this, true);
    	}
    	
    	
    	@SuppressWarnings("unchecked")
		public void run(){
    		while(true){
    			Log.d(TAG,"inside loop");
    			showDevices();
    			try {
					sleep(3000);
				} catch (InterruptedException e2) {
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
						}catch (NullPointerException e) {
							Log.d(TAG, "disconnected from current device ");
							hashdevices.remove(this);
							continue;
						}
		    		}
	    		
	    		}
    		}
	    		
    		
    	}
    	
    	/**
    	 * The method checks is the thread can connect to the device passed as
    	 * parameter
    	 * @param device - the device we want to connect to
    	 * @return true if the thread can connect, false otherwise
    	 */
     	private Boolean	checkCorrectDevice(BluetoothDevice device){
    		if(device.getName() == null){
    			Log.d(TAG, "device is null");
    			return false;
    		}
    		synchronized(hashdevices){
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
    		}
    		Log.d(TAG, "CHECKING CORRECTENESS RETURNONG TRUE_----------");

    		
    		return true;
    	}
    	
    	/**
    	 * The method keeps alive the connection with the remote device sending
    	 * hello packets.
    	 * @throws IOException
    	 * @throws NullPointerException
    	 */
    	private void keepAlive() throws IOException,NullPointerException{
    		while(true){
    				showHashConnected();
    				showDevices();
    				showHashConnect();
    				showHashDevices();
    			Log.d(TAG,"keeping alive");
				hashConnected.get(this).write("Hello".getBytes());
				try {
					sleep(500);
				} catch (InterruptedException e) {
					hashdevices.remove(this);
					e.printStackTrace();
				}
			
    		}
    	}
    }   
    
   
    
	
	/**
	 * The class implements the thread that connect the local device with the remote
	 * device.
	 * @author Andrea Corna - Lorenzo Fontana
	 *
	 */
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    private final DiscoverThread discoverThread;
	 
	    /**
	     * Constructor that instances the device to connect to and the discover thread that
	     * is managing the connection.
	     * @param device - device to connect to 
	     * @param discoverThread - thread that is managing the connection
	     */
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

	        try {
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
	 
	    /**
	     * The method closes the sockect.
	     */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
    
    
    /**
     * The class implements the thread that creates the output socket in order to
     * write to the remote device.
     * @author Andrea Corna - Lorenzo Fontana
     *
     */
    private class ConnectedThread extends Thread {
        private  final BluetoothSocket mmSocket;
        private  final OutputStream mmOutStream;
        private final DiscoverThread discoverThread;
        
        /**
         * The constructor initialises the socket and the discover thread and tries to
         * open the output of the socket
         * @param socket
         * @param discoverThread
         */
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
        
        /**
         * The method closes the socket.
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    
    /*METHODS FOR DEBUGS*/
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
	
   

}
