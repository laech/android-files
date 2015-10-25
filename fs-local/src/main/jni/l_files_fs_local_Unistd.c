#include <jni.h>
#include <errno.h>
#include <unistd.h>
#include "util.h"
#include <linux/limits.h>

void Java_l_files_fs_local_Unistd_close(JNIEnv *env, jclass clazz, jint fd) {
    if (-1 == close((int) fd)) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Unistd_symlink(
        JNIEnv *env, jclass clazz, jstring jtarget, jstring jlinkpath) {

    const char *target = (*env)->GetStringUTFChars(env, jtarget, NULL);
    const char *linkpath = (*env)->GetStringUTFChars(env, jlinkpath, NULL);

    int result = TEMP_FAILURE_RETRY(symlink(target, linkpath));

    (*env)->ReleaseStringUTFChars(env, jtarget, target);
    (*env)->ReleaseStringUTFChars(env, jlinkpath, linkpath);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Unistd_access(
        JNIEnv *env, jclass clazz, jstring jpath, jint mode) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return;
    }
    int result = TEMP_FAILURE_RETRY(access(path, mode));
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

jstring Java_l_files_fs_local_Unistd_readlink(
        JNIEnv *env, jclass clazz, jstring jpath) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    char buf[PATH_MAX];

    ssize_t count = TEMP_FAILURE_RETRY(readlink(path, buf, PATH_MAX - 1));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == count) {
        throw_errno_exception(env);
        return NULL;
    }

    buf[count] = '\0';
    return (*env)->NewStringUTF(env, buf);
}
