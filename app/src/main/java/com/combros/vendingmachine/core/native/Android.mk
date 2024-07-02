LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := NativePortCommunication
LOCAL_SRC_FILES := NativePortCommunication.c
LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)