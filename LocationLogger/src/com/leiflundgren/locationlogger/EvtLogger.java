package com.leiflundgren.locationlogger;

import java.util.Date;

import android.util.Log;

import com.leiflundgren.locationlogger.storage.EventStorage;

public class EvtLogger {

	private EventStorage eventStore;

	public EvtLogger(EventStorage eventStore) {
		this.eventStore = eventStore;
	}
	public EvtLogger() {
		this.eventStore = null;
	}

	void log(String s) {
		log("log", s);
	}
	
	void log(String category, String s) {
		try {
			EventStorage eventStore = this.getEventStore();
			if ( eventStore == null )
				return;
			eventStore.logEvent(category, Utils.getTimeString(new Date()) + " " + s);
		}
		catch ( Exception ex2 ) {
		}
		
		try {
			Log.i(MainService.ActivityName, s);
		}
		catch ( Exception ex2 ) {
		}
	}

	public EventStorage getEventStore() {
		return eventStore;
	}

	public void setEventStore(EventStorage eventStore) {
		this.eventStore = eventStore;
	}	

}
