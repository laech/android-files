#include <jni.h>
#include <errno.h>
#include <sys/inotify.h>
#include "util.h"

jint Java_l_files_fs_local_Inotify_internalInit(
        JNIEnv *env, jclass clazz) {

    int fd = inotify_init();
    if (-1 == fd) {
        throw_errno_exception(env);
    }
    return fd;
}

jint Java_l_files_fs_local_Inotify_internalInit1(
        JNIEnv *env, jclass clazz, jint flags) {

    int fd = inotify_init1(flags);
    if (-1 == fd) {
        throw_errno_exception(env);
    }
    return fd;
}

jint Java_l_files_fs_local_Inotify_internalAddWatch(
        JNIEnv *env, jclass clazz, jint fd, jstring jpath, jint mask) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return -1;
    }

    int wd = inotify_add_watch(fd, path, (uint32_t) mask);
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    if (wd == -1) {
        throw_errno_exception(env);
    }

    return wd;
}

void Java_l_files_fs_local_Inotify_internalRemoveWatch(
        JNIEnv *env, jclass clazz, jint fd, jint wd) {

    int result = inotify_rm_watch(fd, (uint32_t) wd);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
