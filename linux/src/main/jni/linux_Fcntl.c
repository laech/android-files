#include <jni.h>
#include <fcntl.h>
#include "util.h"

void Java_linux_Fcntl_init(JNIEnv *env, jclass class) {
    init_int_field(env, class, "O_ACCMODE", O_ACCMODE);
    init_int_field(env, class, "O_RDONLY", O_RDONLY);
    init_int_field(env, class, "O_WRONLY", O_WRONLY);
    init_int_field(env, class, "O_RDWR", O_RDWR);
    init_int_field(env, class, "O_CREAT", O_CREAT);
    init_int_field(env, class, "O_EXCL", O_EXCL);
    init_int_field(env, class, "O_NOCTTY", O_NOCTTY);
    init_int_field(env, class, "O_TRUNC", O_TRUNC);
    init_int_field(env, class, "O_APPEND", O_APPEND);
    init_int_field(env, class, "O_NONBLOCK", O_NONBLOCK);
    init_int_field(env, class, "O_SYNC", O_SYNC);
    init_int_field(env, class, "FASYNC", FASYNC);
    init_int_field(env, class, "O_DIRECT", O_DIRECT);
    init_int_field(env, class, "O_LARGEFILE", O_LARGEFILE);
    init_int_field(env, class, "O_DIRECTORY", O_DIRECTORY);
    init_int_field(env, class, "O_NOFOLLOW", O_NOFOLLOW);
    init_int_field(env, class, "O_NOATIME", O_NOATIME);
    init_int_field(env, class, "O_NDELAY", O_NDELAY);
    init_int_field(env, class, "O_ASYNC", O_ASYNC);
    init_int_field(env, class, "O_CLOEXEC", O_CLOEXEC);
}

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
