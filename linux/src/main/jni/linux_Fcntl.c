#include <jni.h>
#include <fcntl.h>
#include "util.h"

jint Java_linux_Fcntl_open(JNIEnv *env, jclass class, jbyteArray jpath, jint flags, jint mode) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return -1;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int fd = open(path, flags, mode);

    if (-1 == fd) {
        throw_errno_exception(env);
    }

    return fd;

}
