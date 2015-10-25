#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include "util.h"

void Java_l_files_fs_local_Stat_chmod(
        JNIEnv *env, jclass clazz, jstring jpath, jint mode) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int result = TEMP_FAILURE_RETRY(chmod(path, (mode_t) mode));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Stat_mkdir(
        JNIEnv *env, jclass clazz, jstring jpath, jint mode) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int result = TEMP_FAILURE_RETRY(mkdir(path, (mode_t) mode));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}
