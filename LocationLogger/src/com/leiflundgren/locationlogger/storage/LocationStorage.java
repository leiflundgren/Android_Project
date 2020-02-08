package com.leiflundgren.locationlogger.storage;

import com.leiflundgren.locationlogger.dbtables.LocationTable;
import com.leiflundgren.locationlogger.dbtables.MyDbHelper;

import android.location.Location;

public class LocationStorage {
	// Database fields
	private LocationTable dbHelper;
	public LocationStorage(MyDbHelper dbHelper) {
		this.dbHelper = new LocationTable(dbHelper);
	}

	public void close() {
		dbHelper = null;
	}

	public void addLocation(Location loc) {
		dbHelper.addLocation(loc);
	}
	
	
}
