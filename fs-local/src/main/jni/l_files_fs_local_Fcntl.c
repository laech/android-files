#include <jni.h>
#include <errno.h>
#include <fcntl.h>
#include "util.h"

jint Java_l_files_fs_local_Fcntl_open(
        JNIEnv *env, jclass clazz, jstring jpath, jint flags, jint mode) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int fd = TEMP_FAILURE_RETRY(open(path, flags, mode));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == fd) {
        throw_errno_exception(env);
        return NULL;
    }

    return fd;

}
