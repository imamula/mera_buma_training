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
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.FrameLayout;

/*
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
public class BitGymPreview{// extends TextureView implements SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener{
    private final String TAG = "Preview";
    private boolean _display = true;
    private Parameters parameters;
    
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;
    SurfaceTexture mSurfaceTexture;
    
    @SuppressWarnings("deprecation")
	BitGymPreview(Activity context) {
        //super(context);
        //this.setSurfaceTextureListener(this);
       mSurfaceTexture = new SurfaceTexture(100);
    }

    public void setCamera(Camera camera, int minCameraWidth, int minCameraHeight) throws CameraSetupException {
       // mSurfaceTexture = getSurfaceTexture();

    	mCamera = camera;
        Camera.Parameters parameters = mCamera.getParameters();
        List<Size> mSizes = parameters.getSupportedPreviewSizes();
        Size smallestSuitable = null;
        Size smallestAspectFit = null;
        float desiredRatio = (float)minCameraWidth/(float)minCameraHeight;

        for (int i = 0; i < mSizes.size(); i ++)
        {
        	Size s = mSizes.get(i);
            Log.i("BitGymPreview", "Considering cam preview size: " +s.width + " x " +s.height);
            if(smallestSuitable == null)
            	smallestSuitable = s;
            else if(s.width >= minCameraWidth && s.height >= minCameraHeight && s.width <= smallestSuitable.width && s.height <= smallestSuitable.height){
            	smallestSuitable = s;    
            }
            float rDiff = desiredRatio - (float)s.width/(float)s.height;
            if(rDiff > -0.1f && rDiff < 0.1f ){
            	if(smallestAspectFit == null)
            		smallestAspectFit = s;
            	else if(s.width >= minCameraWidth && s.height >= minCameraHeight && s.width <= smallestAspectFit.width && s.height <= smallestAspectFit.height){
            		if((s.width / minCameraWidth) % 2 == 0 && (s.height / minCameraHeight) % 2 == 0) //optimize: pick camera size power of 2 larger than what we're reducing the images to. 
            			smallestAspectFit = s;    
                }
            }

        }
        if(smallestAspectFit != null) {
			Log.i("BitGymPreview", "Selected camera preview size with aspect fit: " + smallestAspectFit.width + ", " + smallestAspectFit.height);

			parameters.setPreviewSize(smallestAspectFit.width, smallestAspectFit.height);
        } else if(smallestSuitable != null) {
			Log.i("BitGymPreview", "Selected camera preview size (no aspect fit found): " + smallestSuitable.width + ", " + smallestSuitable.height);
			parameters.setPreviewSize(smallestSuitable.width, smallestSuitable.height);
        } else {
        	throw new CameraSetupException("Could not find a camera preview size that would work for us.");
        }
        	        
        mCamera.setParameters(parameters);
        
        ConnectCameraAndSurfaceIfBothAvailable();
        
    }

    public void switchCamera(Camera camera, int minCameraWidth, int minCameraHeight) throws CameraSetupException {
    	setCamera(camera, minCameraWidth, minCameraHeight);
    }

    public void EndPreview() {    	
        if (mCamera != null) {
        	try{
        		mCamera.stopPreview();
        	} catch (RuntimeException e) {
        		Log.e("BitGym", "Camera Released, no big deal");
        	}
        }
    	//mHolder.removeCallback(this);
    }
    
    
    private void ConnectCameraAndSurfaceIfBothAvailable(){
		Log.i("BitGymPreview", "ConnectCameraAndSurfaceIfBothAvailable start");

    	//if(getSurfaceTexture() != null)
    	//	mSurfaceTexture = getSurfaceTexture();
    	
        if(mCamera == null || mSurfaceTexture == null){
        	return;
        }
		Log.i("BitGymPreview", "ConnectCameraAndSurfaceIfBothAvailable got past null check");

        
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        //this.setLayoutParams(new FrameLayout.LayoutParams(
         //       previewSize.width, previewSize.height, Gravity.CENTER));
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
    		Log.i("BitGymPreview", "Camera preview texture set and preview started!");

        } catch (IOException t) {
        	Log.e("BitGymPreview", "Error setting surface texture to camera preview: " + t.getMessage());
        }

        
    }
    
    /*
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    	mSurfaceTexture = surface;
    	mSurfaceTexture.setOnFrameAvailableListener(this);
		Log.i("BitGymPreview", "onSurfaceTextureAvailable");

    	ConnectCameraAndSurfaceIfBothAvailable();
    }
    

	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// Ignored, the Camera does all the work for us
		Log.i("BitGymPreview", "onSurfaceTextureSizeChanged");

	}

	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		//mCamera.stopPreview();
		//mCamera.release();
		Log.i("BitGymPreview", "onSurfaceTextureDestroyed");

		return true;
	}

	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		Log.i("BitGymPreview", "onSurfaceTextureUpdated: " + mSurfaceTexture.getTimestamp());

	}

	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		Log.i("BitGymPreview", "onFrameAvailable: " + surfaceTexture.getTimestamp());
	}
*/
	
    /*
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
    }*/
    /*
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
*/
/*
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
    */
    /*
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
    */

    
    public void Hide() {
    	_display = false;
    }
    
    public void Show() {
    	_display = true;
    }
     

}

