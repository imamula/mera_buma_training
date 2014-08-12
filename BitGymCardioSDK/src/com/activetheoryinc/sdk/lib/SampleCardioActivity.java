/*
 Copyright 2011-2014 Active Theory Inc. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, is not permitted.
 
 THIS SOFTWARE IS PROVIDED BY THE ACTIVE THEORY INC``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL ACTIVE THEORY INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.activetheoryinc.sdk.lib;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.activetheoryinc.sdk.lib.BGExerciseMachineType;
import com.activetheoryinc.sdk.lib.BGExerciseReadingData;
import com.activetheoryinc.sdk.lib.BitGymCardio;
import com.activetheoryinc.sdk.lib.BitGymCardioActivity;
import com.activetheoryinc.sdk.lib.ReadingListener;

public class SampleCardioActivity extends BitGymCardioActivity {

	private ReadingListener<BGExerciseReadingData> exerciseReadingListener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sample);
        BitGymCardio.BGSetExerciseMachineType(BGExerciseMachineType.BG_TREADMILL);
        exerciseReadingListener = new ReadingListener<BGExerciseReadingData>() {
			public void OnNewReading(BGExerciseReadingData reading) {
				Log.v("BitGymReading", reading.toString());
			}
        };
        
        FrameLayout layout = (FrameLayout) findViewById(R.id.frameLayoutBGPreview);
       // mPreview.Hide();
       // layout.addView(mPreview);

        // Show BitGym Feedback
        mFeedback.setLayoutParams(new LayoutParams(640, 480));
        ((FrameLayout) findViewById(R.id.frameLayoutContent)).addView(mFeedback);
        mFeedback.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_cardio_activity, menu);
        
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        UnregisterExerciseReadingUpdateListener(exerciseReadingListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RegisterExerciseReadingUpdateListener(exerciseReadingListener);
    }

}
