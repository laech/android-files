#include <jni.h>
#include <errno.h>
#include <sys/inotify.h>
#include <unistd.h>
#include "util.h"

jint Java_l_files_fs_local_Inotify_internalInit(
        JNIEnv *env, jobject obj) {

    int fd = inotify_init();
    if (-1 == fd) {
        throw_errno_exception(env);
    }
    return fd;
}

jint Java_l_files_fs_local_Inotify_internalAddWatch(
        JNIEnv *env, jobject obj, jint fd, jbyteArray jpath, jint mask) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int wd = TEMP_FAILURE_RETRY(inotify_add_watch(fd, path, (uint32_t) mask));
    if (wd == -1) {
        throw_errno_exception(env);
    }
    return wd;
}

void Java_l_files_fs_local_Inotify_internalRemoveWatch(
        JNIEnv *env, jobject obj, jint fd, jint wd) {

    int result = TEMP_FAILURE_RETRY(inotify_rm_watch(fd, (uint32_t) wd));
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
