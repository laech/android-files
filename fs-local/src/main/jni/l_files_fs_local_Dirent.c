#include <jni.h>
#include <dirent.h>
#include <fcntl.h>
#include "util.h"

static jmethodID dirent_create;

void Java_l_files_fs_local_Dirent_init(JNIEnv *env, jclass clazz) {
    dirent_create = (*env)->GetStaticMethodID(
            env, clazz, "create", "(JILjava/lang/String;)Ll/files/fs/local/Dirent;");
}

jlong Java_l_files_fs_local_Dirent_opendir(
        JNIEnv *env, jclass clazz, jstring jpath, jboolean followLink) {

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (NULL == path) {
        return -1;
    }

    int flags = O_DIRECTORY;
    if (JNI_FALSE == followLink) {
        flags |= O_NOFOLLOW;
    }
    int fd = open(path, flags);
    (*env)->ReleaseStringUTFChars(env, jpath, path);

    if (-1 == fd) {
        throw_errno_exception(env);
        return -1;
    }

    return (jlong) (intptr_t) fdopendir(fd);
}

void Java_l_files_fs_local_Dirent_closedir(JNIEnv *env, jclass clazz, jlong jdir) {
    DIR *dir = (DIR *) (intptr_t) jdir;
    if (0 != closedir(dir)) {
        throw_errno_exception(env);
    }
}

jobject Java_l_files_fs_local_Dirent_readdir(JNIEnv *env, jclass clazz, jlong jdir) {
    DIR *dir = (DIR *) (intptr_t) jdir;
    struct dirent entry;
    struct dirent *result;

    if (0 != readdir_r(dir, &entry, &result)) {
        throw_errno_exception(env);
        return NULL;
    }

    if (NULL == result) {
        return NULL;
    }

    jstring jname = (*env)->NewStringUTF(env, entry.d_name);
    return (*env)->CallStaticObjectMethod(
            env, clazz, dirent_create, entry.d_ino, entry.d_type, jname);
}
