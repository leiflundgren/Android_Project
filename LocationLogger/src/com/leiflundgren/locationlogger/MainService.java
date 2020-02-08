package com.leiflundgren.locationlogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.leiflundgren.locationlogger.MainService.ServiceInterface;
import com.leiflundgren.locationlogger.dbtables.MyDbHelper;
import com.leiflundgren.locationlogger.storage.EventStorage;
import com.leiflundgren.locationlogger.storage.LocationStorage;

import android.R.bool;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("unused")
public class MainService extends Service  {


	static MainService sInstance = null;
	private ServiceInterface serviceInterface = new ServiceInterface();


	
	public static final class MainServiceBinder extends Binder {
		/**
		 * Class for clients to access. Because we know this service always runs in
		 * the same process as its clients, we don't need to deal with IPC.
		 */
		
		MainService getService() {
			return sInstance;
		}

		public ServiceInterface getServiceInterface() {
			return MainService.getInstance();
		}
	}

	public class ServiceInterface {
	
	    //ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
		MainActivity client;
		Location currentLocation = null;

		public MainActivity getClient() {
			return client;
		}

		public void setClient(MainActivity client) {
			this.client = client;
			if ( client != null )
				client.locationChanged(MainService.this, currentLocation, currentLocation);
		}

		public boolean getGpsOn() {
			return MainService.this.locationHandler.getGpsUse();
		}

		public void setGpsOn(boolean useGps) {
			MainService.this.locationHandler.setGpsUse(useGps);
		}

		public void locationChanged(Location location, Location lastLocation) {
			currentLocation = location;
			try {
				final MainActivity client = this.client;
				if ( client != null ) {
					client.locationChanged(MainService.this, location, lastLocation);
				}
			}
			catch ( Exception ex ) {
				log("Failed to send location to client: " + ex.getMessage());
			}			
		}
		
	}
	
	public static ServiceInterface getInstance() {
		MainService svc = sInstance;
		if ( svc == null )
			return null;
		
		return svc.serviceInterface;		
	}
	
	
	



	public static final String ActivityName = "LocLogSvc";
	
	private EventStorage eventStore;
	private Settings settings;
	
	private MyDbHelper dbHelper;
	
	private NotificationManager notifyMgr;
	
	public MainService() {
		super();
	}


	

	

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new MainServiceBinder() ;

	private EvtLogger logger;
	private LocationHandler locationHandler;

	@Override
	public IBinder onBind(Intent intent) {
		log("Someone is binding, intent=" + intent);
		return mBinder;
	}
	

	/**
	 * Show a notification while this service is running.
	 */
	@SuppressWarnings("deprecation")
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.local_service_started);

		// Set the icon, scrolling text and timestamp
		
		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.service_name), text, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		if ( notifyMgr == null )
			notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		if ( notifyMgr != null )
			notifyMgr.notify(R.string.local_service_started, notification);
	}

	void log(String s) {
		if ( logger != null )
			logger.log(s);
		Log.d("MainService", s);
	}	
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		log("Service created");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int res = super.onStartCommand(intent, flags, startId);
		
		log("Service started, startId=" + startId);
		

		initDatabase();
		this.logger = new EvtLogger(this.eventStore);
		
		this.locationHandler = new LocationHandler(dbHelper);
		this.locationHandler.setLogger(logger);
		this.locationHandler.start();
		
		
		log("Database initialized and requesting updates");


		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
		
		return START_STICKY;
	}
	
	private void initDatabase() {
		this.dbHelper = new MyDbHelper(getBaseContext(), null);
		this.eventStore = new EventStorage(dbHelper);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		log("onDestroy");
		
		if ( locationHandler != null ) {
			locationHandler.stop();
			locationHandler = null;
		}
		
		// Cancel the persistent notification.
		if ( notifyMgr != null ) {
			notifyMgr.cancel(R.string.local_service_started);
			notifyMgr = null;
		}
		

		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
	}

	
}
