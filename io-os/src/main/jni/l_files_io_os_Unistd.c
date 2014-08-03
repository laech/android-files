#include <jni.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include "util.h"

jboolean Java_l_files_io_os_Unistd_symlink(JNIEnv *env, jclass clazz, jstring jsrc, jstring jdst) {
  const char *src = (*env)->GetStringUTFChars(env, jsrc, NULL);
  const char *dst = (*env)->GetStringUTFChars(env, jdst, NULL);
  int result = TEMP_FAILURE_RETRY(symlink(src, dst));
  (*env)->ReleaseStringUTFChars(env, jsrc, src);
  (*env)->ReleaseStringUTFChars(env, jdst, dst);
  return (-1 == result) ? JNI_FALSE : JNI_TRUE;
}

void Java_l_files_io_os_Unistd_access(JNIEnv *env, jclass clazz, jstring jpath, jint mode) {
  const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
  if (NULL == path) {
    return; // TODO error handling
  }
  int rc = TEMP_FAILURE_RETRY(access(path, mode));
  if (rc == -1) {
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    throw_errno_exception(env);
  } else {
    (*env)->ReleaseStringUTFChars(env, jpath, path);
  }
}

jstring Java_l_files_io_os_Unistd_readlink(JNIEnv *env, jclass clazz, jstring jpath) {
  const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

  char *buf;
  ssize_t count;
  size_t bufsize = 512;

  for (;;) {
    buf = (char *)malloc(bufsize + 1);
    count = readlink(path, buf, bufsize);

    if (count == -1) {
      (*env)->ReleaseStringUTFChars(env, jpath, path);
      free(buf);
      throw_errno_exception(env);
      return NULL;

    } else if (count < bufsize) {
      buf[count] = '\0';
      jstring result = (*env)->NewStringUTF(env, buf);
      (*env)->ReleaseStringUTFChars(env, jpath, path);
      free(buf);
      return result;

    } else {
      free(buf);
      bufsize *= 2;
    }
  }
}
