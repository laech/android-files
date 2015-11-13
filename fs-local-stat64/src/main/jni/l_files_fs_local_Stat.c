#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#include "util.h"

static jmethodID stat_create;

void Java_l_files_fs_local_Stat_init(JNIEnv *env, jclass clazz) {
    stat_create = (*env)->GetStaticMethodID(
            env,
            clazz,
            "create",
            "(JJIJIIJJJIJIJJ)Ll/files/fs/local/Stat;"
    );
}

jobject to_java_stat64(JNIEnv *env, jclass clazz, struct stat64 *sb) {
    return (*env)->CallStaticObjectMethod(
            env,
            clazz,
            stat_create,
            (jlong) (*sb).st_dev,
            (jlong) (*sb).st_ino,
            (jint) (*sb).st_mode,
            (jlong) (*sb).st_nlink,
            (jint) (*sb).st_uid,
            (jint) (*sb).st_gid,
            (jlong) (*sb).st_rdev,
            (jlong) (*sb).st_size,
            (jlong) (*sb).st_mtime,
            (jint) (*sb).st_mtime_nsec,
            (jlong) (*sb).st_ctime,
            (jint) (*sb).st_ctime_nsec,
            (jlong) (*sb).st_blksize,
            (jlong) (*sb).st_blocks
    );
}

jobject do_stat(JNIEnv *env, jclass clazz, jbyteArray jpath, jboolean is_lstat) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    struct stat64 sb;
    int rc = (JNI_TRUE == is_lstat)
             ? TEMP_FAILURE_RETRY(lstat64(path, &sb))
             : TEMP_FAILURE_RETRY(stat64(path, &sb));

    if (-1 == rc) {
        throw_errno_exception(env);
        return NULL;
    }

    return to_java_stat64(env, clazz, &sb);
}

jobject Java_l_files_fs_local_Stat_stat(JNIEnv *env, jclass clazz, jbyteArray jpath) {
    return do_stat(env, clazz, jpath, JNI_FALSE);
}

jobject Java_l_files_fs_local_Stat_lstat(JNIEnv *env, jclass clazz, jbyteArray jpath) {
    return do_stat(env, clazz, jpath, JNI_TRUE);
}

jobject Java_l_files_fs_local_Stat_fstat(JNIEnv *env, jclass clazz, jint fd) {
    struct stat64 sb;
    if (-1 == TEMP_FAILURE_RETRY(fstat64(fd, &sb))) {
        throw_errno_exception(env);
        return NULL;
    }
    return to_java_stat64(env, clazz, &sb);
}
