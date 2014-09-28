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

/*
 * @author: keerthik
 * This activity is an abstract activity that any activity using
 * BitGym tech will want to inherit from. This activity will be
 * the only BitGym-inherited activity that can run at a given time.
 * It will be holding the lock on the camera and be managing a bunch
 * of listeners. Apps with multi-activity-architecture should use
 * at least one activity that inherits from BitGymActivity.
 */

package com.activetheoryinc.sdk.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;

public abstract class BitGymCardioActivity extends Activity {

	static private BitGymCardioActivity instance;
	int framesToIgnore = 0;

	public static void nativeCrashed() {
		new RuntimeException(
				"crashed here (native trace should follow after the Java trace)")
				.printStackTrace();
		// instance.startActivity(new Intent(instance, CrashHandler.class));
	}

	public static void newExerciseReadingAvailable(BGExerciseReadingData reading) {
		for (ReadingListener<BGExerciseReadingData> listener : instance.listeners) {
			listener.OnNewReading(reading);
		}
	}

	private static PrintWriter logFileWriter;
	private static boolean debugLogging = false;

	private static String logFilePath = Environment
			.getExternalStorageDirectory() + "/bg_debug.log";

	private static void SetupLog() {
		Log.v("file log", "clearing log");

		File f = new File(logFilePath);
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
			logFileWriter = new PrintWriter(new FileWriter(logFilePath, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void LogToFile(String tag, String msg) {
		LogToFile(tag + ": " + msg);
	}

	private static void LogToFile(String msg) {
		if (debugLogging) {
			Log.v("file log", msg);
			logFileWriter.write(msg + "\n");
			logFileWriter.flush();
		}
	}

	/*
	 * Camera and vision systems
	 */
	protected SurfaceTexturePreview mPreview;
	protected GLSurfaceView mFeedback;

	Camera mCamera;

	private final static int reducedFrameWidth = 160;
	private final static int reducedFrameHeight = 120;
	private final CopyOnWriteArraySet<ReadingListener<BGExerciseReadingData>> listeners = new CopyOnWriteArraySet<ReadingListener<BGExerciseReadingData>>();

	/*
	 * Accelerometer and vibration systems
	 */
	protected BitGymAccelerometerReader mAccelerometerReader;
	private SensorManager mSensorManager;
	private PowerManager mPowerManager;
	private Sensor mAccelerometer;

	public void RegisterExerciseReadingUpdateListener(
			ReadingListener<BGExerciseReadingData> listener) {
		Log.v("BitGymListeners", "Registering a listener");

		listeners.add(listener);
		if (mCamera == null)
			StartTracking();
	}

	public void UnregisterExerciseReadingUpdateListener(
			ReadingListener<BGExerciseReadingData> listener) {
		listeners.remove(listener);

		if (listeners.isEmpty()) {
			StopTracking();
		}
	}

	private double lastReadingTimestamp = 0;

	private void notifyListenersIfDataIsNew() {
		BGExerciseReadingData reading = BitGymCardio.BGGetExerciseReadingData();
		// Log.v("debug", "reading.timestamp: " + reading.timestamp + " lastReadingTimestamp:" + lastReadingTimestamp);
		if (reading.timestamp > lastReadingTimestamp)
			for (ReadingListener<BGExerciseReadingData> listener : instance.listeners) {
				listener.OnNewReading(reading);
			}
		lastReadingTimestamp = reading.timestamp;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		// SetupLog();

		// Get an instance of the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccelerometerReader = new BitGymAccelerometerReader();

		// Initialize the BitGym vision-based tracking system
		BitGymCardio.BGInitCardio(
				"com/activetheoryinc/sdk/lib/BGExerciseReadingData",
				reducedFrameWidth, reducedFrameHeight);
		//mPreview = new SurfaceTexturePreview(this);
		mPreview = new SurfaceTexturePreview(this);
		// Manage feedback surface
		mFeedback = new GLSurfaceView(this);

		// so far unsuccessful tests to get alpha in rendering in all devices
		/*
		 * mFeedback.setZOrderMediaOverlay(true);
		 * mFeedback.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		 * mFeedback.getHolder().setFormat(PixelFormat.RGBA_8888);
		 */
		// mFeedback.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		// mFeedback.setDebugFlags(mFeedback.DEBUG_CHECK_GL_ERROR |
		// mFeedback.DEBUG_LOG_GL_CALLS);

		mFeedback.setRenderer(new FeedbackRenderer());
		//mFeedback.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

	}

	protected void KeepScreenAwake() {
		// Game purpose keep screen awake
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				getClass().getName());
	}

	private int DetermineTransform() {
		if (BitGymCameraController.GetInstance().getCameraInfo() == null) {
			return 0;
		}
		int deviceOrientation;
		switch (getWindowManager().getDefaultDisplay().getRotation()) {
		case Surface.ROTATION_0:
			deviceOrientation = 0;
			break;
		case Surface.ROTATION_90:
			deviceOrientation = 90;
			break;
		case Surface.ROTATION_180:
			deviceOrientation = 180;
			break;
		case Surface.ROTATION_270:
			deviceOrientation = 270;
			break;
		default:
			deviceOrientation = 0;
		}
		// Log.v("debug", "device orientation: " +
		// Integer.toString(deviceOrientation));

		int cameraOrientation = BitGymCameraController.GetInstance()
				.getCameraInfo().orientation;
		// Log.v("debug", "camera orientation: " +
		// Integer.toString(cameraOrientation));
		int transform = 0;
		/*
		 * From BGConstants.h: typedef enum BGTransform { BG_NONE = 0,
		 * BG_MIRROR_X = 1, BG_MIRROR_Y = 2, BG_ROTATE_90_MIRROR_X = 3,
		 * BG_ROTATE_90_MIRROR_Y = 4, } BGTransform;
		 */
		transform = deviceOrientation + cameraOrientation;
		while (transform > 360)
			transform -= 360;
		while (transform < 0)
			transform += 360;
		switch (transform) {
		case 90:
			transform = 3;
			break;
		case 180:
			transform = 2;
			break;
		case 270:
			transform = 4;
			break;
		case 0:
			transform = 1;
			break;
		default:
			transform = 1;
		}

		return transform;
	}

	private void StartTracking() {

		mCamera = BitGymCameraController.GetInstance().GetCamera();
		if (mCamera != null) {
			Log.i("BitGymCardioActivity", "Starting camera tracking.");

			mCamera.setPreviewCallback(new PreviewCallback() {
				private int framesToIgnore = 3; //This is a hack that seems to solve some ANR's on task resume.

				public void onPreviewFrame(final byte[] data,
						final Camera camera) {

					if (framesToIgnore > 0) {
						framesToIgnore--;
						return;
					}

					final double currentFrameTimestamp = System.nanoTime() / 1000000000.0;

					if (camera != null) {
						int camWidth = camera.getParameters().getPreviewSize().width;
						int camHeight = camera.getParameters().getPreviewSize().height;
						BitGymCardio.BGSetDeviceOrientation(getScreenOrientation());
						BitGymCardio.BGCopyAndResizeVideoFrame(data, camWidth, camHeight, currentFrameTimestamp, DetermineTransform());
						synchronized (instance) {
							BitGymCardio.BGProcessVideoFrame();
						}
						notifyListenersIfDataIsNew();
						mFeedback.requestRender();
						
					}
				}
			});

			// }
			// });
			synchronized (instance) {
				try {
					mPreview.switchCamera(mCamera, reducedFrameWidth, reducedFrameHeight);
				} catch (CameraSetupException e) {
					// TODO Bubble this error up and handle it in Unity if is present.
					// TODO Check how kindle fire (no front camera) will behave here.
					e.printStackTrace();
				}
			}

			mSensorManager.registerListener(mAccelerometerReader,
					mAccelerometer, SensorManager.SENSOR_DELAY_GAME);

		} else
			Log.e("BitGym", "Creation failed!");

	}

	private void StopTracking() {
		synchronized (instance) {

			if (mCamera != null) {

				Log.i("BitGymCardioActivity", "Stopping camera tracking.");
				mPreview.EndPreview();
				BitGymCameraController.Close();
				mCamera = null;

			}
		}
		mSensorManager.unregisterListener(mAccelerometerReader);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mFeedback.onResume();

		// Open the default i.e. the first front facing camera.
		if (!listeners.isEmpty() && mCamera == null) {
			StartTracking();
		} else {
			Log.i("BitGymCardioActivity", "Not auto-calling StartTracking");
		}


	}

	@Override
	protected void onPause() {
		super.onPause();
		mFeedback.onPause();
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		StopTracking();
	}

	public int getScreenOrientation() {
		/*
		 * BG_UNKNOWN_ORIENTATION = 0, BG_PORTRAIT = 1, BG_LANDSCAPE_LEFT = 2,
		 * BG_UPSIDEDOWN = 3, BG_LANDSCAPE_RIGHT = 4
		 */

		switch (getWindowManager().getDefaultDisplay().getRotation()) {

		case Surface.ROTATION_0:
			return 2;
		case Surface.ROTATION_90:
			return 1;
		case Surface.ROTATION_180:
			return 4;
		case Surface.ROTATION_270:
			return 3;
		default:
			return 0;
		}

	}

	class BitGymAccelerometerReader implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// nothing useful to do here, but it's required.
		}

		public void onSensorChanged(final SensorEvent event) {
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;
			// Inputting data into our native code isn't thread safe.
			synchronized (instance) {
				BitGymCardio.BGInputVibration(event.values[0] / 9.81f,
						event.values[1] / 9.81f, event.values[2] / 9.81f);
			}
			notifyListenersIfDataIsNew();

		}
	}

	class FeedbackRenderer implements GLSurfaceView.Renderer {

		private final int[] cropParams;
		private int [] textureIds = new int [1];
		private final String TAG = "BG Feedback";
		public FeedbackRenderer() {
			cropParams = new int[4];
		}

		private int viewW = 0;
		private int viewH = 0;

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
			gl.glEnable(GL10.GL_TEXTURE_2D);

			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			/*
			 * gl.glEnable(GL10.GL_BLEND); gl.glBlendFunc(GL10.GL_ONE,
			 * GL10.GL_ONE_MINUS_SRC_ALPHA);
			 */
			// Start Open GL rendering
			gl.glGenTextures(1, textureIds, 0);
			BitGymCardio.BGStartFeedbackRender(textureIds[0]);

			cropParams[0] = 0;
			cropParams[1] = 120;
			cropParams[2] = 160;
			cropParams[3] = -120;
			((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
					GL11Ext.GL_TEXTURE_CROP_RECT_OES, cropParams, 0);
			// Log.i("BitGymFeedback","Feedback Parameters set");
			((GL11Ext) gl).glDrawTexfOES(0, 0, 0f, reducedFrameWidth, reducedFrameHeight);

		}

		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			// Pipe the feedback bitmap onto this textureId
			BitGymCardio.BGRenderToTexture();
			((GL11Ext) gl).glDrawTexfOES(0, 0, 0f, viewW, viewH);
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			viewW = width;
			viewH = height;
			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU.gluOrtho2D(gl, 0, width, 0, height);
			Log.i(TAG, "Feedback. Width: " + viewW + "; Height: " + viewH);
		}
	}
}
