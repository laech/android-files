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
            "(JJIJIIJJJIJIJIJJ)Ll/files/fs/local/Stat;"
    );
}

jobject do_stat(JNIEnv *env, jclass clazz, jstring jpath, jboolean is_lstat) {
    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return NULL;
    }
    // TODO use stat64/lstat64
    struct stat sb;
    int rc = (JNI_TRUE == is_lstat)
             ? TEMP_FAILURE_RETRY(lstat(path, &sb))
             : TEMP_FAILURE_RETRY(stat(path, &sb));

    if (-1 == rc) {
        (*env)->ReleaseStringUTFChars(env, jpath, path);
        throw_errno_exception(env);
        return NULL;
    } else {
        (*env)->ReleaseStringUTFChars(env, jpath, path);
    }

    return (*env)->CallStaticObjectMethod(
            env,
            clazz,
            stat_create,
            (jlong) sb.st_dev,
            (jlong) sb.st_ino,
            (jint) sb.st_mode,
            (jlong) sb.st_nlink,
            (jint) sb.st_uid,
            (jint) sb.st_gid,
            (jlong) sb.st_rdev,
            (jlong) sb.st_size,
            (jlong) sb.st_atime,
            (jint) sb.st_atime_nsec,
            (jlong) sb.st_mtime,
            (jint) sb.st_mtime_nsec,
            (jlong) sb.st_ctime,
            (jint) sb.st_ctime_nsec,
            (jlong) sb.st_blksize,
            (jlong) sb.st_blocks
    );
}

jobject Java_l_files_fs_local_Stat_stat64(JNIEnv *env, jclass clazz, jstring jpath) {
    return do_stat(env, clazz, jpath, JNI_FALSE);
}

jobject Java_l_files_fs_local_Stat_lstat64(JNIEnv *env, jclass clazz, jstring jpath) {
    return do_stat(env, clazz, jpath, JNI_TRUE);
}

void Java_l_files_fs_local_Stat_chmod(
        JNIEnv *env, jclass clazz, jstring jpath, jint mode) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int result = TEMP_FAILURE_RETRY(chmod(path, (mode_t) mode));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}

void Java_l_files_fs_local_Stat_mkdir(
        JNIEnv *env, jclass clazz, jstring jpath, jint mode) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int result = TEMP_FAILURE_RETRY(mkdir(path, (mode_t) mode));

    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == result) {
        throw_errno_exception(env);
    }
}
