#include <jni.h>
#include <stdio.h>

void throw_os_exception(JNIEnv *env, const char *msg) {
  jclass c = (*env)->FindClass(env, "l/files/os/OsException");
  if (NULL != c) {
    (*env)->ThrowNew(env, c, msg);
  }
}
