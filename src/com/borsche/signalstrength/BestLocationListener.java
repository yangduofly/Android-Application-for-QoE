package com.borsche.signalstrength;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

public class BestLocationListener extends Observable implements
LocationListener {
	public static final long LOCATION_UPDATE_MIN_TIME = 0;
	public static final long LOCATION_UPDATE_MIN_DISTANCE = 0;
	public static final long SLOW_LOCATION_UPDATE_MIN_TIME = 1000 * 30 * 1;
	public static final long SLOW_LOCATION_UPDATE_MIN_DISTANCE = 20;
	// public static final float REQUESTED_FIRST_SEARCH_ACCURACY_IN_METERS =
	// 100.0f;
	// public static final int REQUESTED_FIRST_SEARCH_MAX_DELTA_THRESHOLD = 1000
	// * 60 * 5;
	public static final long LOCATION_UPDATE_MAX_DELTA_THRESHOLD = 1000 * 60 * 5;

	private Location mLastLocation;
	private CellInfoManager cellManager;
	private WifiInfoManager wifiManager;
	private CellLocationManager cellLocationManager;
	private Context context;
	private Logger logger;



	private LocationManager getSystemService(String locationService) {
		// TODO Auto-generated method stub
		return null;
	}

	public BestLocationListener(CellInfoManager cellMgr,
			WifiInfoManager wifiMgr, Context paramContext) {
		super();
		cellManager = cellMgr;
		wifiManager = wifiMgr;
		context = paramContext;
		logger = Logger.getLogger(BestLocationListener.class);
	}

	private void debug(Object paramObject) {
		logger.debug(paramObject);
		String str = String.valueOf(paramObject);
		Toast.makeText(this.context, str, Toast.LENGTH_SHORT).show();
	}

	public void onLocationChanged(Location location) {
		logger.debug("onLocationChanged: " + location);
		updateLocation(location);
	}

	public void onProviderDisabled(String provider) {
		// do nothing.
		logger.debug("onProviderDisabled: " + provider);
	}

	public void onProviderEnabled(String provider) {
		// do nothing.
		logger.debug("onProviderEnabled: " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// do nothing.
		logger.debug("onStatusChanged: " + provider + " " + status);
	}
	
	public int sendCount() {
		 System.out.println("test4"+cellLocationManager.sendCount());
		return cellLocationManager.sendCount();
	}

	synchronized public void onBestLocationChanged(Location location) {
		logger.debug("onBestLocationChanged: " + location);
		StringBuilder sb = new StringBuilder();
		sb.append(location.getLatitude()).append(",").append(
				location.getLongitude()).append(",").append(
						location.getAccuracy());
		debug(sb.toString());

		mLastLocation = location;
		setChanged();
		notifyObservers(location);
	}

	synchronized public Location getLastKnownLocation() {
		return mLastLocation;
	}

	synchronized public void clearLastKnownLocation() {
		mLastLocation = null;
	}

	public void updateLocation(Location location) {
		logger.debug("updateLocation: Old: " + mLastLocation);
		logger.debug("updateLocation: New: " + location);

		// Cases where we only have one or the other.
		if (location != null && mLastLocation == null) {
			logger.debug("updateLocation: Null last location");
			onBestLocationChanged(location);
			return;

		} else if (location == null) {
			logger.debug("updated location is null, doing nothing");
			return;
		}

		long now = new Date().getTime();
		long locationUpdateDelta = now - location.getTime();
		long lastLocationUpdateDelta = now - mLastLocation.getTime();
		boolean locationIsInTimeThreshold = locationUpdateDelta <= LOCATION_UPDATE_MAX_DELTA_THRESHOLD;
		boolean lastLocationIsInTimeThreshold = lastLocationUpdateDelta <= LOCATION_UPDATE_MAX_DELTA_THRESHOLD;
		// boolean locationIsMostRecent = locationUpdateDelta <=
		// lastLocationUpdateDelta;
		logger.debug(new StringBuilder().append("locationUpdateDelta=").append(
				locationUpdateDelta).append(" ").append(
				"lastLocationUpdateDelta=").append(lastLocationUpdateDelta));

		boolean accuracyComparable = location.hasAccuracy()
		|| mLastLocation.hasAccuracy();
		boolean locationIsMostAccurate = false;
		if (accuracyComparable) {
			// If we have only one side of the accuracy, that one is more
			// accurate.
			if (location.hasAccuracy() && !mLastLocation.hasAccuracy()) {
				locationIsMostAccurate = true;
			} else if (!location.hasAccuracy() && mLastLocation.hasAccuracy()) {
				locationIsMostAccurate = false;
			} else {
				// If we have both accuracies, do a real comparison.
				locationIsMostAccurate = location.getAccuracy() <= mLastLocation
				.getAccuracy();
			}
		}

		// logger.debug("locationIsMostRecent:\t\t\t" + locationIsMostRecent);
		// logger.debug("locationUpdateDelta:\t\t\t" + locationUpdateDelta);
		// logger.debug("lastLocationUpdateDelta:\t\t" +
		// lastLocationUpdateDelta);
		// logger.debug("locationIsInTimeThreshold:\t\t" +
		// locationIsInTimeThreshold);
		// logger.debug("lastLocationIsInTimeThreshold:\t" +
		// lastLocationIsInTimeThreshold);
		//
		// logger.debug("accuracyComparable:\t\t\t" + accuracyComparable);
		// logger.debug("locationIsMostAccurate:\t\t" + locationIsMostAccurate);

		// Update location if its more accurate and w/in time threshold or if
		// the old location is
		// too old and this update is newer.
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER)
				&& locationIsInTimeThreshold) {
			onBestLocationChanged(location);
		} else if (accuracyComparable && locationIsMostAccurate
				&& locationIsInTimeThreshold) {
			onBestLocationChanged(location);
		} else if (locationIsInTimeThreshold && !lastLocationIsInTimeThreshold) {
			onBestLocationChanged(location);
		}
	}

	/*
	 * public boolean isAccurateEnough(Location location) { if (location != null
	 * && location.hasAccuracy() && location.getAccuracy() <=
	 * REQUESTED_FIRST_SEARCH_ACCURACY_IN_METERS) { long locationUpdateDelta =
	 * new Date().getTime() - location.getTime(); if (locationUpdateDelta <
	 * REQUESTED_FIRST_SEARCH_MAX_DELTA_THRESHOLD) {
	 * logger.debug("Location is accurate: " + location.toString()); return
	 * true; } } logger.debug("Location is not accurate: " +
	 * String.valueOf(location)); return false; }
	 */

	public void register(LocationManager locationManager, boolean gps,
			Context context) {

		logger.debug("Registering this location listener: " + this.toString());
		long updateMinTime = SLOW_LOCATION_UPDATE_MIN_TIME;
		long updateMinDistance = SLOW_LOCATION_UPDATE_MIN_DISTANCE;
		if (gps) {
			updateMinTime = LOCATION_UPDATE_MIN_TIME;
			updateMinDistance = LOCATION_UPDATE_MIN_DISTANCE;
		}
		List<String> providers = locationManager.getProviders(true);
		System.out.println("providers=" + providers.toString());
		int providersCount = providers.size();
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& !locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			setChanged();
			notifyObservers(null);
		}
		for (int i = 0; i < providersCount; i++) {
			String providerName = providers.get(i);
			if (locationManager.isProviderEnabled(providerName)) {
				updateLocation(locationManager
						.getLastKnownLocation(providerName));
			}
			// Only register with GPS if we've explicitly allowed it.
			if (gps || !LocationManager.GPS_PROVIDER.equals(providerName)) {
				locationManager.requestLocationUpdates(providerName,
						updateMinTime, updateMinDistance, this);
			}
		}
		if (cellLocationManager == null) {
			cellLocationManager = new CellLocationManager(context, cellManager,
					wifiManager) {
				@Override
				public void onLocationChanged() {
					if ((latitude() == 0.0D) || (longitude() == 0.0D))
						return;
					Location result = new Location("CellLocationManager");
					result.setLatitude(latitude());
					result.setLongitude(longitude());
					result.setAccuracy(accuracy());
					result.setTime(new Date().getTime());
					// onBestLocationChanged(result);
					updateLocation(result);

				}
			};
		}

		cellLocationManager.start();
	}

	public void unregister(LocationManager locationManager) {
		logger
		.debug("Unregistering this location listener: "
				+ this.toString());
		locationManager.removeUpdates(this);
		cellLocationManager.stop();
	}

	/**
	 * Updates the current location with the last known location without
	 * registering any location listeners.
	 * 
	 * @param locationManager
	 *            the LocationManager instance from which to retrieve the latest
	 *            known location
	 */
	synchronized public void updateLastKnownLocation(
			LocationManager locationManager) {
		List<String> providers = locationManager.getProviders(true);
		for (int i = 0, providersCount = providers.size(); i < providersCount; i++) {
			String providerName = providers.get(i);
			if (locationManager.isProviderEnabled(providerName)) {
				updateLocation(locationManager
						.getLastKnownLocation(providerName));
			}
		}
	}
}
