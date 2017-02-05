#include <jni.h>
#include <stdio.h>
#include "util.h"

void Java_linux_Stdio_remove(JNIEnv *env, jclass clazz, jbyteArray jpath) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    int result = remove(path);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_linux_Stdio_rename(
        JNIEnv *env,
        jclass clazz,
        jbyteArray joldpath,
        jbyteArray jnewpath) {

    if (NULL == joldpath) {
        throw_null_pointer_exception(env, "Old path is null");
        return;
    }

    if (NULL == jnewpath) {
        throw_null_pointer_exception(env, "New path is null");
        return;
    }

    JBYTE_ARRAY_TO_CHARS(env, oldpath, joldpath)
    JBYTE_ARRAY_TO_CHARS(env, newpath, jnewpath)

    int result = rename(oldpath, newpath);
    if (-1 == result) {
        throw_errno_exception(env);
    }

}
