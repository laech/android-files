#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

void throw_errno_exception(JNIEnv *env) {
  jclass clazz = (*env)->FindClass(env, "l/files/fs/local/ErrnoException");
  if (NULL == clazz) {
    return;
  }

  jmethodID constructor = (*env)->GetMethodID(env, clazz, "<init>", "(ILjava/lang/String;)V");
  if (NULL == constructor) {
    return;
  }

  jint err = errno;
  jstring msg = (*env)->NewStringUTF(env, strerror(errno));
  jobject exception = (*env)->NewObject(env, clazz, constructor, err, msg);
  if (NULL == exception) {
    return;
  }

  (*env)->Throw(env, exception);
}
