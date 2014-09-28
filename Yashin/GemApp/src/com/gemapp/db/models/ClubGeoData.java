package com.gemapp.db.models;

import android.content.ContentValues;

import com.gemapp.db.DBHelper;

public class ClubGeoData implements DBModelInterface<ClubGeoData> {
    private String label;
    private String id;
    private double longitude;
    private double latitude;
    private float radius;

    public String getLabel() {
        return label;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public float getRadius() {
        return radius;
    }

    public String getId() {
        return id;
    }

    @Override
    public ClubGeoData create(ContentValues data) {
        this.label = data.getAsString(DBHelper.TABLE_GEO_LABEL);
        this.longitude = data.getAsDouble(DBHelper.TABLE_GEO_LONGITUDE);
        this.latitude = data.getAsDouble(DBHelper.TABLE_GEO_LATITUDE);
        this.radius = data.getAsFloat(DBHelper.TABLE_GEO_RADIUS);
        this.id = data.getAsString("id");
        return this;
    }

    @Override
    public ContentValues convertToCV() {
        ContentValues result = new ContentValues();
        result.put(DBHelper.TABLE_GEO_LABEL, label);
        result.put(DBHelper.TABLE_GEO_LONGITUDE, longitude);
        result.put(DBHelper.TABLE_GEO_LATITUDE, latitude);
        result.put(DBHelper.TABLE_GEO_RADIUS, radius);
        return result;
    }
}
