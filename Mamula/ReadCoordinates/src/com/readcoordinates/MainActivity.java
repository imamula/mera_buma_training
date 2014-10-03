package com.readcoordinates;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.widget.TextView;


public class MainActivity extends Activity {
	TextView tvCoordinates;

	LocationManager myLocationManager;
	String PROVIDER = LocationManager.NETWORK_PROVIDER;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		tvCoordinates = (TextView) findViewById(R.id.tv_coordinates);
		tvCoordinates.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		myLocationManager.requestLocationUpdates(PROVIDER, 200, 1, myLocationListener);

		
	}

	@Override
	protected void onDestroy() {
		myLocationManager.removeUpdates(myLocationListener);
		
		super.onDestroy();
	}
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	
	 if(keyCode == KeyEvent.KEYCODE_BACK)
	    {
		 tvCoordinates.setText("");
	    }
	   
	
	return super.onKeyDown(keyCode, event);
}

	private LocationListener myLocationListener = new LocationListener() {
		 String locationsString = "";

		@Override
		public void onLocationChanged(Location location) {
			locationsString = tvCoordinates.getText().toString();
			locationsString +=  "\nLat: " + location.getLatitude()
					+ "'\nLng: " + location.getLongitude()+"\n\n";
			tvCoordinates.setText(locationsString);
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

}
