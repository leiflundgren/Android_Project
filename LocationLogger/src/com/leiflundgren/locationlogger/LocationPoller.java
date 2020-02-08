package com.leiflundgren.locationlogger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationPoller {

	public static final String ActivityName = MainActivity.ActivityName;
		
	public class LocationPollingTask extends TimerTask {

		@Override
		public void run() {
			pollLocation();
		}

	}	
	
	public static String[] prioritizedLocationProviders = new String[] { LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER };

	private static String findProviderToUse(LocationManager locationManager) {

		List<String> existingProviders = locationManager.getAllProviders();
		for ( String provToUse : prioritizedLocationProviders ) {
			
			if ( existingProviders.contains(provToUse) ) {
				return provToUse;
			}
		}
		
		return null;		
	}
	
	public LocationPoller(LocationManager locationManager) {
		//super("LocationPoller");
		

		this.locationListener = new MyLocationListener();
	
		String provToUse = findProviderToUse(locationManager);
		if ( provToUse == null ) 
			throw new RuntimeException("Cannot start, no location Manager exists!");
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(provToUse, 0, 0, locationListener);
		
				
		
		//pollingTimer = new Timer("LocationPollerTimer", true);

		//TimerTask pollingTask = new LocationPollingTask();
		//pollingTimer.scheduleAtFixedRate(pollingTask, 5000, 60000);

	}
	
	public void stop() {
		if (pollingTimer != null ) {
			pollingTimer.cancel();
			pollingTimer = null;		
		}
	}
	
	private Timer pollingTimer;
	private LocationListener locationListener; 
	
	private Object listenersLock = new Object();
	private Collection<LocationChangedListener> listeners = new LinkedList<LocationChangedListener>();
	
	public void addListener(LocationChangedListener listener) {
		synchronized (listenersLock) {
			listeners.add(listener);
		}
	}

	public void removeListener(LocationChangedListener listener) {
		synchronized (listenersLock) {
			listeners.remove(listener);
		}
	}
	
	
	private void fireEvent(Location loc) {
//		Collection<LocationChangedListener> listeners;
//		synchronized (listenersLock) {
//			listeners = new Vector<LocationChangedListener>(this.listeners);			
//		}		
//		
//		for ( LocationChangedListener listener : listeners ) {
//			locationM LocationHandler.localtionChanged(listener, this, loc);
//		}
	}
	
	
	
	// Define a listener that responds to location updates
	private class MyLocationListener implements android.location.LocationListener {

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {
	    	Log.v(ActivityName, "LocationProvider " + provider + " enabled.");
	    }

	    public void onProviderDisabled(String provider) {
	    	Log.v(ActivityName, "LocationProvider " + provider + " disabled.");
	    }

		@Override
		public void onLocationChanged(android.location.Location location) {
		      // Called when a new location is found by the network location provider.
		      makeUseOfNewLocation(location);
		}
	  };
	
	public void pollLocation() {
		
	}

	public void makeUseOfNewLocation(android.location.Location location) {
		fireEvent(location);
	}
	
	
}
