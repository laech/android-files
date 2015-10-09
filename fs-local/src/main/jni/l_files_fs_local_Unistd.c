#include <jni.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include "util.h"

void Java_l_files_fs_local_Unistd_close(JNIEnv *env, jclass clazz, jint fd) {
    if (-1 == close((int)fd)) {
        throw_errno_exception(env);
    }
}
