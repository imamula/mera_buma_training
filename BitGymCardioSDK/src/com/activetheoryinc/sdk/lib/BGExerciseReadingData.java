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

public class BGExerciseReadingData {
// This Class needs to wrap the C HeadReading definition

	public float cadence;
	public float vibrationalEnergy;
	public float effort; 
	public float workoutConfidence; 
	public float cyclePosition;
	
	public float x, y;
	public double timestamp;
	
	public BGExerciseReadingData copy() {
		BGExerciseReadingData n = new BGExerciseReadingData();
		n.cadence = cadence;
		n.vibrationalEnergy = vibrationalEnergy;
		n.effort = effort;
		n.workoutConfidence = workoutConfidence;
		n.cyclePosition = cyclePosition;
		n.x = x;
		n.y = y;
		n.timestamp = timestamp;
		return n;
	}
	
	public String toString() {
		String msg = String.format("%1.4f|%1.4f|%1.4f|%1.4f|%1.4f|%10.5f|%1.4f|%1.4f",
									cadence, 
									workoutConfidence,
									cyclePosition, 
									effort,  
									vibrationalEnergy, 
									timestamp, 
									x, y);
		
		return msg;
	}
}