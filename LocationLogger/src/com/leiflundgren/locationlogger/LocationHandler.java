package com.leiflundgren.locationlogger;

import com.leiflundgren.locationlogger.dbtables.MyDbHelper;
import com.leiflundgren.locationlogger.storage.LocationStorage;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHandler implements LocationListener  {
		

	private Settings settings;
	private EvtLogger logger;
	private LocationStorage locationStore;
	private LocationManager locationManager;
	private float minDistanceMeters = 250.0f;
	private long minTimeMillis = 30*1000;
	private String locationProviderName;
	private MyDbHelper dbHelper;
		
	
	public LocationHandler(MyDbHelper dbHelper) {
		super();
		this.dbHelper = dbHelper;
	}

	public void start() {
		
		this.locationStore = new LocationStorage(dbHelper);		
		
		// ---use the LocationManager class to obtain GPS locations---
		locationManager = (LocationManager) MainService.sInstance.getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
				minTimeMillis, 
				minDistanceMeters,
				this);
		locationProviderName = LocationManager.NETWORK_PROVIDER;
	}
	
	public void stop() {
		if ( locationManager != null ) {
			locationManager.removeUpdates(this);
			locationManager = null;
		}
		if ( dbHelper != null ) {
			dbHelper.close();
			dbHelper = null;
		}
		
	}

	private void log(String s) {
		if ( logger != null )
			logger.log(s);
	}
	private void log(String category, String s) {
		if ( logger != null )
			logger.log(category, s);
	}

	@Override
	public void onLocationChanged(Location location) {
		
		Settings settings = getSettings();
		if ( settings == null ) {
			settings = new Settings();
			setSettings(settings);			
		}
		
		final Location lastLocation = settings.getLastLocation();
		double change = lastLocation.distanceTo(location);
		@SuppressWarnings("unused")
		double accurancy = lastLocation.getAccuracy();
		
		settings.incrLocationChangedCalls();

		if ( lastLocation != null && change < 10.0 ) {
			log("Location change only " + change + ". Ignoring update");
			return;
		}  
		if ( lastLocation != null && location.hasAccuracy() && lastLocation.hasAccuracy() && lastLocation.getAccuracy() <= location.getAccuracy()) {
			log("Location change only " + change + ". Ignoring update");
			return;
		}  
		
		
		settings.incrLocationChangesDetected();
		settings.setLastLocation(location);
		
		float acc = ( location.hasAccuracy() ) ? location.getAccuracy() : 0;

		
		try {
			locationStore.addLocation(location);
		}
		catch ( Exception ex ) {
			log("Failed to add location to DB: " + ex.getMessage());
		}
		
		String s = location.getTime() + "," + location.getLatitude() + "," + location.getLongitude() + "," + acc + "," + change;		
		
		log("LocationChange", s);
			
		try {
			log("Location update: " + Utils.getTimeString(location.getTime()) + "," + location.getLatitude() + "," + location.getLongitude() + "," + acc + "," + change);
		}
		catch ( Exception ex2 ) {
		}
		
		MainService.getInstance().locationChanged(location, lastLocation);
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		log("onStatusChanged(provider" + provider + ", status=" + status + ", extras=" + extras + ")");
	}

	@Override
	public void onProviderEnabled(String provider) {
		log("onProviderEnabled(" + provider + ")");
	}

	@Override
	public void onProviderDisabled(String provider) {
		log("onProviderDisabled(" + provider + ")");
	}
	
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public EvtLogger getLogger() {
		return logger;
	}

	public void setLogger(EvtLogger logger) {
		this.logger = logger;
	}

	public float getMinDistanceMeters() {
		return minDistanceMeters;
	}

	public long getMinTimeMillis() {
		return minTimeMillis;
	}

	public void setMinDistanceMeters(float minDistanceMeters) {
		this.minDistanceMeters = minDistanceMeters;
	}

	public void setMinTimeMillis(long minTimeMillis) {
		this.minTimeMillis = minTimeMillis;
	}

	public boolean getGpsUse() {
		return LocationManager.GPS_PROVIDER.equals(locationProviderName);
	}

	public void setGpsUse(boolean b) {		
		if ( locationManager == null ) {
			locationProviderName = b ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
			return;
		}
		
		String provider = b ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
		
		log("Requesting location updated from " + provider);
		
		try {
			locationProviderName = null;
			locationManager.requestLocationUpdates(
					provider, 
					minTimeMillis, 
					minDistanceMeters,
					this);
			locationProviderName = provider;
		} catch (Exception e) {
			log("Request failed: " + e.getMessage());
		}	
	}

}
