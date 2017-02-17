#include <jni.h>
#include <fcntl.h>
#include <ftw.h>
#include "util.h"

void Java_l_files_fs_Path_setModificationTime(
        JNIEnv *env,
        jclass clazz,
        jbyteArray jpath,
        jlong seconds,
        jint nanos,
        jboolean followLink) {

    struct timespec times[2];
    times[0].tv_nsec = UTIME_OMIT;
    times[1].tv_sec = seconds;
    times[1].tv_nsec = nanos;

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    int flags = JNI_FALSE == followLink ? AT_SYMLINK_NOFOLLOW : 0;
    int result = utimensat(AT_FDCWD, path, times, flags);
    if (0 != result) {
        throw_errno_exception(env);
    }

}
