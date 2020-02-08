package com.leiflundgren.locationlogger.dbtables;

import android.content.ContentValues;
import android.location.Location;

public class LocationTable {

	public static final String TABLE_NAME = "location";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_ACCURANCY = "accurancy";

	public static final String[] ALL_COLUMNS = new String[] { 
		COLUMN_ID, COLUMN_TIMESTAMP, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ACCURANCY };

	
	// Database creation sql statement
	private static final String TABLE_CREATE = 
			"create table " + TABLE_NAME + "( " 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_TIMESTAMP + " integer, "
			+ COLUMN_LATITUDE + " real, "
			+ COLUMN_LONGITUDE + " real, "
			+ COLUMN_ACCURANCY + " real "
			+ ");";
	private final MyDbHelper dbHelper;
			
	
	
	
	public LocationTable(MyDbHelper dbHelper) {
		this.dbHelper = dbHelper;
		dbHelper.addTable(new MyDbHelper.TableInfo() {
			@Override
			public String getTableName() { return TABLE_NAME; }
			public String getCreateSql() { return TABLE_CREATE; }
		});
	}

	public void addLocation(Location loc) {
		ContentValues values = new ContentValues();
		values.put(LocationTable.COLUMN_TIMESTAMP, loc.getTime());
		values.put(LocationTable.COLUMN_LATITUDE, loc.getLatitude());
		values.put(LocationTable.COLUMN_LONGITUDE, loc.getLongitude());		
		if ( loc.hasAccuracy() )
			values.put(LocationTable.COLUMN_ACCURANCY, loc.getAccuracy());
		dbHelper.getDatabase().insert(LocationTable.TABLE_NAME, null, values);
	}

	
}
