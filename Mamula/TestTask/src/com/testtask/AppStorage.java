package com.testtask;

import java.util.ArrayList;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

public class AppStorage extends Application{
	private ArrayList<LatLng> routesPoints;

	public ArrayList<LatLng> getRoutesPoints() {
		return routesPoints;
	}

	public void setRoutesPoints(ArrayList<LatLng> routesPoints) {
		this.routesPoints = routesPoints;
	}

	public AppStorage() {
		super();
		routesPoints= new ArrayList<LatLng>();
	}
	

}
