#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include "util.h"

jobject do_stat(JNIEnv *env, jstring jpath, jboolean is_lstat) {
  const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
  if (NULL == path) {
    return NULL;
  }
  struct stat sb;
  int rc = (JNI_TRUE == is_lstat)
      ? TEMP_FAILURE_RETRY(lstat(path, &sb))
      : TEMP_FAILURE_RETRY(stat(path, &sb));
  if (-1 == rc) {
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    throw_os_exception(env, strerror(errno));
    return NULL;
  } else {
    (*env)->ReleaseStringUTFChars(env, jpath, path);
  }

  jclass clazz = (*env)->FindClass(env, "l/files/os/Stat");
  jmethodID ctor = (*env)->GetMethodID(env, clazz, "<init>", "(JJIJIIJJJJJJJ)V");
  return (*env)->NewObject(env, clazz, ctor,
      (jlong) sb.st_dev,
      (jlong) sb.st_ino,
      (jint)  sb.st_mode,
      (jlong) sb.st_nlink,
      (jint)  sb.st_uid,
      (jint)  sb.st_gid,
      (jlong) sb.st_rdev,
      (jlong) sb.st_size,
      (jlong) sb.st_atime,
      (jlong) sb.st_mtime,
      (jlong) sb.st_ctime,
      (jlong) sb.st_blksize,
      (jlong) sb.st_blocks);
}

jobject Java_l_files_os_Stat_stat(JNIEnv* env, jclass clazz, jstring jpath) {
  return do_stat(env, jpath, JNI_FALSE);
}

jobject Java_l_files_os_Stat_lstat(JNIEnv* env, jclass clazz, jstring jpath) {
  return do_stat(env, jpath, JNI_TRUE);
}