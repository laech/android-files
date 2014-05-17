#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include "util.h"

void Java_l_files_io_os_Stdio_rename(JNIEnv *env, jclass clazz, jstring jold, jstring jnew) {
  const char *old = (*env)->GetStringUTFChars(env, jold, NULL);
  const char *new = (*env)->GetStringUTFChars(env, jnew, NULL);
  int result = TEMP_FAILURE_RETRY(rename(old, new));
  (*env)->ReleaseStringUTFChars(env, jold, old);
  (*env)->ReleaseStringUTFChars(env, jnew, new);
  if (-1 == result) {
    throw_errno_exception(env);
  }
}
