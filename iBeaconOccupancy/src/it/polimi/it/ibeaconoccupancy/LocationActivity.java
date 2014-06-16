package it.polimi.it.ibeaconoccupancy;

import it.polimi.it.ibeaconoccupancy.R;
import it.polimi.it.ibeaconoccupancy.compare.Logic;
import it.polimi.it.ibeaconoccupancy.helper.DataBaseHelper;
import it.polimi.it.ibeaconoccupancy.http.HttpHandler;
import it.polimi.it.ibeaconoccupancy.services.MonitoringService;
import it.polimi.it.ibeaconoccupancy.services.RangingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.radiusnetworks.ibeacon.IBeacon;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LocationActivity extends Activity {
	
	protected static final String TAG = "LocationActivity";
	
	
	
	
	private HashMap<String, String> beaconLocation;
	private SparseArray<String> answers;
	private PostTestOnServerTask taskPost;
	private String bestBeacon = new String();
	private BeaconReceiver receiver;
	private SharedPreferences prefs;



	
	private DataBaseHelper myDbHelper;


	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RangingService.ACTION);
		intentFilter.addAction(MonitoringService.ACTION);
		ActionBar ab = getActionBar(); 
        ab.setDisplayHomeAsUpEnabled(true);
		
		
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		myDbHelper = new DataBaseHelper(this);
		bestBeacon = (String)this.getIntent().getSerializableExtra("BestBeacon");
		beaconLocation = (HashMap<String, String>) this.getIntent().getSerializableExtra("beaconLocation");
		receiver =  new BeaconReceiver();

		registerReceiver(receiver, intentFilter);
		setupLayout();
     
	}
	
	/**
	 * Creating the list of possible answers taking random rooms from the DB
	 */
	private void setupLayout() {
		TextView textView = (TextView) findViewById(R.id.question);
		textView.setText("Where are you?");
		Button asw1 = (Button) findViewById(R.id.answer1);
		Button asw2 = (Button) findViewById(R.id.answer2);
		Button asw3 = (Button) findViewById(R.id.answer3);
		Button asw4 = (Button) findViewById(R.id.answer4);
		Button asw5 = (Button) findViewById(R.id.answer5);
		Button asw6 = (Button) findViewById(R.id.answer6);
		Button asw7 = (Button) findViewById(R.id.answer7);
		
		ArrayList<String> rooms = new ArrayList<String>(beaconLocation.values());
		Collections.shuffle(rooms);
		
		answers = new SparseArray<String>();
		setButtonText(asw1,0, rooms);
		setButtonText(asw2,1,rooms);
		setButtonText(asw3,2,rooms);
		setButtonText(asw5,3,rooms);
		setButtonText(asw6,4,rooms);
		setButtonText(asw4,5,rooms);
		asw7.setText("Nessuna");

		
	}
	
	
	/**
	 * Setting the text to the buttons and adding field to the sparse array Answers
	 * If the element at that index of the array doesn't exist(aka not enough rooms to add to the answers) button is hidden
	 * @param but	button to which add the text
	 * @param index	index in the rooms array where to find the value
	 * @param rooms	array of string of the different rooms from which take the value
	 */
	private void setButtonText(Button but, int  index, ArrayList<String> rooms){
		try {
			String randomRoom = rooms.get(index);
			but.setText(randomRoom);
			answers.append(but.getId(), randomRoom);
		} catch (IndexOutOfBoundsException e) {
			but.setVisibility(View.GONE);
			Log.d(TAG, "Ci devono essere almeno 3 entry ne database");
		}
		
	}
	
	/**
	 * Method called by the buttons of the view which sends to the server the information about the answer
	 * @param view button which has called this method
	 */
	public void checkAnswer(View view){
		Log.d(TAG, "in beacon loaction "+bestBeacon);
		IBeacon best = Logic.getInstance().getBestLocation();
		//String correctRoom = beaconLocation.get(bestBeacon);
		String correctRoom = beaconLocation.get(best.getProximityUuid()+best.getMajor()+best.getMinor());
		
		Log.d(TAG," correct room "+correctRoom);
		
		//checking if answer is different from Nessuna
		if (view.getId() != R.id.answer7) {
			String answerRoom = answers.get(view.getId());
			Log.d(TAG," answer room "+answerRoom);
			if (correctRoom!=null){
				if(answerRoom.equals(correctRoom)){
					Log.d(TAG, "correct specific answer "+answerRoom+" correct"+correctRoom);
					sendAnswer( answerRoom, correctRoom, 1);
				}else{
					Log.d(TAG, "wrong specific answer "+ answerRoom+" "+correctRoom);
					sendAnswer( answerRoom, correctRoom, 0);
				}
			}
			else {
				Log.d(TAG, "wrong specific answer "+ answerRoom+"Nessuna");
				sendAnswer( answerRoom, "Nessuna", 0);

			
			}	
		}
		
		//checking correctness when answer is  Nessuna
		else {
			Log.d(TAG, "correctRoom  "+correctRoom);
			//check if in the other answers there is the correct one
			if (correctRoom==null /*|| answers.indexOfValue(correctRoom)<0*/) {
				
				sendAnswer( "Nessuna", "Nessuna", 1);
				Log.d(TAG, "correct generic answer  "+correctRoom);
				
			}
			else {
				sendAnswer( "Nessuna",correctRoom, 0);
				Log.d(TAG, "wrong generic answer "+" correct"+correctRoom);
			}
		}
		Toast.makeText(getApplicationContext(), "Answer submitted!", Toast.LENGTH_SHORT).show();
		
		this.finish();
	}
	
	/**
	 * This method wil handle the submit to the server of the answer given
	 * If there is no internet connection the answers will be saved in an internal SQLite database and sent the next time when I'll try to send an answer with the
	 * internet connection
	 * @param answerRoom  
	 * @param correctRoom
	 * @param correct
	 */
	private void sendAnswer(String answerRoom,String correctRoom,int correct){
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
 
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork){
    		prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		boolean onClient = prefs.getBoolean("pref_logic", true);
    		HttpHandler http;
    		if(onClient)
    			http =new HttpHandler(Constants.ADDRESS_TEST_SERVER_CLIENT);
    		else
    			http =new HttpHandler(Constants.ADDRESS_TEST_SERVER_LEARNING);

			taskPost = new PostTestOnServerTask(http, answerRoom, correctRoom, correct);
			taskPost.execute(null,null,null);
			Log.d(TAG,"connection available Sending cached data");
			sendCachedAnswers();
			
			
        }	
        else{
			saveDataSqlite(answerRoom, correctRoom, correct);
			Log.d(TAG,"Caching answer");
			
        }	
		
		
	}
	
	/**
	 * This method will  send to the server all the answers which has been given when there was no internet connection
	 */
	@SuppressWarnings("static-access")
	private void sendCachedAnswers(){
		Log.d(TAG, "");
		SQLiteDatabase myDb = myDbHelper.getReadableDatabase();
		Cursor cursor = myDb.query(myDbHelper.TABLE_SAVED_ANSWERS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(cursor.isAfterLast()==false){
			String answer = cursor.getString(1);
			String correct_answer  = cursor.getString(2);
			int correct  = cursor.getInt(3);
			
			HttpHandler http =new HttpHandler("http://ibeacon.no-ip.org/ibeacon/test");
			taskPost = new PostTestOnServerTask(http, answer, correct_answer, correct);
			taskPost.execute(null,null,null);
			
			Log.d(TAG, "In SQLITEDB  answer "+answer+" correct_answer "+correct_answer+" correct "+correct);
			cursor.moveToNext();
		}
		cursor.close();
		myDb.delete(myDbHelper.TABLE_SAVED_ANSWERS,null,null);

	}
	
	private boolean saveDataSqlite(String answer,String correct_answer,int correct){
		boolean createSuccessful =false;
		SQLiteDatabase myDb = myDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("answer_room", answer);
		values.put("correct_room", correct_answer);
		values.put("correct", correct);
		createSuccessful = myDb.insert("saved_answer", null, values) > 0;
	    myDb.close();

	    return createSuccessful;


	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        case R.id.action_settings:
        	return true;
        default:
            return super.onOptionsItemSelected(item);
    }
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_location,
					container, false);
			return rootView;
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	
	private class PostTestOnServerTask extends AsyncTask<Void, Void, Void>{

		private HttpHandler http;
		private String answerRoom;
		private String correctRoom;
		private int correct;
		
		public PostTestOnServerTask(HttpHandler http, String answerRoom, String correctRoom, int correct){
			this.http = http;
			this.answerRoom = answerRoom;
			this.correct = correct;
			this.correctRoom = correctRoom;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			http.postAnswer(answerRoom, correctRoom, correct);
			return null;
		}
		
	}
	
	
	/**
	 * Class which receive the message sent by the RangingService(information about the beacons in range) and set the bestBeacon attribute 
	 *
	 */
	private class BeaconReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {	
			
			if (intent.getExtras().getBoolean("exitRegion")){
				Log.d(TAG, "notify exit");
				bestBeacon=null;
			}
			else {
				@SuppressWarnings("unused")
				ArrayList<String> beacons = intent.getStringArrayListExtra("BeaconInfo");
				String strongerBeacon = intent.getExtras().getString("StrongerBeacon");
				bestBeacon = strongerBeacon.intern();
				Log.d(TAG, "on Receive strong beacon "+bestBeacon);
				
			}
		}
	}
	
		

}
