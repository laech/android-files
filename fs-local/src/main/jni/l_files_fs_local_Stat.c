#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include "util.h"

static jmethodID stat_create;

void Java_l_files_fs_local_Stat_init(JNIEnv *env, jclass clazz) {
    stat_create = (*env)->GetStaticMethodID(
            env,
            clazz,
            "create",
            "(IJJIJ)Ll/files/fs/local/Stat;"
    );
}

jobject to_java_stat(JNIEnv *env, jclass clazz, struct stat *sb) {
    return (*env)->CallStaticObjectMethod(
            env,
            clazz,
            stat_create,
            (jint) (*sb).st_mode,
            (jlong) (*sb).st_size,
            (jlong) (*sb).st_mtime,
            (jint) (*sb).st_mtime_nsec,
            (jlong) (*sb).st_blocks
    );
}

jobject do_stat(JNIEnv *env, jclass clazz, jbyteArray jpath, jboolean is_lstat) {
    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    struct stat sb;
    int rc = (JNI_TRUE == is_lstat)
             ? TEMP_RETRY(lstat(path, &sb))
             : TEMP_RETRY(stat(path, &sb));

    if (-1 == rc) {
        throw_errno_exception(env);
        return NULL;
    }

    return to_java_stat(env, clazz, &sb);
}

jobject Java_l_files_fs_local_Stat_stat(JNIEnv *env, jclass clazz, jbyteArray jpath) {
    return do_stat(env, clazz, jpath, JNI_FALSE);
}

jobject Java_l_files_fs_local_Stat_lstat(JNIEnv *env, jclass clazz, jbyteArray jpath) {
    return do_stat(env, clazz, jpath, JNI_TRUE);
}

jobject Java_l_files_fs_local_Stat_fstat(JNIEnv *env, jclass clazz, jint fd) {
    struct stat sb;
    if (-1 == TEMP_RETRY(fstat(fd, &sb))) {
        throw_errno_exception(env);
        return NULL;
    }
    return to_java_stat(env, clazz, &sb);
}

void Java_l_files_fs_local_Stat_mkdir(
        JNIEnv *env, jclass clazz, jbyteArray jpath, jint mode) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int result = TEMP_RETRY(mkdir(path, (mode_t) mode));
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
