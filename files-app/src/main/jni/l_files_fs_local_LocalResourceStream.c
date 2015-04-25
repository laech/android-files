#include <jni.h>
#include <errno.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "util.h"
#include <android/log.h>

static jmethodID method_notify;

void Java_l_files_fs_local_LocalResourceStream_init(JNIEnv *env, jclass clazz) {
  method_notify = (*env)->GetMethodID(env, clazz, "notify",
      "(JLjava/lang/String;Z)Z");
}

jlong Java_l_files_fs_local_LocalResourceStream_open(
        JNIEnv* env, jclass clazz, jstring jpath, jboolean followLink) {

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

    return (jlong)(intptr_t)fdopendir(fd);
}

void Java_l_files_fs_local_LocalResourceStream_close(
        JNIEnv *env, jclass clazz, jlong jdir) {

    DIR *dir = (DIR*)(intptr_t)jdir;
    if (0 != closedir(dir)) {
        throw_errno_exception(env);
    }

}

void Java_l_files_fs_local_LocalResourceStream_list(
        JNIEnv *env, jobject obj, jlong jdir) {

    DIR *dir = (DIR*)(intptr_t)jdir;
    for (;;) {
        errno = 0;
        struct dirent *entry = readdir(dir);
        if (NULL == entry) {
            if (0 != errno) {
                throw_errno_exception(env);
            }
            return;
        }

        jstring name = (*env)->NewStringUTF(env, entry->d_name);
        jboolean result = (*env)->CallBooleanMethod(env, obj, method_notify,
                entry->d_ino, name, DT_DIR == entry->d_type);

        if (NULL != name) {
            (*env)->DeleteLocalRef(env, name);
        }
        if (JNI_FALSE == result) {
            return;
        }

    }

}
