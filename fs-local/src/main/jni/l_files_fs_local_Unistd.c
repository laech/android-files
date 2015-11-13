#include <jni.h>
#include <errno.h>
#include <unistd.h>
#include "util.h"
#include <linux/limits.h>

void Java_l_files_fs_local_Unistd_close(JNIEnv *env, jclass clazz, jint fd) {
    // See https://android.googlesource.com/platform/libcore/+/master/luni/src/main/native/libcore_io_Posix.cpp
    // Even if close(2) fails with EINTR, the fd will have been closed.
    // Using TEMP_FAILURE_RETRY will either lead to EBADF or closing someone else's fd.
    // http://lkml.indiana.edu/hypermail/linux/kernel/0509.1/0877.html
    if (-1 == close((int) fd)) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Unistd_symlink(
        JNIEnv *env, jclass clazz, jbyteArray jtarget, jbyteArray jlinkpath) {

    jsize targetlen = (*env)->GetArrayLength(env, jtarget);
    char targetpath[targetlen + 1];
    (*env)->GetByteArrayRegion(env, jtarget, 0, targetlen, targetpath);
    targetpath[targetlen] = '\0';

    jsize linklen = (*env)->GetArrayLength(env, jlinkpath);
    char linkpath[linklen + 1];
    (*env)->GetByteArrayRegion(env, jlinkpath, 0, linklen, linkpath);
    linkpath[linklen] = '\0';

    int result = TEMP_FAILURE_RETRY(symlink(targetpath, linkpath));
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Unistd_access(
        JNIEnv *env, jclass clazz, jbyteArray jpath, jint mode) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    int result = TEMP_FAILURE_RETRY(access(path, mode));
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

jbyteArray Java_l_files_fs_local_Unistd_readlink(
        JNIEnv *env, jclass clazz, jbyteArray jpath) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    char buf[PATH_MAX];
    ssize_t count = TEMP_FAILURE_RETRY(readlink(path, buf, PATH_MAX - 1));

    if (-1 == count) {
        throw_errno_exception(env);
        return NULL;
    }

    buf[count] = '\0';

    jbyteArray link = (*env)->NewByteArray(env, (jsize) count);
    (*env)->SetByteArrayRegion(env, link, 0, (jsize) count, buf);
    return link;
}
