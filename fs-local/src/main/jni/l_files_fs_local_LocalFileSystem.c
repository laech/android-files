#include <jni.h>
#include <fcntl.h>
#include <ftw.h>
#include <fts.h>
#include "util.h"

static jmethodID accumulate;

void Java_l_files_fs_local_LocalFileSystem_init(JNIEnv *env, jclass clazz) {
    jclass size_visitor_class = (*env)->FindClass(env, "l/files/fs/FileSystem$SizeVisitor");
    if (NULL != size_visitor_class) {
        accumulate = (*env)->GetMethodID(env, size_visitor_class, "onSize", "(JJ)Z");
    }
}

void setTimes(
        JNIEnv *env,
        jbyteArray jpath,
        const struct timespec times[2],
        jboolean followLink) {

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
        jboolean followLink) {

    struct timespec times[2];
    times[0].tv_nsec = UTIME_OMIT;
    times[1].tv_sec = seconds;
    times[1].tv_nsec = nanos;
    setTimes(env, jpath, times, followLink);

}

void Java_l_files_fs_local_LocalFileSystem_traverseSize(
        JNIEnv *env,
        jclass clazz,
        jbyteArray jpath,
        jboolean followLink,
        jobject callback) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    char *const paths[2] = {path, NULL};

    int options = FTS_PHYSICAL | FTS_NOCHDIR | FTS_NOSTAT;
    if (JNI_TRUE == followLink) {
        options |= FTS_COMFOLLOW;
    }

    FTS *fts = fts_open(paths, options, NULL);
    if (NULL == fts) {
        throw_errno_exception(env);
        return;
    }

    struct stat sb;
    for (; ;) {

        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            break;
        }

        FTSENT *ent = fts_read(fts);

        if (NULL == ent) {
            if (0 != errno) {
                throw_errno_exception(env);
            }
            break;
        }

        if (FTS_DP == ent->fts_info) {
            continue;
        }

        if (0 != lstat(ent->fts_path, &sb)) {
            continue;
        }

        if (JNI_FALSE == (*env)->CallBooleanMethod(
                env,
                callback,
                accumulate,
                sb.st_size,
                sb.st_blocks * 512)) {
            break;
        }

    }

    if (0 != fts_close(fts)) {
        throw_errno_exception(env);
    }

}
