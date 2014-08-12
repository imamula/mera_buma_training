package com.activetheoryinc.samplecardioactivity;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.gemapp.db.DBHelper;
import com.gemapp.db.DBHelper.MACHINES;
import com.gemapp.db.models.MachineStatistics;

public class StatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_layout);

        ArrayAdapter<String> machineAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, new String[] {
                        MACHINES.BIKE.name(), MACHINES.ELLIPTICAL.name(),
                        MACHINES.ERGOMETER.name(), MACHINES.TREADMILL.name() });

        Spinner machineSpinner = (Spinner) findViewById(R.id.machine_spinner);
        machineSpinner.setAdapter(machineAdapter);
        machineSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                updateStatistics(getMachineBySpinnerPosition(position));
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView1);
        calendarView.setOnDateChangeListener(new OnDateChangeListener() {

            public void onSelectedDayChange(CalendarView view, int year,
                    int month, int dayOfMonth) {
                int position = ((Spinner) findViewById(R.id.machine_spinner))
                        .getSelectedItemPosition();
                updateStatistics(getMachineBySpinnerPosition(position));
            }
        });
    }

    private MACHINES getMachineBySpinnerPosition(int position) {
        MACHINES machine = MACHINES.BIKE;
        switch (position) {
        case 0:
            machine = MACHINES.BIKE;
            break;
        case 1:
            machine = MACHINES.ELLIPTICAL;
            break;
        case 2:
            machine = MACHINES.ERGOMETER;
            break;
        case 3:
            machine = MACHINES.TREADMILL;
            break;
        }
        return machine;
    }

    private void updateStatistics(MACHINES machine) {
        DBHelper dbHelper = new DBHelper(StatisticsActivity.this);
        Date date = new Date(
                ((CalendarView) findViewById(R.id.calendarView1)).getDate());
        MachineStatistics statistics = dbHelper.getDayStatisticsByDate(date)[machine
                .ordinal()];

        ((TextView) findViewById(R.id.calories)).setText(String
                .valueOf(statistics.getCalories()));
        ((TextView) findViewById(R.id.time)).setText(String.valueOf(statistics
                .getTime()));
        ((TextView) findViewById(R.id.miles)).setText(String.valueOf(statistics
                .getMiles()));
        ((TextView) findViewById(R.id.mph)).setText(String.valueOf(statistics
                .getMph()));
        ((TextView) findViewById(R.id.rmp)).setText(String.valueOf(statistics
                .getRpm()));

        dbHelper.close();
    }
}
