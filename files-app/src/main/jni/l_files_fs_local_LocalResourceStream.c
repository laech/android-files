#include <jni.h>
#include <errno.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "util.h"
#include <android/log.h>

jlong Java_l_files_fs_local_LocalResourceStream_open(
        JNIEnv* env, jclass clazz, jstring jpath) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return NULL;
    }

    int fd = open(path, O_DIRECTORY | O_NOFOLLOW);
    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == fd) {
        throw_errno_exception(env);
        return NULL;
    }

    return (jlong)(intptr_t)fdopendir(fd);
}
