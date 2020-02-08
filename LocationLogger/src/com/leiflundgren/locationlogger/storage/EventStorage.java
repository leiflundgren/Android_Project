package com.leiflundgren.locationlogger.storage;

import com.leiflundgren.locationlogger.dbtables.MyDbHelper;
import com.leiflundgren.locationlogger.dbtables.NotableEventsTable;

public class EventStorage {
	// Database fields
	private NotableEventsTable dbHelper;
	public EventStorage(MyDbHelper dbHelper) {
		this.dbHelper = new NotableEventsTable(dbHelper);		
	}

	public void close() {
		dbHelper = null;
	}

	public void logEvent(String type, String desc) {
		dbHelper.logEvent(type, desc);
	}
}
