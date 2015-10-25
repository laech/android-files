#include <jni.h>
#include <stdio.h>
#include <errno.h>

void throw_errno_exception(JNIEnv *env) {
    jclass clazz = (*env)->FindClass(env, "l/files/fs/local/ErrnoException");
    if (NULL == clazz) {
        return;
    }

    jmethodID constructor = (*env)->GetMethodID(env, clazz, "<init>", "(I)V");
    if (NULL == constructor) {
        return;
    }

    jobject exception = (*env)->NewObject(env, clazz, constructor, errno);
    if (NULL == exception) {
        return;
    }

    (*env)->Throw(env, exception);
}
