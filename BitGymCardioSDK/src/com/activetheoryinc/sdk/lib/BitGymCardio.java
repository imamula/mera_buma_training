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

public class BitGymCardio {
	public static int AverageColor(byte[] data) {
		return data[0];
	}
	
	// Signatures for Awesome BitGym Functions!
	public native static boolean BGUnityHasListener();
	public native static void BGInitCardio(String readingClass, int _frameWidth, int _frameHeight);
	public native static void BGCopyAndResizeVideoFrame(byte[] greyscaleImage, int camWidth, int camHeight, double frameTimestamp, int cameraOrientation);
	public native static boolean BGProcessVideoFrame();
	public native static boolean BGInputVibration(float x, float y, float z);
	public native static BGExerciseReadingData BGGetExerciseReadingData();
	private native static void BGSetExerciseMachineType(int machineType);
	public native static void BGSetDeviceOrientation(int orient);
	public native static void BGRenderToTexture();
	public native static void BGStartFeedbackRender(int textureId);
	public static void BGSetExerciseMachineType(BGExerciseMachineType machineType) {
		BGSetExerciseMachineType(machineType.getInt());
	}
	static {
		System.loadLibrary("com_activetheoryinc_sdk_lib_BitGymCardio");
	}
}