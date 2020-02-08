package com.leiflundgren.locationlogger;


import android.location.Location;
import android.os.Bundle;

public class Settings {

	private int locationChangedCalls = 0;
	private int locationChangesDetected = 0;
	private Location lastLocation;
	private int savedCount = 0;
	private int restoreCount = 0;
		
	public Settings() {		
		setLocationChangesDetected(0);
		setLocationChangedCalls(0);
		setLastLocation(new Location((String)null));
	}

	public Settings(Bundle savedInstanceState) {
		restoreCount++;
		
		setLocationChangesDetected(savedInstanceState.getInt("locationChangesDetected"));
		setLocationChangedCalls(savedInstanceState.getInt("locationChangedCalls"));
		setLastLocation(restoreLocation(savedInstanceState, "lastLocation"));
		setRestoreCount(savedInstanceState.getInt("restoreCount"));
		setSavedCount(savedInstanceState.getInt("savedCount"));
	}

	public void save(Bundle state) {
		savedCount++;
		
		state.putInt("locationChangesDetected", getLocationChangesDetected());
		state.putInt("locationChangedCalls", getLocationChangedCalls());
		state.putInt("restoreCount", getRestoreCount());
		state.putInt("savedCount", getSavedCount());
		saveLocation(state, "lastLocation", getLastLocation());
	}

	

	public int getLocationChangedCalls() {
		return locationChangedCalls;
	}
	public void setLocationChangedCalls(int locationChangedCalls) {
		this.locationChangedCalls = locationChangedCalls;
	}

	public int getLocationChangesDetected() {
		return locationChangesDetected;
	}
	public void setLocationChangesDetected(int locationChangesDetected) {
		this.locationChangesDetected = locationChangesDetected;
	}

	public Location getLastLocation() {
		return lastLocation;
	}
	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}

	

	public int getSavedCount() {
		return savedCount;
	}

	public void setSavedCount(int savedCount) {
		this.savedCount = savedCount;
	}

	public int getRestoreCount() {
		return restoreCount;
	}

	public void setRestoreCount(int restoreCount) {
		this.restoreCount = restoreCount;
	}

	private static Location restoreLocation(Bundle savedInstanceState, String settingPrefix) {
	    Location location = new Location((String)null);

        location.setLatitude( savedInstanceState.getDouble(settingPrefix + "Latitude", 17.7));
        location.setLongitude(savedInstanceState.getDouble(settingPrefix + "Longitude", 59.3));
        location.setAccuracy((float) savedInstanceState.getDouble(settingPrefix + "Accurancy"));
        location.setTime(savedInstanceState.getLong(settingPrefix + "Time"));

        return location;
	}

	private static void saveLocation(Bundle savedInstanceState, String settingPrefix, Location location) {
        savedInstanceState.putDouble(settingPrefix + "Latitude", location.getLatitude());
        savedInstanceState.putDouble(settingPrefix + "Longitude", location.getLongitude());
        savedInstanceState.putDouble(settingPrefix + "Accurancy", location.getAccuracy());
        savedInstanceState.putLong(settingPrefix + "Time", location.getTime());
	}

	public void incrLocationChangedCalls() {
		++locationChangedCalls;
	}

	public void incrLocationChangesDetected() {
		++locationChangesDetected;
	}

}
