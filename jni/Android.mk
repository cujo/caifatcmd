# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# TOP_PATH refers to the project root dir (MyProject)
TOP_PATH := $(call my-dir)/..

# Build libat
include $(CLEAR_VARS)
LOCAL_PATH := $(TOP_PATH)/libat
LOCAL_MODULE := libat
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_CFLAGS := -g -DDEBUG_TRACE
#-DHAVE_ANDROID_OS
LOCAL_SRC_FILES := at_tok.c atchannel.c misc.c
include $(BUILD_STATIC_LIBRARY)

# Build main
include $(CLEAR_VARS)
LOCAL_PATH := $(TOP_PATH)/jni
LOCAL_MODULE    := at-cmd
LOCAL_SRC_FILES := at-cmd.c
LOCAL_C_INCLUDES := $(TOP_PATH)/libat
LIB_PATH := $(TOP_PATH)/libs/armeabi
LOCAL_LDLIBS += -L$(LIB_PATH)
LOCAL_STATIC_LIBRARIES := libat
LOCAL_CFLAGS	:= -g

# include $(BUILD_SHARED_LIBRARY)
include $(BUILD_EXECUTABLE)
