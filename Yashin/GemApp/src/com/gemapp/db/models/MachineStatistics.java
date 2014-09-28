package com.gemapp.db.models;

import android.content.ContentValues;

import com.gemapp.db.DBHelper;

public class MachineStatistics implements DBModelInterface<MachineStatistics> {

    private int time;
    private int calories;
    private int miles;
    private int rpm;
    private int mph;

    public int getTime() {
        return time;
    }

    public int getCalories() {
        return calories;
    }

    public int getMiles() {
        return miles;
    }

    public int getRpm() {
        return rpm;
    }

    public int getMph() {
        return mph;
    }

    @Override
    public MachineStatistics create(ContentValues data) {
        this.time = data.getAsInteger(DBHelper.TABLE_MACHINESTATISTICS_TIME);
        this.calories = data.getAsInteger(DBHelper.TABLE_MACHINESTATISTICS_CALORIES);
        this.miles = data.getAsInteger(DBHelper.TABLE_MACHINESTATISTICS_MILES);
        this.rpm = data.getAsInteger(DBHelper.TABLE_MACHINESTATISTICS_RPM);
        this.mph = data.getAsInteger(DBHelper.TABLE_MACHINESTATISTICS_MPH);
        return this;
    }

    @Override
    public ContentValues convertToCV() {
        ContentValues result = new ContentValues();
        result.put(DBHelper.TABLE_MACHINESTATISTICS_TIME, time);
        result.put(DBHelper.TABLE_MACHINESTATISTICS_CALORIES, calories);
        result.put(DBHelper.TABLE_MACHINESTATISTICS_MILES, miles);
        result.put(DBHelper.TABLE_MACHINESTATISTICS_RPM, rpm);
        result.put(DBHelper.TABLE_MACHINESTATISTICS_MPH, mph);
        return result;
    }

}
