#include <jni.h>
#include <dirent.h>
#include <fcntl.h>
#include "util.h"

static jmethodID dirent_create;

void Java_l_files_fs_local_Dirent_init(JNIEnv *env, jclass clazz) {
    dirent_create = (*env)->GetStaticMethodID(
            env, clazz, "create", "(JI[B)Ll/files/fs/local/Dirent;");
}

jlong Java_l_files_fs_local_Dirent_opendir(
        JNIEnv *env, jclass clazz, jbyteArray jpath, jboolean followLink) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    int flags = O_DIRECTORY;
    if (JNI_FALSE == followLink) {
        flags |= O_NOFOLLOW;
    }

    int fd = open(path, flags);

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

    jsize len = (jsize) strlen(entry.d_name);
    jbyteArray name = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, name, 0, len, entry.d_name);

    return (*env)->CallStaticObjectMethod(
            env, clazz, dirent_create, entry.d_ino, entry.d_type, name);
}
