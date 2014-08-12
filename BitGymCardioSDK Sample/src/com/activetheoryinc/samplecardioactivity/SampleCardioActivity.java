package com.activetheoryinc.samplecardioactivity;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.activetheoryinc.sdk.lib.BGExerciseMachineType;
import com.activetheoryinc.sdk.lib.BGExerciseReadingData;
import com.activetheoryinc.sdk.lib.BitGymCardio;
import com.activetheoryinc.sdk.lib.BitGymCardioActivity;
import com.activetheoryinc.sdk.lib.ReadingListener;
import com.gemapp.db.DBHelper;
import com.gemapp.db.DBHelper.MACHINES;
import com.gemapp.db.models.MachineStatistics;

// The activity must extend from BitGymCardioActivity to use BitGym
public class SampleCardioActivity extends BitGymCardioActivity {

    private ReadingListener<BGExerciseReadingData> exerciseReadingListener;
    private static boolean isStart = false;

    private TextView cadenceText;
    private TextView confidenceText;
    private TextView cycleText;
    private TextView vibrationalEnergy;
    private TextView effort;
    private TextView timestamp;
    private TextView x;
    private TextView y;
    private Button startButton;
    private Button stopButton;
    private Button showButton;
    private Button mapButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cadenceText = (TextView) findViewById(R.id.cadence);
        confidenceText = (TextView) findViewById(R.id.confidence);
        cycleText = (TextView) findViewById(R.id.cycleposition);
        vibrationalEnergy = (TextView) findViewById(R.id.vibrational_energy);
        effort = (TextView) findViewById(R.id.effort);
        timestamp = (TextView) findViewById(R.id.timestamp);
        x = (TextView) findViewById(R.id.x);
        y = (TextView) findViewById(R.id.y);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        showButton = (Button) findViewById(R.id.show_button);
        mapButton = (Button) findViewById(R.id.map_button);

        initializeData(SampleCardioActivity.this);

        startButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                isStart = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                showButton.setEnabled(false);
                mapButton.setEnabled(false);
            }
        });

        stopButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                isStart = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                showButton.setEnabled(true);
                mapButton.setEnabled(true);
                saveStatistics(SampleCardioActivity.this);
            }
        });

        showButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent next = new Intent(SampleCardioActivity.this,
                        StatisticsActivity.class);
                startActivity(next);
            }
        });

        mapButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent next = new Intent(SampleCardioActivity.this,
                        MapActivity.class);
                startActivity(next);
            }
        });

        // Create a reading listener to handle the reading data obtained every
        // cam frame
        // Don't forget to register it (see onResume) before it can be used
        exerciseReadingListener = new ReadingListener<BGExerciseReadingData>() {
            public void OnNewReading(BGExerciseReadingData reading) {
                // The reading is reliable when confidence reaches 1
                if (isStart) {
                    boolean confident = !(reading.workoutConfidence < 1);

                    confidenceText.setText(Float
                            .toString(reading.workoutConfidence
                                    + Float.valueOf(confidenceText.getText()
                                            .toString())));
                    confidenceText.setTextColor(confident ? 0xFF11EE11
                            : 0xFFEE1111);

                    cadenceText.setText(Float.toString(reading.cadence
                            + Float.valueOf(cadenceText.getText().toString())));
                    cadenceText.setTextColor(confident ? 0xFF000000
                            : 0xFF666666);

                    cycleText.setText(Float.toString(reading.cyclePosition
                            + Float.valueOf(cycleText.getText().toString())));

                    vibrationalEnergy.setText(Float
                            .toString(reading.vibrationalEnergy
                                    + Float.valueOf(vibrationalEnergy.getText()
                                            .toString())));

                    effort.setText(Float.toString(reading.effort
                            + Float.valueOf(effort.getText().toString())));

                    timestamp.setText(Double.toString(reading.timestamp
                            + Float.valueOf(timestamp.getText().toString())));

                    x.setText(Float.toString(reading.x));

                    y.setText(Float.toString(reading.y));
                }
            }
        };
        // Set the machine type the user is on for better data representation
        // [optional]
        BitGymCardio
                .BGSetExerciseMachineType(BGExerciseMachineType.BG_TREADMILL);

        // Show BitGym Feedback in the app
        FrameLayout feedbackFrame = (FrameLayout) findViewById(R.id.frameLayoutBGPreview);
        // Set the size in pixels (default is 160x120)
        mFeedback.setLayoutParams(new LayoutParams(640, 480));
        feedbackFrame.addView(mFeedback);
        mFeedback.requestFocus();
    }

    private void initializeData(Activity context) {

        DBHelper dbHelper = new DBHelper(context);
        MachineStatistics statistic = dbHelper
                .getDayStatisticsByDate(new Date())[0];
        cadenceText.setText(String.valueOf(statistic.getCalories()));
        confidenceText.setText(String.valueOf(statistic.getMiles()));
        cycleText.setText(String.valueOf(statistic.getMph()));
        effort.setText(String.valueOf(statistic.getRpm()));
        timestamp.setText(String.valueOf(statistic.getTime()));
        dbHelper.close();
    }

    public void saveStatistics(Activity context) {
        DBHelper dbHelper = new DBHelper(context);
        MachineStatistics[] data = getMachineStatistics();
        Date dateNow = new Date();
        if (dbHelper.dayStatisticsIsEmpty(dateNow))
            dbHelper.insertDayStatistics(dateNow, data);
        else
            for (MACHINES machine : MACHINES.values())
                dbHelper.updateDayStatistics(dateNow, data[machine.ordinal()],
                        machine);
        dbHelper.close();
    }

    private MachineStatistics[] getMachineStatistics() {
        MachineStatistics[] result = new MachineStatistics[MACHINES.values().length];
        for (int i = 0; i < result.length; ++i) {
            int calories = (int) ((i + 1) * 10 * Double.valueOf(cadenceText
                    .getText().toString()));
            int miles = (int) ((i + 1) * 10 * Double.valueOf(confidenceText
                    .getText().toString()));
            int mph = (int) ((i + 1) * 10 * Double.valueOf(cycleText.getText()
                    .toString()));
            int rpm = (int) ((i + 1) * 10 * Double.valueOf(effort.getText()
                    .toString()));
            int time = (int) ((i + 1) * Double.valueOf(timestamp.getText()
                    .toString()));
            result[i] = DBHelper.createMachineStatistics(calories, miles, mph,
                    rpm, time);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_cardio_activity, menu);
        return true;
    }

    // The listener defined above is unregistered when the application is paused
    // to
    // save battery, and to reduce cpu and memory usage when the app is
    // minimized
    @Override
    protected void onPause() {
        super.onPause();
        UnregisterExerciseReadingUpdateListener(exerciseReadingListener);
    }

    // !! Don't forget to register the listener !!
    // It is done here so that when the app is brought back to front it is
    // re-registered
    @Override
    protected void onResume() {
        super.onResume();
        RegisterExerciseReadingUpdateListener(exerciseReadingListener);
    }

}
