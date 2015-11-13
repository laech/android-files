#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include "util.h"

void Java_l_files_fs_local_Stat_chmod(
        JNIEnv *env, jclass clazz, jbyteArray jpath, jint mode) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    int result = TEMP_FAILURE_RETRY(chmod(path, (mode_t) mode));
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Stat_mkdir(
        JNIEnv *env, jclass clazz, jbyteArray jpath, jint mode) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    int result = TEMP_FAILURE_RETRY(mkdir(path, (mode_t) mode));
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
