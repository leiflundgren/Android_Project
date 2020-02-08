package com.leiflundgren.locationlogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import android.annotation.SuppressLint;

public class Utils {

	public static String getTimeString(long milliSecondsSinceEpoc){      
		 return getTimeString(new Date(milliSecondsSinceEpoc));  
	}
	public static String getTimeString(Date d){      
		 return Utils.sdf.format(d);  
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

	public static String getTimeString(){      
		 Calendar cal = Calendar.getInstance();  
		 return Utils.sdf.format(cal.getTime());  
	}

	@SuppressLint("SimpleDateFormat")
	static final SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT_NOW);
	static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

}
