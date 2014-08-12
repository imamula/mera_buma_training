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

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
public class SurfaceViewPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "SurfaceViewPreview";
    private boolean _display = true;
    final static int optimal_width = 640;
    final static int optimal_height = 480;
    private Parameters parameters;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    @SuppressWarnings("deprecation")
	  SurfaceViewPreview(Activity context) {
        super(context);
        mHolder = getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    public void switchCamera(Camera camera) {
       setCamera(camera);
       try {
           mCamera.setPreviewDisplay(mHolder);
       } catch (IOException exception) {
           Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
       }
       Camera.Parameters parameters = camera.getParameters();
       parameters.setPreviewSize(optimal_width, optimal_height);//mPreviewSize.width, mPreviewSize.height);
       requestLayout();
       mHolder.addCallback(this);
       camera.setParameters(parameters);
       mCamera.startPreview();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (_display) setMeasuredDimension(width, height);
        else setMeasuredDimension(1, 1);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, optimal_width, optimal_height);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.

        try {
            if (mCamera != null) {
            	mCamera.setPreviewDisplay(holder);
                parameters = mCamera.getParameters();
                mPreviewSize = parameters.getPreviewSize();
            }
        } catch (IOException exception) {
        	EndPreview();
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }

    	setWillNotDraw(false);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
    	EndPreview();
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        //Camera.Parameters parameters = mCamera.getParameters();
        if (mCamera != null) {
        	parameters.setPreviewSize(w, h);
	        requestLayout();
            // Deactivate parameter setting for tablet
        	//mCamera.setParameters(parameters);
        }
    }

    public void EndPreview() {
        if (mCamera != null) {
        	try{
        		mCamera.stopPreview();
        	} catch (RuntimeException e) {
        		Log.e("BitGym", "Camera Released, no big deal");
        	}
        }
    	mHolder.removeCallback(this);
    }

    public void Hide() {
    	_display = false;
    }

    public void Show() {
    	_display = true;
    }
}
