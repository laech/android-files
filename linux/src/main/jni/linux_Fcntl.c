#include <jni.h>
#include <fcntl.h>
#include "util.h"

void Java_linux_Fcntl_init(JNIEnv *env, jclass class) {
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_ACCMODE", "I"), O_ACCMODE);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_RDONLY", "I"), O_RDONLY);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_WRONLY", "I"), O_WRONLY);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_RDWR", "I"), O_RDWR);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_CREAT", "I"), O_CREAT);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_EXCL", "I"), O_EXCL);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_NOCTTY", "I"), O_NOCTTY);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_TRUNC", "I"), O_TRUNC);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_APPEND", "I"), O_APPEND);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_NONBLOCK", "I"), O_NONBLOCK);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_DSYNC", "I"), O_DSYNC);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "FASYNC", "I"), FASYNC);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_DIRECT", "I"), O_DIRECT);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_LARGEFILE", "I"), O_LARGEFILE);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_DIRECTORY", "I"), O_DIRECTORY);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_NOFOLLOW", "I"), O_NOFOLLOW);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_NOATIME", "I"), O_NOATIME);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_CLOEXEC", "I"), O_CLOEXEC);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "__O_SYNC", "I"), __O_SYNC);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_SYNC", "I"), O_SYNC);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_PATH", "I"), O_PATH);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "__O_TMPFILE", "I"), __O_TMPFILE);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_TMPFILE", "I"), O_TMPFILE);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_TMPFILE_MASK", "I"), O_TMPFILE_MASK);
    (*env)->SetStaticIntField(env, class, (*env)->GetStaticFieldID(env, class, "O_NDELAY", "I"), O_NDELAY);
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
