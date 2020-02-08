package com.leiflundgren.locationlogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.leiflundgren.locationlogger.MainService.MainServiceBinder;
import com.leiflundgren.locationlogger.dbtables.LocationTable;
import com.leiflundgren.locationlogger.dbtables.MyDbHelper;
import com.leiflundgren.locationlogger.storage.EventStorage;
import com.leiflundgren.locationlogger.storage.LocationStorage;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class MainActivity extends Activity {

	public static final String ActivityName = "LocLogAct";
	

	private EventStorage eventStore;
	private Settings settings;
	private final DecimalFormat sevenSigDigits = new DecimalFormat("0.#######");
	
	private final Intent loggerServiceIntent  = new Intent(this, MainService.class);

	
	private MyDbHelper dbHelper;
	
	
	public MainActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		if ( savedInstanceState != null )
			onRestoreInstanceState(savedInstanceState);
		
		dbHelper = new MyDbHelper(this, null); 
		
		eventStore = new EventStorage(dbHelper);
		
		setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.ButtonStart)).setOnClickListener( mStartListener);
        ((Button)findViewById(R.id.ButtonStop)).setOnClickListener(mStopListener);
        ((Button)findViewById(R.id.ButtonBind)).setOnClickListener(mBindListener);
        ((Button)findViewById(R.id.ButtonExport)).setOnClickListener(mExportListener);
        
        ((CheckBox)findViewById(R.id.CheckGPS)).setOnCheckedChangeListener( mGpsChangedListener );
		
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		final String allProviders = join(locationManager.getAllProviders(), ", " );
		appendLog("All providers: " +  allProviders);
		
		appendLog("#time,lat,long,accurancy,change(m),  " + getTimeString() );
	}
	
	@Override protected void onRestart() {
		super.onRestart();
		if ( settings != null )
			updateDisplayText(0.0);
	}
	
	@Override
	protected void onStop() {
		
		appendLog("onStop() called");
		
		unbindLoggerService();
		super.onStop();
	}
	
	@Override 
	protected void onStart() {
		super.onStart();
		
		appendLog("onStart() called");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if ( settings != null ) {
			appendLog("Had old settings when restore happended. Using new settings.");
		}
		settings = new Settings(savedInstanceState);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//bindLoggerService();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unbindLoggerService();
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if ( settings != null)
			settings.save(outState);
	}
	
	@Override
	protected void onDestroy() {
		appendLog("# onDestroy " + getTimeString());
		

		eventStore.close();
		
		dbHelper.close();
		
		settings = null;

		super.onDestroy();
	}

	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}

	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";   
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

	private static String getTimeString(){      
		 Calendar cal = Calendar.getInstance();  
		 return sdf.format(cal.getTime());  
	}
	
	private static String getTimeString(long milliSecondsSinceEpoc){      
		 return sdf.format(new Date(milliSecondsSinceEpoc));  
	}
	
	public void appendLog(String text)
	{       		
		EventStorage eventStore = this.eventStore;
		if ( eventStore == null )
			return;
		
		eventStore.logEvent("log", text);
		
		updateDisplayText(0.0, "\nlog: " + text + "\n");
	}	
	
	/** Should get distance in meters between two long/lat
	 * http://stackoverflow.com/questions/639695/how-to-convert-latitude-or-longitude-to-meters
	 * @return
	 */
	public static double measureDistance(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
		double R = 6378.137; // Radius of earth in KM
		double dLat = (lat2 - lat1) * Math.PI / 180;
		double dLon = (lon2 - lon1) * Math.PI / 180;
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;
	    return d * 1000; // meters
	}
	
//	private int locationChangedCalls = 0, locationChangesDetected = 0;
//	
//	private Location lastLocation = null;
	

	private void updateDisplayText(double change) { updateDisplayText(change, null); }
	private void updateDisplayText(String msg) { updateDisplayText(0.0, msg); }
	
	private void updateDisplayText(double change, String msg) {
		String location = "";
		String sTime = Utils.getTimeString();
		String s2; 

		if ( msg != null && !msg.endsWith("\n"))
			msg += "\n";
		
		if ( settings != null ) {
		
			if ( settings.getLastLocation() != null ) {
				final Location l = settings.getLastLocation();
				final String sChange = change == 0.0 ? "" : ("\nChange:" + change);
				location += 
					getTimeString(l.getTime())
					+ "\nLong: " + l.getLatitude() 
					+ "\nLat: " + l.getLongitude() 
					+ "\nAcc: " + l.getAccuracy() 
					+ "\nProvider: " + l.getProvider()
					+  sChange;
	
			}
	
			s2 = 
				sTime
				+ ( msg != null ? msg : "" )
				+ " (m)\nlocationChangedCalls: " + settings.getLocationChangedCalls() 
				+ "\nlocationChangesDetected: " + settings.getLocationChangesDetected() 
				+ "\nSave/restore count: " + settings.getSavedCount() + "/" + settings.getRestoreCount();
		}			
		else {
			s2 = 
				sTime
				+ ( msg != null ? msg : "" )
				+ "\nNo old settings exists!";
		}
	
		try {
			TextView tv = (TextView) findViewById(R.id.TextOutput);
			tv.setText( s2 );
		}
		catch ( Exception ex ) {
			Log.w(ActivityName, "Failed to set test-data");
		}
	}

	private void updatGpsCheckbox(boolean checked) {
		try {
			CheckBox chk = (CheckBox) findViewById(R.id.CheckGPS);
			chk.setChecked(checked);
		}
		catch ( Exception ex ) {
			Log.w(ActivityName, "Failed to set test-data");
		}
	}

	public static String join(Collection<?> collection, String delimiter)
	{
	    StringBuilder sb = new StringBuilder ();
	    for (Object item : collection)
	    {
	    	if (item == null) continue;
	    	sb.append (item).append (delimiter);
	    }
	    sb.setLength (sb.length () - delimiter.length ());
	    return sb.toString ();
	}
	
	
	private OnClickListener mStartListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
            startService(new Intent(MainActivity.this, MainService.class));
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
            stopService(new Intent(MainActivity.this, MainService.class));
        }
    };

    private OnClickListener mExportListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
    		doExport();
    	}
    };
    
    private OnClickListener mBindListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
    		MainActivity.this.bindLoggerService();
    	}
    };
    
    
    
    private OnCheckedChangeListener mGpsChangedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if ( serviceInterface != null )
				try {
					serviceInterface.setGpsOn(isChecked);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	};
	
	private final class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(MainActivity.ActivityName, "onServiceConnected(name=" + name.flattenToString());
			if ( name.equals(mainServiceName)) {
				serviceInterface = null;				
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			long waitMs = new Date().getTime() - serviceConnectionAttemptTime.getTime();

			Log.i(MainActivity.ActivityName, "onServiceConnected(name=" + name.flattenToString() + ")  took " + waitMs + "ms.");
			if ( name.equals(mainServiceName)) {
				serviceInterface = ((MainServiceBinder) service).getServiceInterface();
				
				serviceInterface.setClient(MainActivity.this);
				updatGpsCheckbox(serviceInterface.getGpsOn());
							
			}
		}
	}

	/**
	 * Handler of incoming messages from service.
	 */
	static class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
//	            case MainService.MSG_SET_VALUE:
//	                mCallbackText.setText("Received from service: " + msg.arg1);
//	                break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
	}

	
	MainService.ServiceInterface serviceInterface;
	ComponentName mainServiceName;


	private ServiceConnection serviceConnection;
	private Date serviceConnectionAttemptTime;
    
    @Override
    public ComponentName startService(Intent service) {
    	mainServiceName = super.startService(service);
    	if ( mainServiceName == null ) {
        	updateDisplayText(0.0, "Failed to start " + service + "\n");
        	return null;
    	}    	
    	
    	final String cmpName = mainServiceName.flattenToString();
		updateDisplayText(0.0, "Starting service " + cmpName + "\n");
		//bindLoggerService();

    	
    	return mainServiceName;
    }


	
	public void bindLoggerService() {
		if ( serviceInterface == null ) {
			if ( this.serviceConnection == null ) {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				this.serviceConnection = new MyServiceConnection();
				this.serviceConnectionAttemptTime = new Date();
				boolean res = bindService(loggerServiceIntent, this.serviceConnection, BIND_AUTO_CREATE);
				Log.d(ActivityName, "Attempt to bind to service " + loggerServiceIntent + " --> " + res);
				
								
				if ( res ) {
		    		updateDisplayText(0.0, "Successfull bind to service!\n");
				}
				else {
//					unbindService(serviceConnection);
//					serviceConnection = null;
		    		updateDisplayText(0.0, "Attempt to bind to service returned false\n");

				}
			}
			else {
				long waitMs = new Date().getTime() - serviceConnectionAttemptTime.getTime();
				Log.d(ActivityName, "Had already an existing serviceConnection-object. So we should currently just be waiting for a connection... Have been waiting for " + (waitMs/1000) + " seconds." );
			}
		}
	}
	
	private void unbindLoggerService() {
		if ( serviceInterface != null ) {
			Log.d(ActivityName, "Unbinding from service");
			serviceInterface.setClient(null);
			serviceInterface = null;
			unbindService(serviceConnection);
    		updateDisplayText(0.0, "Unconnected from service\n");
		}
	}
    
    @Override
    public boolean stopService(Intent name) {
    	updateDisplayText(0.0, "Stopping service\n");
    	unbindLoggerService();
    	return super.stopService(name);
    }

	private void doExport() {
		// export the db contents to a kml file
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = openOrCreateDatabase(MyDbHelper.DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE, null);
			cursor = db.rawQuery("SELECT * " +
                    " FROM " + LocationTable.TABLE_NAME +
                    " ORDER BY " + LocationTable.COLUMN_TIMESTAMP + " ASC",
                    null);
            
			int timestampColumnIndex = cursor.getColumnIndexOrThrow(LocationTable.COLUMN_TIMESTAMP);
            int latitudeColumnIndex = cursor.getColumnIndexOrThrow(LocationTable.COLUMN_LATITUDE);
            int longitudeColumnIndex = cursor.getColumnIndexOrThrow(LocationTable.COLUMN_LONGITUDE);
            int accuracyColumnIndex = cursor.getColumnIndexOrThrow(LocationTable.COLUMN_ACCURANCY);
            
			if (cursor.moveToFirst()) {
				StringBuffer fileBuf = new StringBuffer();
				String beginTimestamp = null;
				String endTimestamp = null;
				String timestamp = null;
				do {
					timestamp = cursor.getString(timestampColumnIndex);
					if (beginTimestamp == null) {
						beginTimestamp = timestamp;
					}
					double latitude = cursor.getDouble(latitudeColumnIndex);
					double longitude = cursor.getDouble(longitudeColumnIndex);
					double accuracy = cursor.getDouble(accuracyColumnIndex);
					fileBuf.append(sevenSigDigits.format(longitude)+","+sevenSigDigits.format(latitude)+"\n");
				} while (cursor.moveToNext());
				
				endTimestamp = timestamp;
				
				closeFileBuf(fileBuf, beginTimestamp, endTimestamp);
				String fileContents = fileBuf.toString();
				Log.d(ActivityName, fileContents);
				File sdcard_path = Environment.getExternalStorageDirectory();
				File sdDir = new File(sdcard_path, "GPSLogger");
				sdDir.mkdirs();
				File file = new File(sdDir, zuluFormat(beginTimestamp) + ".kml");
				FileWriter sdWriter = new FileWriter(file, false);
				sdWriter.write(fileContents);
				sdWriter.close();
    			Toast.makeText(getBaseContext(),
    					"Export completed!",
    					Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getBaseContext(),
						"I didn't find any location points in the database, so no KML file was exported.",
						Toast.LENGTH_LONG).show();
			}
		} catch (FileNotFoundException fnfe) {
			Toast.makeText(getBaseContext(),
					"Error trying access the SD card.  Make sure your handset is not connected to a computer and the SD card is properly installed",
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Error trying to export: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}
	
	private static void initFileBuf(StringBuffer fileBuf, Map<String, String> valuesMap) {
		fileBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fileBuf.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
		fileBuf.append("  <Document>\n");
		fileBuf.append("    <name>"+valuesMap.get("FILENAME")+"</name>\n");
		fileBuf.append("    <description>GPSLogger KML export</description>\n");
		fileBuf.append("    <Style id=\"yellowLineGreenPoly\">\n");
		fileBuf.append("      <LineStyle>\n");
		fileBuf.append("        <color>7f00ffff</color>\n");
		fileBuf.append("        <width>4</width>\n");
		fileBuf.append("      </LineStyle>\n");
		fileBuf.append("      <PolyStyle>\n");
		fileBuf.append("        <color>7f00ff00</color>\n");
		fileBuf.append("      </PolyStyle>\n");
		fileBuf.append("    </Style>\n");
		fileBuf.append("    <Placemark>\n");
		fileBuf.append("      <name>Absolute Extruded</name>\n");
		fileBuf.append("      <description>Transparent green wall with yellow points</description>\n");
		fileBuf.append("      <styleUrl>#yellowLineGreenPoly</styleUrl>\n");
		fileBuf.append("      <LineString>\n");
		fileBuf.append("        <extrude>"+valuesMap.get("EXTRUDE")+"</extrude>\n");
		fileBuf.append("        <tessellate>"+valuesMap.get("TESSELLATE")+"</tessellate>\n");
		fileBuf.append("        <altitudeMode>"+valuesMap.get("ALTITUDEMODE")+"</altitudeMode>\n");
		fileBuf.append("        <coordinates>\n");
	}
	
	private static void closeFileBuf(StringBuffer fileBuf, String beginTimestamp, String endTimestamp) {
		fileBuf.append("        </coordinates>\n");
		fileBuf.append("     </LineString>\n");
		fileBuf.append("	 <TimeSpan>\n");
		String formattedBeginTimestamp = zuluFormat(beginTimestamp);
		fileBuf.append("		<begin>"+formattedBeginTimestamp+"</begin>\n");
		String formattedEndTimestamp = zuluFormat(endTimestamp);
		fileBuf.append("		<end>"+formattedEndTimestamp+"</end>\n");
		fileBuf.append("	 </TimeSpan>\n");
		fileBuf.append("    </Placemark>\n");
		fileBuf.append("  </Document>\n");
		fileBuf.append("</kml>");
	}

	private static String zuluFormat(String beginTimestamp) {
		// turn 20081215135500 into 2008-12-15T13:55:00Z
		StringBuffer buf = new StringBuffer(beginTimestamp);
		buf.insert(4, '-');
		buf.insert(7, '-');
		buf.insert(10, 'T');
		buf.insert(13, ':');
		buf.insert(16, ':');
		buf.append('Z');
		return buf.toString();
	}

	public void locationChanged(Object sender, Location location, Location lastLocation) {
		if ( settings == null )
			settings = new Settings();
		double dist = measureDistance(location.getLatitude(), location.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude());
		settings.setLastLocation(lastLocation);
		updateDisplayText(dist, "Service signalled location change");
		
		
	}

}
