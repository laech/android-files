#include <jni.h>
#include <errno.h>
#include <dirent.h>
#include "util.h"

static jmethodID dirent_create;

void Java_l_files_fs_local_Dirent_init(JNIEnv *env, jclass clazz) {
  dirent_create = (*env)->GetStaticMethodID(env, clazz, "create",
      "(JILjava/lang/String;)Ll/files/fs/local/Dirent;");
}

jlong Java_l_files_fs_local_Dirent_opendir(JNIEnv *env, jclass clazz, jstring jpath) {
  const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
  DIR *dir = opendir(path);
  (*env)->ReleaseStringUTFChars(env, jpath, path);

  if (NULL == dir) {
    throw_errno_exception(env);
    return -1;
  } else {
    return (jlong)(intptr_t)dir;
  }
}

void Java_l_files_fs_local_Dirent_closedir(JNIEnv *env, jclass clazz, jlong jdir) {
  DIR *dir = (DIR*)(intptr_t)jdir;
  if (0 != closedir(dir)) {
    throw_errno_exception(env);
  }
}

jobject Java_l_files_fs_local_Dirent_readdir(JNIEnv *env, jclass clazz, jlong jdir) {
  DIR *dir = (DIR*)(intptr_t)jdir;
  errno = 0;
  struct dirent *entry = readdir(dir);
  if (NULL == entry) {
    if (0 != errno) {
      throw_errno_exception(env);
    }
    return NULL;
  }

  jstring jname = (*env)->NewStringUTF(env, entry->d_name);
  return (*env)->CallStaticObjectMethod(env, clazz, dirent_create,
      entry->d_ino, entry->d_type, jname);
}