package com.testtask;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;

public class MOCKLocationServerService extends Service {
	private ArrayList<LatLng> routesPoints;
	Thread serviceThread = null;
	LocationManager locationManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		serviceThread = new Thread(new Runnable() {

			@Override
			public void run() {
				MOCKLocationServer();

			}
		});
		serviceThread.start();
	}

	@Override
	public void onDestroy() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
		serviceThread.interrupt();
		serviceThread = null;
		super.onDestroy();

	}
	
	void MOCKLocationServer() {
		AppStorage appStorage = (AppStorage) getApplication();
		routesPoints = appStorage.getRoutesPoints();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.addTestProvider(LocationManager.NETWORK_PROVIDER,
				false, false, false, false, false, true, true, 0, 5);
		locationManager.setTestProviderEnabled(
				LocationManager.NETWORK_PROVIDER, true);

		
		int i = 0;
		while (true) {
			if(routesPoints.isEmpty())
			{
				continue;
			}
			if (i >= routesPoints.size()) {
				i = 0;
			}
			
			Location mocLocation = new Location(LocationManager.NETWORK_PROVIDER);			
			mocLocation.setLatitude(routesPoints.get(i).latitude);
			mocLocation.setLongitude(routesPoints.get(i).longitude);
			mocLocation.setAltitude(0);
			mocLocation.setTime(System.currentTimeMillis());
			locationManager.setTestProviderLocation(
					LocationManager.NETWORK_PROVIDER, mocLocation);
			i++;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
