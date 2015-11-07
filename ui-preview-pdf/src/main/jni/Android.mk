LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libpdfium

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(TARGET_ARCH_ABI)/libpdfium.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := previewpdf

LOCAL_CFLAGS += -DHAVE_PTHREADS
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES += libpdfium
LOCAL_LDLIBS += -llog -ljnigraphics

LOCAL_SRC_FILES :=  $(LOCAL_PATH)/l_files_ui_preview_Pdf.c

include $(BUILD_SHARED_LIBRARY)
