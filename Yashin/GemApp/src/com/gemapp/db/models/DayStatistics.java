package com.gemapp.db.models;

import android.content.ContentValues;

import com.gemapp.db.DBHelper;

public class DayStatistics implements DBModelInterface<DayStatistics> {

    private String date;
    private int bikeId;
    private int treadmillId;
    private int ellipticalId;
    private int ergometerId;

    public String getDate() {
        return date;
    }

    public int getBikeId() {
        return bikeId;
    }

    public int getTreadmillId() {
        return treadmillId;
    }

    public int getEllipticalId() {
        return ellipticalId;
    }

    public int getErgometerId() {
        return ergometerId;
    }

    @Override
    public DayStatistics create(ContentValues data) {
        this.date = data.getAsString(DBHelper.TABLE_DAYSTATISTICS_DATE);
        this.bikeId = data.getAsInteger(DBHelper.TABLE_DAYSTATISTICS_BIKE_ID);
        this.treadmillId = data
                .getAsInteger(DBHelper.TABLE_DAYSTATISTICS_TREADMILL_ID);
        this.ellipticalId = data
                .getAsInteger(DBHelper.TABLE_DAYSTATISTICS_ELLIPTICAL_ID);
        this.ergometerId = data
                .getAsInteger(DBHelper.TABLE_DAYSTATISTICS_ERGOMETER_ID);
        return this;
    }

    @Override
    public ContentValues convertToCV() {
        ContentValues result = new ContentValues();
        result.put(DBHelper.TABLE_DAYSTATISTICS_DATE, date);
        result.put(DBHelper.TABLE_DAYSTATISTICS_BIKE_ID, bikeId);
        result.put(DBHelper.TABLE_DAYSTATISTICS_TREADMILL_ID, treadmillId);
        result.put(DBHelper.TABLE_DAYSTATISTICS_ELLIPTICAL_ID, ellipticalId);
        result.put(DBHelper.TABLE_DAYSTATISTICS_ERGOMETER_ID, ergometerId);
        return result;
    }

}
