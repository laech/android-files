#include <jni.h>
#include <fcntl.h>
#include <sys/stat.h>
#include "util.h"

void setTimes(
        JNIEnv *env,
        jbyteArray jpath,
        const struct timespec times[2],
        jboolean followLink
) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int flags = JNI_FALSE == followLink ? AT_SYMLINK_NOFOLLOW : 0;
    int result = utimensat(AT_FDCWD, path, times, flags);
    if (0 != result) {
        throw_errno_exception(env);
    }

}

void Java_l_files_fs_local_LocalFileSystem_setModificationTime(
        JNIEnv *env,
        jclass clazz,
        jbyteArray jpath,
        jlong seconds,
        jint nanos,
        jboolean followLink
) {

    struct timespec times[2];
    times[0].tv_nsec = UTIME_OMIT;
    times[1].tv_sec = seconds;
    times[1].tv_nsec = nanos;
    setTimes(env, jpath, times, followLink);

}
