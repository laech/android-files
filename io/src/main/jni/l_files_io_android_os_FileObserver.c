/* //device/libs/android_runtime/android_util_FileObserver.cpp
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <sys/inotify.h>

// This is a copy of the original android_util_FileObserver.cpp simplified
// https://github.com/android/platform_frameworks_base/blob/master/core/jni/android_util_FileObserver.cpp

static jmethodID method_onEvent;

jint Java_l_files_io_android_os_FileObserver_00024ObserverThread_init(
    JNIEnv* env, jobject object) {

  return (jint)inotify_init();
}

void Java_l_files_io_android_os_FileObserver_00024ObserverThread_observe(
    JNIEnv* env, jobject object, jint fd) {

  char event_buf[512];
  struct inotify_event* event;

  while (1) {
    int event_pos = 0;
    int num_bytes = read(fd, event_buf, sizeof(event_buf));

    if (num_bytes < (int)sizeof(*event)) {
      if (errno == EINTR) {
        continue;
      }
      return;
    }

    while (num_bytes >= (int)sizeof(*event)) {
      int event_size;
      event = (struct inotify_event *)(event_buf + event_pos);

      jstring path = NULL;

      if (event->len > 0) {
        path = (*env)->NewStringUTF(env, event->name);
      }

      (*env)->CallVoidMethod(env, object, method_onEvent, event->wd, event->mask, path);
      if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
      }
      if (path != NULL) {
        (*env)->DeleteLocalRef(env, path);
      }

      event_size = sizeof(*event) + event->len;
      num_bytes -= event_size;
      event_pos += event_size;
    }
  }
}

jint Java_l_files_io_android_os_FileObserver_00024ObserverThread_startWatching(
    JNIEnv* env, jobject object, jint fd, jstring pathString, jint mask) {
  int res = -1;
  if (fd >= 0) {
    const char* path = (*env)->GetStringUTFChars(env, pathString, NULL);
    res = inotify_add_watch(fd, path, mask);
    (*env)->ReleaseStringUTFChars(env, pathString, path);
  }
  return res;
}

void Java_l_files_io_android_os_FileObserver_00024ObserverThread_stopWatching(
    JNIEnv* env, jobject object, jint fd, jint wfd) {
  inotify_rm_watch((int)fd, (uint32_t)wfd);
}

int Java_l_files_io_android_os_FileObserver_init(JNIEnv* env) {
  jclass clazz;

  clazz = (*env)->FindClass(env, "l/files/io/android/os/FileObserver$ObserverThread");

  if (clazz == NULL) {
    return -1;
  }

  method_onEvent = (*env)->GetMethodID(env, clazz, "onEvent", "(IILjava/lang/String;)V");
  if (method_onEvent == NULL) {
    return -1;
  }
}
