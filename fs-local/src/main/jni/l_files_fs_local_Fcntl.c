#include <jni.h>
#include <errno.h>
#include <fcntl.h>
#include "util.h"

jint Java_l_files_fs_local_Fcntl_open(
        JNIEnv *env, jclass clazz, jbyteArray jpath, jint flags, jint mode) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    int fd = TEMP_FAILURE_RETRY(open(path, flags, mode));

    if (-1 == fd) {
        throw_errno_exception(env);
        return NULL;
    }

    return fd;

}
