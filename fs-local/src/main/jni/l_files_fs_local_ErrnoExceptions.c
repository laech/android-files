#include <jni.h>
#include <string.h>
#include <errno.h>

jstring Java_l_files_fs_local_ErrnoExceptions_strerror(
        JNIEnv *env,
        jclass clazz,
        jint err)
{
    return (*env)->NewStringUTF(env, strerror(err));
}
