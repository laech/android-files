#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

void throw_errno_exception(JNIEnv *env) {
  jclass clazz = (*env)->FindClass(env, "android/system/ErrnoException");
  if (NULL == clazz) {
    return;
  }

  jmethodID constructor = (*env)->GetMethodID(env, clazz, "<init>", "(Ljava/lang/String;I)V");
  if (NULL == constructor) {
    return;
  }

  jobject exception = (*env)->NewObject(env, clazz, constructor, NULL, errno);
  if (NULL == exception) {
    return;
  }

  (*env)->Throw(env, exception);
}
