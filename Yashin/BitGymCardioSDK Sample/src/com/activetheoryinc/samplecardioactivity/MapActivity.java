package com.activetheoryinc.samplecardioactivity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gemapp.db.DBHelper;
import com.gemapp.db.models.ClubGeoData;
import com.geo.MainActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity {

    public class ClubsGeoAdapter extends BaseAdapter {

        Activity context;
        ArrayList<ClubGeoData> geoDataList;

        public ClubsGeoAdapter(Activity context) {
            this.context = context;
            DBHelper dbHelper = new DBHelper(context);
            updateList(dbHelper);
            dbHelper.close();
        }

        public int getCount() {
            return geoDataList.size() + 1;
        }

        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public long getItemId(int position) {
            if (position == geoDataList.size())
                return -1;
            else
                return position;
        }

        public View getView(final int position, View convertView,
                ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(
                        android.R.layout.simple_list_item_1, null);
            String text;
            if (position == geoDataList.size())
                text = "click to add..";
            else
                text = getGeoDataString(geoDataList.get(position));
            ((TextView) convertView).setText(text);
            convertView.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    if (position == geoDataList.size()) {
                        showAddGeoDialog(null);
                    }

                }
            });
            return convertView;
        }

        private void updateList(DBHelper dbHelper) {
            geoDataList = dbHelper.getAllClubsGeoData();
        }

        private void showAddGeoDialog(LatLng latLon) {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.add_geo_layout);
            dialog.setTitle("Set Geo parameters:");
            final TextView latitudeTextView = ((TextView) dialog
                    .findViewById(R.id.lat));
            final TextView longitudeTextView = ((TextView) dialog
                    .findViewById(R.id.lon));
            final TextView radiusTextView = ((TextView) dialog
                    .findViewById(R.id.radius));
            final TextView labelTextView = ((TextView) dialog
                    .findViewById(R.id.label));
            if (latLon != null) {
                latitudeTextView.setText(String.valueOf(latLon.latitude));
                longitudeTextView.setText(String.valueOf(latLon.longitude));
            }

            dialog.findViewById(R.id.cancel_button).setOnClickListener(
                    new OnClickListener() {

                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

            dialog.findViewById(R.id.save_button).setOnClickListener(
                    new OnClickListener() {

                        public void onClick(View v)
                                throws NumberFormatException {
                            DBHelper dbHelper = new DBHelper(context);
                            try {
                                String label = labelTextView.getText()
                                        .toString();
                                if (label.length() == 0) {
                                    Toast.makeText(context,
                                            "Label is required!!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    double lat = Double
                                            .parseDouble(latitudeTextView
                                                    .getText().toString());
                                    double lon = Double
                                            .parseDouble(longitudeTextView
                                                    .getText().toString());
                                    if (lat < -90 || lat >= 90)
                                        throw new NumberFormatException(
                                                "Latitude valid range is [-90, 90)");
                                    if (lon < -180 || lon >= 180)
                                        throw new NumberFormatException(
                                                "Longitude valid range is [-180, 180)");
                                    LatLng l = new LatLng(lat, lon);
                                    float rad = Float.parseFloat(radiusTextView
                                            .getText().toString());
                                    ClubGeoData data = dbHelper
                                            .insertClubGeoData(label, lat, lon,
                                                    rad);
                                    setClubMarkerInMap(data);
                                    updateList(dbHelper);
                                    notifyDataSetInvalidated();
                                    dialog.cancel();
                                }
                            } catch (NullPointerException e) {
                                Log.d("error", e.toString());
                                Toast.makeText(context,
                                        "All fields are required!!",
                                        Toast.LENGTH_SHORT).show();
                            } catch (NumberFormatException e) {
                                Log.d("error", e.toString());
                                if (e.toString().contains("float"))
                                    Toast.makeText(context,
                                            "Radius is in wrong format!!",
                                            Toast.LENGTH_SHORT).show();
                                else if (e.toString().contains("double"))
                                    Toast.makeText(
                                            context,
                                            "Latitude or longitude is in wrong format!!",
                                            Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                            } finally {
                                dbHelper.close();
                            }
                        }
                    });

            dialog.show();
        }

        private String getGeoDataString(ClubGeoData data) {
            StringBuilder builder = new StringBuilder();
            builder.append(data.getLabel()).append(": lat = ")
                    .append(data.getLatitude()).append(", lon = ")
                    .append(data.getLongitude()).append(", rad = ")
                    .append(data.getRadius());
            return builder.toString();
        }
    }

    private GoogleMap mMap;
    private ListView clubsGeoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        clubsGeoListView = (ListView) findViewById(R.id.listView1);
        clubsGeoListView.setAdapter(new ClubsGeoAdapter(this));
        setUpMapIfNeeded();

        Button registrationButton = (Button) findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                DBHelper dbHelper = new DBHelper(getApplicationContext());
                boolean result = dbHelper.clubsGeofencesIsEmpty();
                dbHelper.close();
                if (!result) {
                    Intent next = new Intent(MapActivity.this,
                            MainActivity.class);
                    startActivity(next);
                } else
                    Toast.makeText(getApplicationContext(),
                            "Geofences list is empty!", Toast.LENGTH_SHORT)
                            .show();
            }
        });

        mMap.setOnMapLongClickListener(new OnMapLongClickListener() {

            public void onMapLongClick(LatLng point) {
                ((ClubsGeoAdapter) clubsGeoListView.getAdapter())
                        .showAddGeoDialog(point);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setAllClubMarkersInMap();
            }
        }
    }

    private void setClubMarkerInMap(ClubGeoData data) {
        mMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(data.getLatitude(), data.getLongitude()))
                        .title(data.getLabel())).setIcon(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
    }

    private void setAllClubMarkersInMap() {
        DBHelper dbHelper = new DBHelper(MapActivity.this);
        ArrayList<ClubGeoData> list = dbHelper.getAllClubsGeoData();
        for (ClubGeoData data : list)
            setClubMarkerInMap(data);
        dbHelper.close();
    }

}
