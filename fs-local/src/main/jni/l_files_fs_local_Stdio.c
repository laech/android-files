#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include "util.h"

void Java_l_files_fs_local_Stdio_remove(
        JNIEnv *env, jclass clazz, jstring jpath) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int result = TEMP_FAILURE_RETRY(remove(path));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Stdio_rename(
        JNIEnv *env, jclass clazz, jstring joldpath, jstring jnewpath) {

    const char *oldpath = (*env)->GetStringUTFChars(env, joldpath, NULL);
    const char *newpath = (*env)->GetStringUTFChars(env, jnewpath, NULL);

    int result = TEMP_FAILURE_RETRY(rename(oldpath, newpath));

    (*env)->ReleaseStringUTFChars(env, joldpath, oldpath);
    (*env)->ReleaseStringUTFChars(env, jnewpath, newpath);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}
