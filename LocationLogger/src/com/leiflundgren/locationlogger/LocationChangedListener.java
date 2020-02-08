package com.leiflundgren.locationlogger;

import android.location.Location;

public interface LocationChangedListener {
	void localtionChanged(Object sender, Location location);
}