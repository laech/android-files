#include <jni.h>
#include <dirent.h>
#include <fcntl.h>
#include "util.h"

static jmethodID on_next;

void Java_l_files_fs_local_Dirent_init(JNIEnv *env, jclass clazz) {
    jclass callback_class = (*env)->FindClass(env, "l/files/fs/local/Dirent$Callback");
    if (NULL != callback_class) {
        on_next = (*env)->GetMethodID(env, callback_class, "onNext", "([BIZ)Z");
    }
}

DIR *open_directory(
        JNIEnv *env,
        jbyteArray jpath,
        jboolean followLink) {

    jsize path_len = (*env)->GetArrayLength(env, jpath);
    char path[path_len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, path_len, (jbyte *) path);
    path[path_len] = '\0';

    int flags = O_DIRECTORY;
    if (JNI_FALSE == followLink) {
        flags |= O_NOFOLLOW;
    }

    int fd = open(path, flags);
    if (-1 == fd) {
        return NULL;
    }

    return fdopendir(fd);
}

void Java_l_files_fs_local_Dirent_list(
        JNIEnv *env,
        jclass clazz,
        jbyteArray jpath,
        jboolean followLink,
        jobject callback) {

    DIR *dir = open_directory(env, jpath, followLink);
    if (NULL == dir) {
        throw_errno_exception(env);
        return;
    }

    jsize name_len;
    jboolean isdir;
    jbyteArray name_buf = (*env)->NewByteArray(env, 256);
    struct dirent entry;
    struct dirent *result;

    for (; ;) {

        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            break;
        }

        if (0 != readdir_r(dir, &entry, &result)) {
            throw_errno_exception(env);
            break;
        }

        if (NULL == result) {
            break;
        }

        if (strcmp(entry.d_name, ".") == 0 ||
            strcmp(entry.d_name, "..") == 0) {
            continue;
        }

        isdir = (jboolean) (DT_DIR == entry.d_type);
        name_len = (jsize) strlen(entry.d_name);
        (*env)->SetByteArrayRegion(
                env,
                name_buf,
                0,
                name_len,
                (const jbyte *) entry.d_name);

        if (!(*env)->CallBooleanMethod(env, callback, on_next, name_buf, name_len, isdir)) {
            break;
        }

    }

    if (0 != closedir(dir)) {
        throw_errno_exception(env);
    }

}
