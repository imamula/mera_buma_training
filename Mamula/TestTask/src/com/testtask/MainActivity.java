package com.testtask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity {
	private GoogleMap googleMap = null;

	private Marker startMarker = null;
	private Marker endMarker = null;
	private final int CAMERA_ZOOM = 15;
	private final int MAX_NUM_OF_POINTS = 2;
	private final String XML_STEP = "step";
//TODO	edit xml parse/   private final String XML_START = "start_location";
//TODO	edit xml parse/   private final String XML_END = "end_location";
	private final String XML_LAT = "lat";
	private final String XML_LNG = "lng";
	private boolean IS_IT_START_POINT = true;
	private final String NAVOGATION_MODE_DRIVING = "driving";
	private int numOfPoints = 0;
	private ArrayList<LatLng> routePoints;
	Polyline polyline;
	PolylineOptions options;
	private AppStorage appStorage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		appStorage = (AppStorage) getApplication();
		initializeMap();

	}

	private void initializeMap() {

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();
		
		//For testing
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {

					@Override
					public boolean onMyLocationButtonClick() {
						googleMap.animateCamera(CameraUpdateFactory
								.zoomTo(CAMERA_ZOOM));
						return false;
					}
				});

		
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			//If Marker Clicked remove that marker and stop MOCK serivce
			@Override
			public boolean onMarkerClick(Marker arg0) {
				stopService(new Intent(MainActivity.this,
						MOCKLocationServerService.class));
				routePoints.clear();
				arg0.setTitle(null);
				arg0.remove();
				polyline.remove();
				numOfPoints--;
				if (arg0.equals(startMarker)) {
					IS_IT_START_POINT = true;
				}
				
				return false;
			}
		});
		
		googleMap.setOnMapClickListener(new OnMapClickListener() {
			//When tap on the screen, put the start or end marker in that place
			private LatLng startPoint = null;
			private LatLng endPoint = null;
			@Override
			public void onMapClick(LatLng arg0) {
				if (numOfPoints >= MAX_NUM_OF_POINTS) {
					return;
				}
				if (IS_IT_START_POINT) {

					startPoint = new LatLng(arg0.latitude, arg0.longitude);
					startMarker = googleMap.addMarker(new MarkerOptions()
							.position(arg0)
							.title(getString(R.string.start_string))
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
							.draggable(true));

					startMarker.showInfoWindow();
					IS_IT_START_POINT = false;

				} else {

					endPoint = new LatLng(arg0.latitude, arg0.longitude);
					endMarker = googleMap.addMarker(new MarkerOptions()
							.position(arg0)
							.title(getString(R.string.end_string))
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_RED))
							.draggable(true));
					endMarker.showInfoWindow();

				}
				numOfPoints++;
				if (numOfPoints == 2) {
					//Connect two points, in another thread
					new GetDirectionsTask().execute(startPoint, endPoint);

				}

			}
		});

	}

	
	//Get direction's points from google map
	public void getDirectionsPoints(LatLng startPoint, LatLng endPoint,
			String navigationMode) {

		String mapUrl = "http://maps.googleapis.com/maps/api/directions/xml?"
				+ "origin=" + startPoint.latitude + "," + startPoint.longitude
				+ "&destination=" + endPoint.latitude + ","
				+ endPoint.longitude + "&sensor=false&units=metric&mode="
				+ navigationMode;

		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(mapUrl);
		Document document = null;
		try {
			HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			String data = EntityUtils.toString(httpEntity);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			byte[] dataBytes = data.getBytes();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					dataBytes);
			document = documentBuilder.parse(inputStream);
			File f = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath(), "ponts.xml");

			FileOutputStream fos = new FileOutputStream(f);
			fos.write(dataBytes);
			fos.flush();
			fos.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		document.getDocumentElement().normalize();
		NodeList nodeStep = document.getElementsByTagName(XML_STEP);
		NodeList nodeLocaton = null;
		routePoints = new ArrayList<LatLng>();
		Element elementLocation = null;
		LatLng point;
		for (int i = 0; i < nodeStep.getLength(); i++) {
			nodeLocaton = nodeStep.item(i).getChildNodes();

			double lat, lng;

			elementLocation = (Element) nodeLocaton.item(3);
			lat = Double.parseDouble(elementLocation
					.getElementsByTagName(XML_LAT).item(0).getTextContent());
			lng = Double.parseDouble(elementLocation
					.getElementsByTagName(XML_LNG).item(0).getTextContent());
			point = new LatLng(lat, lng);
			routePoints.add(point);
			//TODO parse XML like it should
			elementLocation = (Element) nodeLocaton.item(5);
			lat = Double.parseDouble(elementLocation
					.getElementsByTagName(XML_LAT).item(0).getTextContent());
			lng = Double.parseDouble(elementLocation
					.getElementsByTagName(XML_LNG).item(0).getTextContent());
			point = new LatLng(lat, lng);

			routePoints.add(point);

		}
		appStorage.setRoutesPoints(routePoints);
	}

	//Task for calculating routes points and drawing polyline
	class GetDirectionsTask extends AsyncTask<LatLng, String, String> {
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage(getString(R.string.finding_route));
			progressDialog.show();

		}

		@Override
		protected String doInBackground(LatLng... params) {
			getDirectionsPoints(params[0], params[1], NAVOGATION_MODE_DRIVING);

			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			progressDialog.dismiss();
			options = new PolylineOptions().width(5).color(Color.RED)
					.geodesic(true);
			for (int z = 0; z < routePoints.size(); z++) {
				LatLng point = (LatLng) routePoints.get(z);
				options.add(point);
			}
			polyline = googleMap.addPolyline(options);
			startService(new Intent(MainActivity.this,
					MOCKLocationServerService.class));

		}
	}
}