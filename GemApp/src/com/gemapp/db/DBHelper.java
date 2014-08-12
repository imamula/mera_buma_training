package com.gemapp.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gemapp.db.models.ClubGeoData;
import com.gemapp.db.models.DayStatistics;
import com.gemapp.db.models.MachineStatistics;

public class DBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "db";
    private final static int DB_VERSION = 1;

    private final static String TABLE_DAYSTATISTICS_NAME = "day_statistics_table";
    public final static String TABLE_DAYSTATISTICS_DATE = "date";
    public final static String TABLE_DAYSTATISTICS_BIKE_ID = "bike_id";
    public final static String TABLE_DAYSTATISTICS_TREADMILL_ID = "treadmill_id";
    public final static String TABLE_DAYSTATISTICS_ELLIPTICAL_ID = "elliptical_id";
    public final static String TABLE_DAYSTATISTICS_ERGOMETER_ID = "ergometer_id";

    private final static String TABLE_MACHINESTATISTICS_NAME = "machine_statistics_table";
    public final static String TABLE_MACHINESTATISTICS_TIME = "time";
    public final static String TABLE_MACHINESTATISTICS_CALORIES = "calories";
    public final static String TABLE_MACHINESTATISTICS_MILES = "miles";
    public final static String TABLE_MACHINESTATISTICS_RPM = "rpm";
    public final static String TABLE_MACHINESTATISTICS_MPH = "mph";

    private final static String TABLE_GEO_NAME = "geo_table";
    public final static String TABLE_GEO_LABEL = "label";
    public final static String TABLE_GEO_LONGITUDE = "longitude";
    public final static String TABLE_GEO_LATITUDE = "latitude";
    public final static String TABLE_GEO_RADIUS = "radius";

    public static enum MACHINES {
        BIKE, TREADMILL, ELLIPTICAL, ERGOMETER
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_DAYSTATISTICS_NAME
                + " ( id integer primary key autoincrement, "
                + TABLE_DAYSTATISTICS_DATE + " text, "
                + TABLE_DAYSTATISTICS_BIKE_ID + " integer, "
                + TABLE_DAYSTATISTICS_ELLIPTICAL_ID + " integer, "
                + TABLE_DAYSTATISTICS_ERGOMETER_ID + " integer, "
                + TABLE_DAYSTATISTICS_TREADMILL_ID + " integer" + ");");

        db.execSQL("create table " + TABLE_MACHINESTATISTICS_NAME
                + " ( id integer primary key autoincrement, "
                + TABLE_MACHINESTATISTICS_TIME + " integer, "
                + TABLE_MACHINESTATISTICS_CALORIES + " integer, "
                + TABLE_MACHINESTATISTICS_MILES + " integer, "
                + TABLE_MACHINESTATISTICS_RPM + " integer, "
                + TABLE_MACHINESTATISTICS_MPH + " integer" + ");");

        db.execSQL("create table " + TABLE_GEO_NAME
                + " ( id integer primary key autoincrement, " + TABLE_GEO_LABEL
                + " text, " + TABLE_GEO_LATITUDE + " integer, "
                + TABLE_GEO_LONGITUDE + " integer, " + TABLE_GEO_RADIUS
                + " integer " + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    public void insertDayStatistics(Date date, MachineStatistics[] data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dayStatistics = new ContentValues();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        dayStatistics.put(TABLE_DAYSTATISTICS_DATE, format.format(date));
        for (int i = 0; i < data.length; ++i) {
            ContentValues cv;
            if (data[i] == null)
                cv = getEmptyMachineStatisticsValue();
            else
                cv = data[i].convertToCV();
            db.insert(TABLE_MACHINESTATISTICS_NAME, null, cv);
            int id = getLastMachineStatisticsId(db);
            switch (MACHINES.values()[i]) {
            case BIKE:
                dayStatistics.put(TABLE_DAYSTATISTICS_BIKE_ID, id);
                break;
            case TREADMILL:
                dayStatistics.put(TABLE_DAYSTATISTICS_TREADMILL_ID, id);
                break;
            case ELLIPTICAL:
                dayStatistics.put(TABLE_DAYSTATISTICS_ELLIPTICAL_ID, id);
                break;
            case ERGOMETER:
                dayStatistics.put(TABLE_DAYSTATISTICS_ERGOMETER_ID, id);
                break;
            }
        }
        db.insert(TABLE_DAYSTATISTICS_NAME, null, dayStatistics);
        db.close();
    }

    public void updateDayStatistics(Date date, MachineStatistics data,
            MACHINES machine) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = getDayStatisticsByDate(date, db);
        ContentValues dayStatistics = convertCursorToContentValue(c);
        c.close();
        int id = -1;
        switch (machine) {
        case BIKE:
            id = dayStatistics.getAsInteger(TABLE_DAYSTATISTICS_BIKE_ID);
            break;
        case TREADMILL:
            id = dayStatistics.getAsInteger(TABLE_DAYSTATISTICS_TREADMILL_ID);
            break;
        case ELLIPTICAL:
            id = dayStatistics.getAsInteger(TABLE_DAYSTATISTICS_ELLIPTICAL_ID);
            break;
        case ERGOMETER:
            id = dayStatistics.getAsInteger(TABLE_DAYSTATISTICS_ERGOMETER_ID);
            break;
        }
        ContentValues oldData = convertCursorToContentValue(getMachineStatisticsById(
                id, db));
        db.update(TABLE_MACHINESTATISTICS_NAME,
                updateValues(oldData, data.convertToCV()), "id = " + id, null);
        db.close();
    }

    private ContentValues updateValues(ContentValues oldData,
            ContentValues newData) {
        for (String name : newData.keySet()) {
            oldData.put(name,
                    oldData.getAsInteger(name) + newData.getAsInteger(name));
        }
        return oldData;
    }

    public void showDayStatictics(Date date) {

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = getDayStatisticsByDate(date, db);
        if (cursor.moveToFirst()) {
            do {
                StringBuilder builder = new StringBuilder();
                for (String name : cursor.getColumnNames()) {
                    if (!name.equals(TABLE_DAYSTATISTICS_DATE)) {
                        builder.append(name + " = "
                                + cursor.getInt(cursor.getColumnIndex(name))
                                + ", ");
                        if (name.equals(TABLE_DAYSTATISTICS_BIKE_ID)) {
                            Cursor bikeCursor = getMachineStatisticsById(
                                    cursor.getInt(cursor.getColumnIndex(name)),
                                    db);
                            if (bikeCursor.moveToFirst())
                                builder.append("bike time = "
                                        + bikeCursor.getInt(bikeCursor
                                                .getColumnIndex(TABLE_MACHINESTATISTICS_TIME)));
                        }
                    } else
                        builder.append(name + " = "
                                + cursor.getString(cursor.getColumnIndex(name))
                                + ", ");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    Cursor getMachineStatisticsById(int id, SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MACHINESTATISTICS_NAME
                + " WHERE id = " + id, null);
        c.moveToFirst();
        return c;
    }

    Cursor getDayStatisticsByDate(Date date, SQLiteDatabase db) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Cursor c = db.query(TABLE_DAYSTATISTICS_NAME, null,
                TABLE_DAYSTATISTICS_DATE + " = ?",
                new String[] { format.format(date) }, null, null, null);
        c.moveToFirst();
        return c;
    }

    public MachineStatistics[] getDayStatisticsByDate(Date date) {
        SQLiteDatabase db = getWritableDatabase();
        MachineStatistics[] result = new MachineStatistics[MACHINES.values().length];
        Cursor c = getDayStatisticsByDate(date, db);
        if (c.moveToFirst()) {
            DayStatistics value = new DayStatistics()
                    .create(convertCursorToContentValue(c));
            result[MACHINES.BIKE.ordinal()] = new MachineStatistics()
                    .create(convertCursorToContentValue(getMachineStatisticsById(
                            value.getBikeId(), db)));
            result[MACHINES.ELLIPTICAL.ordinal()] = new MachineStatistics()
                    .create(convertCursorToContentValue(getMachineStatisticsById(
                            value.getEllipticalId(), db)));
            result[MACHINES.ERGOMETER.ordinal()] = new MachineStatistics()
                    .create(convertCursorToContentValue(getMachineStatisticsById(
                            value.getErgometerId(), db)));
            result[MACHINES.TREADMILL.ordinal()] = new MachineStatistics()
                    .create(convertCursorToContentValue(getMachineStatisticsById(
                            value.getTreadmillId(), db)));
        } else {
            for (int i = 0; i < result.length; ++i)
                result[i] = new MachineStatistics();
        }
        c.close();
        db.close();
        return result;
    }

    ContentValues convertCursorToContentValue(Cursor cursor) {
        if (cursor.getCount() != 0) {
            ContentValues result = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, result);
            return result;
        } else
            return null;
    }

    int getLastMachineStatisticsId(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT MAX(id) AS max_id FROM "
                + TABLE_MACHINESTATISTICS_NAME, null);
        int result = 0;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    int getLastClubGeoDataId(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT MAX(id) AS max_id FROM "
                + TABLE_GEO_NAME, null);
        int result = 0;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    public boolean dayStatisticsIsEmpty(Date date) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = getDayStatisticsByDate(date, db);
        boolean result = !c.moveToFirst();
        db.close();
        c.close();
        return result;
    }

    public static MachineStatistics createMachineStatistics(int calories,
            int miles, int mph, int rpm, int time) {
        ContentValues result = new ContentValues();
        result.put(TABLE_MACHINESTATISTICS_CALORIES, calories);
        result.put(TABLE_MACHINESTATISTICS_MILES, miles);
        result.put(TABLE_MACHINESTATISTICS_MPH, mph);
        result.put(TABLE_MACHINESTATISTICS_RPM, rpm);
        result.put(TABLE_MACHINESTATISTICS_TIME, time);
        return new MachineStatistics().create(result);
    }

    ContentValues getEmptyMachineStatisticsValue() {
        ContentValues result = new ContentValues();
        result.put(TABLE_MACHINESTATISTICS_CALORIES, 0);
        result.put(TABLE_MACHINESTATISTICS_MILES, 0);
        result.put(TABLE_MACHINESTATISTICS_MPH, 0);
        result.put(TABLE_MACHINESTATISTICS_RPM, 0);
        result.put(TABLE_MACHINESTATISTICS_TIME, 0);
        return result;
    }

    public ClubGeoData insertClubGeoData(String label, double lat, double lon,
            float rad) {
        ContentValues data = new ContentValues();
        data.put(TABLE_GEO_LABEL, label);
        data.put(TABLE_GEO_LATITUDE, lat);
        data.put(TABLE_GEO_LONGITUDE, lon);
        data.put(TABLE_GEO_RADIUS, rad);

        SQLiteDatabase db = getWritableDatabase();
        long rowCount = db.insert(TABLE_GEO_NAME, null, data);
        Log.d("log", rowCount + " rows were added");
        data.put("id", getLastClubGeoDataId(db));
        db.close();
        return new ClubGeoData().create(data);
    }

    public void insertClubGeoData(ClubGeoData data) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_GEO_NAME, null, data.convertToCV());
        db.close();
    }

    private Cursor getAllClubsGeoData(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_GEO_NAME, null);
        c.moveToFirst();
        return c;
    }

    public ArrayList<ClubGeoData> getAllClubsGeoData() {
        ArrayList<ClubGeoData> result = new ArrayList<ClubGeoData>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = getAllClubsGeoData(db);
        if (c.moveToFirst()) {
            do {
                result.add(new ClubGeoData()
                        .create(convertCursorToContentValue(c)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return result;
    }

    public boolean clubsGeofencesIsEmpty() {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = getAllClubsGeoData(db).moveToFirst();
        db.close();
        return !result;
    }
}
