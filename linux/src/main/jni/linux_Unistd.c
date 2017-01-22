#include <jni.h>
#include <unistd.h>
#include "util.h"
#include <linux/limits.h>

void Java_linux_Unistd_init(JNIEnv *env, jclass class) {
    init_byte_field(env, class, "R_OK", R_OK);
    init_byte_field(env, class, "W_OK", W_OK);
    init_byte_field(env, class, "X_OK", X_OK);
    init_byte_field(env, class, "F_OK", F_OK);
}

void Java_linux_Unistd_close(JNIEnv *env, jclass class, jint fd) {
    // See https://android.googlesource.com/platform/libcore/+/master/luni/src/main/native/libcore_io_Posix.cpp
    // Even if close(2) fails with EINTR, the fd will have been closed.
    // Using TEMP_FAILURE_RETRY will either lead to EBADF or closing someone else's fd.
    // http://lkml.indiana.edu/hypermail/linux/kernel/0509.1/0877.html
    if (-1 == close((int) fd)) {
        throw_errno_exception(env);
    }
}

void Java_linux_Unistd_symlink(JNIEnv *env, jclass class, jbyteArray jtarget, jbyteArray jlinkpath) {

    if (NULL == jtarget) {
        throw_null_pointer_exception(env, "Target is null");
        return;
    }

    if (NULL == jlinkpath) {
        throw_null_pointer_exception(env, "Link is null");
        return;
    }

    JBYTE_ARRAY_TO_CHARS(env, targetpath, jtarget)
    JBYTE_ARRAY_TO_CHARS(env, linkpath, jlinkpath)

    int result = symlink(targetpath, linkpath);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_linux_Unistd_access(JNIEnv *env, jclass class, jbyteArray jpath, jint mode) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    int result = access(path, mode);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}

jbyteArray Java_linux_Unistd_readlink(JNIEnv *env, jclass class, jbyteArray jpath) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return NULL;
    }

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    char buf[PATH_MAX];
    ssize_t count = readlink(path, buf, PATH_MAX - 1);

    if (-1 == count) {
        throw_errno_exception(env);
        return NULL;
    }

    buf[count] = '\0';

    jbyteArray link = (*env)->NewByteArray(env, (jsize) count);
    (*env)->SetByteArrayRegion(env, link, 0, (jsize) count, (const jbyte *) buf);
    return link;
}
