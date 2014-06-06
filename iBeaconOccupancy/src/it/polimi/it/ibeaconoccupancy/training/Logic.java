package it.polimi.it.ibeaconoccupancy.training;

import it.polimi.it.ibeaconoccupancy.Constants;
import it.polimi.it.ibeaconoccupancy.http.HttpHandler;

import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;

public class Logic {
	
	private static Logic instance;
	private Collection<IBeacon> trainingInformation;
	private HttpHandler http;

	
	private Logic(){
		http = new HttpHandler(Constants.ADDRESS_TRAINING_LEARNING);
	}
	
	public static Logic getInstance(){
		if(instance == null){
			instance = new Logic();
		}
		return instance;
	}
	
	public void training(String answer, String MAC){
		http.postForTraining(trainingInformation, answer, MAC);
	}
	
	
	
	public void updateInformation(Collection<IBeacon> newInformation){
		trainingInformation = newInformation;
		
	}
	
	
	

}



