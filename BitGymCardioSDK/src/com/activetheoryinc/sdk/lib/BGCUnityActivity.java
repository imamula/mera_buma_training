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

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.util.Log;

public class BGCUnityActivity extends BitGymCardioActivity {
	
	private static String TAG = "BG Unity Cardio";
	/*
	 * It is the responsibility of the final application to assign  a UnityReadingListener
	 * Please review the documentation for how to define a UnityReadingListener
	 * This must be defined in onCreate
	 */
	protected ReadingListener<BGExerciseReadingData> mUnityReadingListener = null;

	/*
	 * These names refer to GameObject (RECEIVER) and its message-based function
	 * call (DATA_TYPE) in the BitGym Unity Plugin. Make sure these reflect the right
	 * names from the used version of the BitGym Unity Plugin.
	 */
	protected String BG_NATIVE_RECEIVER = "nativeTrackerContainer";
	protected String BG_DATA_TYPE = "ExerciseReading";
	
	
	private boolean _unityListenerRegistered = false;
	private Timer timer;

    protected void ManageUnityListeners () {
    	timer = new Timer();
    	if (mUnityReadingListener == null) return;
    	final Handler handler = new Handler ();
    	timer.scheduleAtFixedRate (new TimerTask (){
            public void run (){
            	handler.post (new Runnable () {
                    public void run (){
                    	//Log.i(TAG, "Looking for Unity Listeners");
                    	// Check with C++ for unity listeners
                    	if (BitGymCardio.BGUnityHasListener() && !_unityListenerRegistered) {
                    		RegisterExerciseReadingUpdateListener(mUnityReadingListener);
		        			_unityListenerRegistered = true;
		        			Log.i(TAG, "Adding in Unity Listener");
		        		}
		        		if (!BitGymCardio.BGUnityHasListener() && _unityListenerRegistered) {
		        			UnregisterExerciseReadingUpdateListener(mUnityReadingListener);
		        			_unityListenerRegistered = false;
		        			Log.i(TAG, "Removing Unity Listener");
		        		}
                    }
            	});
            }
        }, 0, (long) 400.0);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();    	
    	ManageUnityListeners();
    	if (mUnityReadingListener != null){ 
	    	UnregisterExerciseReadingUpdateListener(mUnityReadingListener);
			_unityListenerRegistered = false;
			Log.i(TAG, "Removing Unity Listener in onResume");
    	}
    }
    
    @Override
    protected void onPause() {
    	Log.i(TAG, "Pausing BGCU");
    	if (timer != null) {
        	timer.cancel();
        	timer = null;    		
    	}
    	if (mUnityReadingListener != null){	
	    	UnregisterExerciseReadingUpdateListener(mUnityReadingListener);
			_unityListenerRegistered = false;
			Log.i(TAG, "Removing Unity Listener in onPause");
	    }
    	super.onPause();
    }    
}
