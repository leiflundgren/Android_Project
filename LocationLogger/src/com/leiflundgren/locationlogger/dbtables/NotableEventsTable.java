package com.leiflundgren.locationlogger.dbtables;

import com.leiflundgren.locationlogger.Utils;

import android.content.ContentValues;

public class NotableEventsTable {

	public static final String TABLE_NAME = "notable_events";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_TYPE = "event_type";
	public static final String COLUMN_DESCRIPTION = "description";

	// Database creation sql statement
	private static final String TABLE_CREATE = 
			"create table " + TABLE_NAME + "( " 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_TIMESTAMP + " text, "
			+ COLUMN_TYPE + " text not null, "
			+ COLUMN_DESCRIPTION + " text "
			+ ");";
			
	private class TableInfo implements MyDbHelper.TableInfo {

		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public String getCreateSql() {
			return TABLE_CREATE;
		}
		
	}

	private MyDbHelper dbHelper;
	
	
	
	public NotableEventsTable(MyDbHelper dbHelper) {
		this.dbHelper = dbHelper;
		this.dbHelper.addTable(new TableInfo());
	}
	
	public void logEvent(String type, String desc) {
		ContentValues values = new ContentValues();
		values.put(NotableEventsTable.COLUMN_TIMESTAMP,  Utils.getTimeString());
		values.put(NotableEventsTable.COLUMN_TYPE, type);
		values.put(NotableEventsTable.COLUMN_DESCRIPTION, desc);
		dbHelper.getDatabase().insert(NotableEventsTable.TABLE_NAME, null, values);
	}
}
