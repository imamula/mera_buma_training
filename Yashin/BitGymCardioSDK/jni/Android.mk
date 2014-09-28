LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

X_FILE_LIST := $(wildcard $(LOCAL_PATH)/BitGymCore/core/common/*.cpp)
C_FILE_LIST := $(wildcard $(LOCAL_PATH)/BitGymCore/core/cardio/*.cpp)
V_FILE_LIST	:= $(wildcard $(LOCAL_PATH)/BitGymCore/core/cardio/camera_tracking/*.cpp)
A_FILE_LIST := $(wildcard $(LOCAL_PATH)/BitGymCore/android_wrapper/cardio/*.cpp)
LOCAL_SRC_FILES := 	$(X_FILE_LIST:$(LOCAL_PATH)/%=%) \
					$(C_FILE_LIST:$(LOCAL_PATH)/%=%) \
					$(V_FILE_LIST:$(LOCAL_PATH)/%=%) \
					$(A_FILE_LIST:$(LOCAL_PATH)/%=%) \
					com_activetheoryinc_sdk_lib_BitGymCardio.cpp
LOCAL_CPPFLAGS += -fexceptions -Wall 
#-O3 (aggressive optimizations)
#-O2 (safer optimizations)
#-fpermissive

LOCAL_CFLAGS	:= -DBG_BUILD_SDK -g
LOCAL_LDLIBS    := -llog -lGLESv2 -ldl -lEGL
LOCAL_C_INCLUDES := $(LOCAL_PATH)/BitGymCore/core/common \
					$(LOCAL_PATH)/BitGymCore/core/cardio \
					$(LOCAL_PATH)/BitGymCore/core/cardio/headers \
					$(LOCAL_PATH)/BitGymCore/core/cardio/camera_tracking \
					$(LOCAL_PATH)/BitGymCore/android_wrapper/cardio
LOCAL_MODULE := com_activetheoryinc_sdk_lib_BitGymCardio
BG_ANDROID := 1
include $(BUILD_SHARED_LIBRARY)
