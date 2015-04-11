#include <jni.h>
#include <errno.h>
#include <sys/inotify.h>
#include "util.h"

jint Java_l_files_fs_local_Inotify_init(JNIEnv *env, jclass clazz) {
    int fd = inotify_init();
    if (-1 == fd) {
        throw_errno_exception(env);
    }
    return fd;
}

jint Java_l_files_fs_local_Inotify_init1(JNIEnv *env, jclass clazz, jint flags) {
    int fd = inotify_init1(flags);
    if (-1 == fd) {
        throw_errno_exception(env);
    }
    return fd;
}

jint Java_l_files_fs_local_Inotify_addWatch(JNIEnv *env, jclass clazz, jint fd, jstring jpath, jint mask) {
    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return;
    }
    int wd = inotify_add_watch(fd, path, mask);
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    if (wd == -1) {
        throw_errno_exception(env);
    }
    return wd;
}

void Java_l_files_fs_local_Inotify_removeWatch(JNIEnv *env, jclass clazz, jint fd, jint wd) {
    int result = inotify_rm_watch(fd, wd);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
