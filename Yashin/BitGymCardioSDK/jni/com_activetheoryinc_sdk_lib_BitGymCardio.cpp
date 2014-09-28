/*
 Copyright 2011-2012 Active Theory Inc. All rights reserved.

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
 * This is a file that will be packaged into the Android SDK of BitGym
 * It contains type-casts and wrappers for the BitGym C++ functions
 * and contains the definitions for the actual functions Android apps
 * will call to make BitGym calls. All Java data memory cleanup has to
 * be done in the functions defined here after acquiring a result from
 * the C++ functions
 */

#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)

#include <stddef.h>

#define JNI_FALSE  0
#define JNI_TRUE   1

#include "com_activetheoryinc_sdk_lib_BitGymCardio.h"

#include "BitGymCardioAndroid.h"
#include "BitGymCardio.h"
#include "BGCardioBrain.h"
#include "BGUtility.h"
#include "BGExerciseReadingData.h"

// Flags and settings and things
BGDeviceOrientation orientation = BG_PORTRAIT;
bool unityHasListener = false;
bool shouldRenderToTexture = false;
int feedbackTextureNativeId = 0;
unsigned char* BGAndroidFeedback;

unsigned char* buffer0;
unsigned char* buffer1;
unsigned char* currentBuffer;
unsigned char* previousBuffer;
double lastFrameTimestamp;
double currentFrameDelta;

int frameWidth;
int frameHeight;
static bool firstFrame = true;
static pthread_key_t envKey;
static jobject obj = NULL;

static jmethodID nativeCrashedMethod;
static jmethodID newExerciseReadingAvailableMethod;

static JNIEnv *envOfLastDataInput;



#define CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION              \
                                                                  \
                                                               \
  catch (const std::bad_alloc& e)                                 \
  {                                                               \
    /* OOM exception */                                           \
    jclass jc = env->FindClass("java/lang/OutOfMemoryError");     \
    if(jc) env->ThrowNew (jc, e.what());                          \
  }                                                               \
  catch (const std::ios_base::failure& e)                         \
  {                                                               \
    /* IO exception */                                            \
    jclass jc = env->FindClass("java/io/IOException");            \
    if(jc) env->ThrowNew (jc, e.what());                          \
  }                                                               \
  catch (const std::exception& e)                                 \
  {                                                               \
    /* unknown exception */                                       \
	LOGE("unknown exception catch block");\
    jclass jc = env->FindClass("java/lang/Error");                \
    if(jc) env->ThrowNew (jc, e.what());                          \
  }                                                               \
  catch (...)                                                     \
  {                                                               \
    /* Oops I missed identifying this exception! */               \
	LOGE("catch-all catch block");\
    jclass jc = env->FindClass("java/lang/Error");                \
    if(jc) env->ThrowNew (jc, "unidentified exception");          \
  }

JNIEXPORT jboolean JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGUnityHasListener
  (JNIEnv *env, jclass clazz){
	jboolean result = (jboolean) unityHasListener;
	return result;
}

typedef struct _JNI_EXERCISEREADING_DATA {
	jclass cls;
	jmethodID ctorID;

	jfieldID xID;
	jfieldID yID;
	jfieldID vibrationalEnergyID;
	jfieldID effortID;
	jfieldID workoutConfidenceID;
	jfieldID cyclePositionID;
	jfieldID cadenceID;
	jfieldID timestampID;

} JNI_EXERCISEREADINGDATA;

JNI_EXERCISEREADINGDATA * exerciseReading;
jobject jreading;



void BGExerciseReadingDataAvailable(){

}

void javaExerciseReading(JNIEnv *env, jobject jexerciseReading, BGExerciseReadingData cexerciseReading) {

	env->SetFloatField(jexerciseReading, exerciseReading->xID, cexerciseReading.x);
	env->SetFloatField(jexerciseReading, exerciseReading->yID, cexerciseReading.y);
	env->SetFloatField(jexerciseReading, exerciseReading->vibrationalEnergyID, cexerciseReading.vibrationalEnergy);
	env->SetFloatField(jexerciseReading, exerciseReading->effortID, cexerciseReading.effort);
	env->SetFloatField(jexerciseReading, exerciseReading->workoutConfidenceID, cexerciseReading.workoutConfidence);
	env->SetFloatField(jexerciseReading, exerciseReading->cyclePositionID, cexerciseReading.cyclePosition);
	env->SetFloatField(jexerciseReading, exerciseReading->cadenceID, cexerciseReading.cadence);
	env->SetDoubleField(jexerciseReading, exerciseReading->timestampID, cexerciseReading.timestamp);
}

JNIEXPORT jobject JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGGetExerciseReadingData
  (JNIEnv *env, jclass clazz) {
	// Get data from SDK call
    BGExerciseReadingData data = BGPollExercise();
	javaExerciseReading(env, jreading, data);
	return jreading;
}

JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGInitCardio
  (JNIEnv *env, jclass clazz, jstring readingClass, jint _frameWidth, jint _frameHeight) {
	//try{

	// Initialize BGExerciseReadingData struct mapping
	const char *classname = env->GetStringUTFChars(readingClass, 0);
	exerciseReading = new JNI_EXERCISEREADINGDATA;
	exerciseReading->cls = env->FindClass(classname);
	env->ReleaseStringUTFChars(readingClass, classname);

	exerciseReading->ctorID  				= env->GetMethodID(exerciseReading->cls, "<init>", "()V");

	exerciseReading->xID  					= env->GetFieldID(exerciseReading->cls, "x", "F");
	exerciseReading->yID  					= env->GetFieldID(exerciseReading->cls, "y", "F");
	exerciseReading->cadenceID				= env->GetFieldID(exerciseReading->cls, "cadence", "F");
	exerciseReading->vibrationalEnergyID	= env->GetFieldID(exerciseReading->cls, "vibrationalEnergy", "F");
	exerciseReading->effortID				= env->GetFieldID(exerciseReading->cls, "effort", "F");
	exerciseReading->workoutConfidenceID	= env->GetFieldID(exerciseReading->cls, "workoutConfidence", "F");
	exerciseReading->cyclePositionID		= env->GetFieldID(exerciseReading->cls, "cyclePosition", "F");
	exerciseReading->timestampID 			= env->GetFieldID(exerciseReading->cls, "timestamp", "D");

	frameHeight = (int) _frameHeight;
	frameWidth = (int) _frameWidth;
	buffer0 = (unsigned char*) malloc(frameHeight*frameWidth);
	buffer1 = (unsigned char*) malloc(frameHeight*frameWidth);
	currentBuffer = buffer0;
	previousBuffer = NULL;

	BGAndroidFeedback = new unsigned char[frameWidth * frameHeight * 4];
	BGCardioBrain::shared()->setFrame(frameWidth, frameHeight);
	BGCardioBrain::shared()->setFeedbackBitmap(BGAndroidFeedback);

	jobject localRef = (env)->NewObject(exerciseReading->cls, exerciseReading->ctorID);
	jreading = (env)->NewGlobalRef(localRef);
	//}   CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}


JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGCopyAndResizeVideoFrame(JNIEnv *env, jclass clazz, jbyteArray _greyscaleImage, jint inputWidth, jint inputHeight, jdouble timestamp, jint transform) {
	jbyte* t_greyscaleImage = (env)->GetByteArrayElements (_greyscaleImage, 0);
	//try{

	if(firstFrame){
		lastFrameTimestamp = timestamp - 0.05; //for first frame, assume 20fps
		firstFrame = false;
	}


    //LOGV("inputWidth: %d, inputHeight %d, frameWidth: %d, frameHeight: %d, Transform: %d, Timestamp: %2.2f\n", inputWidth, inputHeight, frameWidth, frameHeight, transform, timestamp);
	BGZoomAndRotateBitmap((unsigned char*)t_greyscaleImage, currentBuffer, inputWidth, inputHeight, frameWidth, frameHeight, (BGTransform)transform, 1, 0, 0);

    currentFrameDelta = timestamp - lastFrameTimestamp;
	if(currentFrameDelta > 0.2){
		float deltaTimeOverride = 0.03;
		LOGD("Camera frame gap of %f ! overriding camera frame processings deltaTime to be %f . this is done to freeze rather than to decay tracking state in hopes of recovering",currentFrameDelta,deltaTimeOverride);
		currentFrameDelta=deltaTimeOverride;
	}
    lastFrameTimestamp = timestamp;

	/*if(BGTime() > 5.0){
		LOGD("trigger crash");
		float * tmp = NULL;
		tmp[0] = 1.0f;
		LOGD("should never see this, exception should have been thrown.");

	}*/
	(env)->ReleaseByteArrayElements(_greyscaleImage, t_greyscaleImage, JNI_ABORT);
	//}   CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

int frameNo = 0;
JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGProcessVideoFrame(JNIEnv *env, jclass clazz) {
	try{

	if(previousBuffer != NULL) {//not first frame input
		//LOGV("Processing frame with delta: %f", currentFrameDelta);
		BGCardioBrain::shared()->processCameraFrame(currentBuffer, previousBuffer, currentFrameDelta);
	} else {
		LOGV("previous buffer is null, skipping process step.");
	}


	previousBuffer = currentBuffer;
	currentBuffer  = (currentBuffer == buffer0) ? buffer1:buffer0;
	}   CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGInputVibration
  (JNIEnv *env, jclass clazz, jfloat _x, jfloat _y, jfloat _z) {
	//try{
	float x = (float)_x;
	float y = (float)_y;
	float z = (float)_z;
	BGCardioBrain::shared()->inputVibration(x, y, z);
	//}   CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGSetExerciseMachineType
  (JNIEnv *env, jclass clazz, jint machineType) {
	//try{
		BGSetExerciseMachineType((BGExerciseMachineType)machineType);
	//}  CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGSetDeviceOrientation
  (JNIEnv *env, jclass clazz, jint orient) {
	orientation = (BGDeviceOrientation)orient;
}

JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGRenderToTexture
  (JNIEnv *env, jclass clazz) {
	//try{
	renderFeedbackToTexture();
	//}   CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL Java_com_activetheoryinc_sdk_lib_BitGymCardio_BGStartFeedbackRender
  (JNIEnv *env, jclass clazz, jint textureId) {
	//try{
	//glEnable(GL_TEXTURE_2D);
	//glGenTextures(1, (GLuint*) &feedbackTextureNativeId);
	BGStartExerciseFeedback( textureId );
	LOGI("feedbackId: %d", feedbackTextureNativeId);
	//}   CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}


static struct sigaction old_sa[NSIG];

void android_sigaction(int signal, siginfo_t *info, void *reserved)
{
	LOGE("android_sigaction for signal: %d", signal);
	JNIEnv *env = (JNIEnv*)pthread_getspecific(envKey);
	//LOGE("envKey got %d %d", env, envKey);

	jclass clazz = (env)->FindClass("com/activetheoryinc/sdk/lib/BitGymCardioActivity");
	LOGE("clazz got");

	(env)->CallStaticVoidMethod(clazz, nativeCrashedMethod);
	LOGE("static method called");

	old_sa[signal].sa_handler(signal);
	LOGE("handler method called");

}


void Java_com_activetheoryinc_sdk_lib_BitGymCardio_init(JNIEnv *_env, jobject _obj){
	obj=_obj;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	LOGD("OnLoad start");
	JNIEnv *env;
	if ((jvm)->GetEnv((void **)&env, JNI_VERSION_1_2)) return JNI_ERR;
	pthread_key_create(&envKey, NULL);
	pthread_setspecific(envKey, env);
	jclass activityClass = (env)->FindClass("com/activetheoryinc/sdk/lib/BitGymCardioActivity");

	nativeCrashedMethod = env->GetStaticMethodID(activityClass,  "nativeCrashed", "()V");
	newExerciseReadingAvailableMethod = env->GetStaticMethodID(activityClass,  "newExerciseReadingAvailable", "(Lcom/activetheoryinc/sdk/lib/BGExerciseReadingData;)V");


	// Try to catch crashes...
	/*
		struct sigaction handler;
		memset(&handler, 0, sizeof(sigaction));
		handler.sa_sigaction = android_sigaction;
		handler.sa_flags = SA_RESETHAND;
	#define CATCHSIG(X) sigaction(X, &handler, &old_sa[X])
		CATCHSIG(SIGILL);
		CATCHSIG(SIGABRT);
		CATCHSIG(SIGBUS);
		CATCHSIG(SIGFPE);
		CATCHSIG(SIGSEGV);
		CATCHSIG(SIGSTKFLT);
		CATCHSIG(SIGPIPE);
		*/

		return JNI_VERSION_1_2;

}
