#include <jni.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>
#include "util.h"

void setTimes(JNIEnv* env, jstring jpath, const struct timespec times[2]) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return;
    }

    int result = utimensat(AT_FDCWD, path, times, AT_SYMLINK_NOFOLLOW);
    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (0 != result) {
        throw_errno_exception(env);
    }

}

void Java_l_files_fs_local_LocalResource_setModificationTime(
        JNIEnv* env, jclass clazz, jstring jpath, jlong seconds, jint nanos) {

    struct timespec times[2];
    times[0].tv_nsec = UTIME_OMIT;
    times[1].tv_sec = seconds;
    times[1].tv_nsec = nanos;
    setTimes(env, jpath, times);

}

void Java_l_files_fs_local_LocalResource_setAccessTime(
        JNIEnv* env, jclass clazz, jstring jpath, jlong seconds, jint nanos) {

    struct timespec times[2];
    times[0].tv_sec = seconds;
    times[0].tv_nsec = nanos;
    times[1].tv_nsec = UTIME_OMIT;
    setTimes(env, jpath, times);

}
