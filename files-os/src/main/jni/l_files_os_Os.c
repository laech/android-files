#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
/*#include <android/log.h>*/

/*#define TAG "Os.c"*/

jobject doStat(JNIEnv* env, jstring jpath, jboolean is_lstat) {
  const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
  if (NULL == path) {
    return NULL;
  }
  struct stat sb;
  int rc = (JNI_TRUE == is_lstat)
      ? TEMP_FAILURE_RETRY(lstat(path, &sb))
      : TEMP_FAILURE_RETRY(stat(path, &sb));
  (*env)->ReleaseStringUTFChars(env, jpath, path);
  if (-1 == rc) {
    // TODO
    // throwErrnoException(env, isLstat ? "lstat" : "stat");
    return NULL;
  }

/*
  __android_log_write(ANDROID_LOG_WARN, TAG, path);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_dev is %llu", sb.st_dev);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_ino is %llu", sb.st_ino);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_mode is %u", sb.st_mode);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_nlink is %u", sb.st_nlink);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_uid is %lu", sb.st_uid);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_gid is %lu", sb.st_gid);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_rdev is %llu", sb.st_rdev);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_size is %lld", sb.st_size);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_atime is %lu", sb.st_atime);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_mtime is %lu", sb.st_mtime);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_ctime is %lu", sb.st_ctime);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_blksize is %ld", sb.st_blksize);
  __android_log_print(ANDROID_LOG_WARN, TAG, "st_blocks is %llu", sb.st_blocks);
*/

  jclass clazz = (*env)->FindClass(env, "l/files/os/io/Stat");
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

jobject Java_l_files_os_io_Os_stat(JNIEnv* env, jclass clazz, jstring jpath) {
  return doStat(env, jpath, JNI_FALSE);
}

jobject Java_l_files_os_io_Os_lstat(JNIEnv* env, jclass clazz, jstring jpath) {
  return doStat(env, jpath, JNI_TRUE);
}

jlong Java_l_files_os_io_Os_inode(JNIEnv *env, jobject clazz, jstring jpath) {
  const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
  if (NULL == path) {
    return -1;
  }
  struct stat sb;
  int result = TEMP_FAILURE_RETRY(stat(path, &sb));
  (*env)->ReleaseStringUTFChars(env, jpath, path);
  return (-1 == result) ? -1 : sb.st_ino;
}

jboolean Java_l_files_os_io_Os_symlink(JNIEnv *env, jclass clazz, jstring jsrc, jstring jdst) {
  const char *src = (*env)->GetStringUTFChars(env, jsrc, NULL);
  const char *dst = (*env)->GetStringUTFChars(env, jdst, NULL);
  int result = TEMP_FAILURE_RETRY(symlink(src, dst));
  (*env)->ReleaseStringUTFChars(env, jsrc, src);
  (*env)->ReleaseStringUTFChars(env, jdst, dst);
  return (-1 == result) ? JNI_FALSE : JNI_TRUE;
}
