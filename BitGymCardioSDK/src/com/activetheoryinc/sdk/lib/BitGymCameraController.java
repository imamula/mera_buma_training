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

/*
 */
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.GINGERBREAD) public class BitGymCameraController {
	/**
	 * Open the camera.  First attempt to find and open the front-facing camera.
	 * If that attempt fails, then fall back to whatever camera is available.
	 * 
	 * @return a Camera object
	 */
	// Singleton mode
	private static BitGymCameraController instance = null;
	private BitGymCameraController() {}
	public static BitGymCameraController GetInstance() {
		if (instance == null)
			instance = new BitGymCameraController();
		return instance;
	}
	
	private Camera camera = null;
	private CameraInfo cameraInfo;
	
	public CameraInfo getCameraInfo(){
		return cameraInfo;
	}
	
	private Camera openFrontFacingCamera() {
	    int cameraCount = 0;
	    Camera cam = null;
	    cameraInfo = new Camera.CameraInfo();
	    cameraCount = Camera.getNumberOfCameras();
	    for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	        Camera.getCameraInfo( camIdx, cameraInfo );
	        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
	            try {
	                cam = Camera.open( camIdx );
	                Camera.getCameraInfo(camIdx, cameraInfo);
	            } catch (RuntimeException e) {
	                Log.e("BitGymCardio SDK", "Camera failed to open: " + e.getLocalizedMessage());
	            }
	        }
	    }

	    return cam;
	}

	public Camera GetCamera() {
		if(camera == null)
			camera = openFrontFacingCamera();
		return camera;
	}
	
	public void CloseCamera() {
		if (camera != null) {
	        camera.setPreviewCallback(null); //Not sure if this will do anything to fix my current bug, but am trying.-AG feb 28th 2013
	        camera.release();
			camera = null;
		}
	}
	public static void Close() {
		if (instance!= null) GetInstance().CloseCamera();
		instance = null;
	}
	
}


