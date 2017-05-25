#include <jni.h>
#include <sys/stat.h>
#include "util.h"

static jmethodID stat_constructor;

void Java_l_files_fs_Stat_init(JNIEnv *env, jclass class) {

    init_int_field(env, class, "S_IFMT", S_IFMT);
    init_int_field(env, class, "S_IFSOCK", S_IFSOCK);
    init_int_field(env, class, "S_IFLNK", S_IFLNK);
    init_int_field(env, class, "S_IFREG", S_IFREG);
    init_int_field(env, class, "S_IFBLK", S_IFBLK);
    init_int_field(env, class, "S_IFDIR", S_IFDIR);
    init_int_field(env, class, "S_IFCHR", S_IFCHR);
    init_int_field(env, class, "S_IFIFO", S_IFIFO);
    init_int_field(env, class, "S_ISUID", S_ISUID);
    init_int_field(env, class, "S_ISGID", S_ISGID);
    init_int_field(env, class, "S_ISVTX", S_ISVTX);
    init_int_field(env, class, "S_IRWXU", S_IRWXU);
    init_int_field(env, class, "S_IRUSR", S_IRUSR);
    init_int_field(env, class, "S_IWUSR", S_IWUSR);
    init_int_field(env, class, "S_IXUSR", S_IXUSR);
    init_int_field(env, class, "S_IRWXG", S_IRWXG);
    init_int_field(env, class, "S_IRGRP", S_IRGRP);
    init_int_field(env, class, "S_IWGRP", S_IWGRP);
    init_int_field(env, class, "S_IXGRP", S_IXGRP);
    init_int_field(env, class, "S_IRWXO", S_IRWXO);
    init_int_field(env, class, "S_IROTH", S_IROTH);
    init_int_field(env, class, "S_IWOTH", S_IWOTH);
    init_int_field(env, class, "S_IXOTH", S_IXOTH);

    stat_constructor = (*env)->GetMethodID(env, class, "<init>", "(IJJIJ)V");
}

jobject new_stat(JNIEnv *env, jclass stat_class, struct stat *stat) {
    return (*env)->NewObject(
            env,
            stat_class,
            stat_constructor,
            (jint) (*stat).st_mode,
            (jlong) (*stat).st_size,
            (jlong) (*stat).st_mtim.tv_sec,
            (jint) (*stat).st_mtim.tv_nsec,
            (jlong) (*stat).st_blocks);
}

jobject do_stat(JNIEnv *env, jclass class, jbyteArray jpath, jboolean is_lstat) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return NULL;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    struct stat sb;
    int rc = (JNI_TRUE == is_lstat) ? lstat(path, &sb) : stat(path, &sb);

    if (-1 == rc) {
        throw_errno_exception(env);
        return NULL;
    }

    return new_stat(env, class, &sb);

}

jobject Java_l_files_fs_Stat_stat(JNIEnv *env, jclass class, jbyteArray jpath) {
    return do_stat(env, class, jpath, JNI_FALSE);
}

jobject Java_l_files_fs_Stat_lstat(JNIEnv *env, jclass class, jbyteArray jpath) {
    return do_stat(env, class, jpath, JNI_TRUE);
}

jobject Java_l_files_fs_Stat_fstat(JNIEnv *env, jclass class, jint fd) {

    struct stat sb;
    int rc = fstat(fd, &sb);
    if (-1 == rc) {
        throw_errno_exception(env);
        return NULL;
    }

    return new_stat(env, class, &sb);
}

void Java_l_files_fs_Stat_chmod(JNIEnv *env, jclass class, jbyteArray jpath, jint mode) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int result = chmod(path, (mode_t) mode);
    if (-1 == result) {
        throw_errno_exception(env);
    }

}

void Java_l_files_fs_Stat_mkdir(JNIEnv *env, jclass class, jbyteArray jpath, jint mode) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    int result = mkdir(path, (mode_t) mode);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
