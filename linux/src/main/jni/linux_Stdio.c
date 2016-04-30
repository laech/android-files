#include <jni.h>
#include <stdio.h>
#include "util.h"

void Java_linux_Stdio_remove(JNIEnv *env, jclass clazz, jbyteArray jpath) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int result = remove(path);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_linux_Stdio_rename(JNIEnv *env, jclass clazz, jbyteArray joldpath, jbyteArray jnewpath) {

    if (NULL == joldpath) {
        throw_null_pointer_exception(env, "Old path is null");
        return;
    }

    if (NULL == jnewpath) {
        throw_null_pointer_exception(env, "New path is null");
        return;
    }

    jsize oldlen = (*env)->GetArrayLength(env, joldpath);
    char oldpath[oldlen + 1];
    (*env)->GetByteArrayRegion(env, joldpath, 0, oldlen, (jbyte *) oldpath);
    oldpath[oldlen] = '\0';

    jsize newlen = (*env)->GetArrayLength(env, jnewpath);
    char newpath[newlen + 1];
    (*env)->GetByteArrayRegion(env, jnewpath, 0, newlen, (jbyte *) newpath);
    newpath[newlen] = '\0';

    int result = rename(oldpath, newpath);
    if (-1 == result) {
        throw_errno_exception(env);
    }

}
