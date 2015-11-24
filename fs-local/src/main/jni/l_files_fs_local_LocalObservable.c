#include <jni.h>
#include <errno.h>
#include <sys/inotify.h>
#include "util.h"
#include <unistd.h>
#include <sys/vfs.h>

static jmethodID method_onEvent;
static jmethodID method_isClosed;

void Java_l_files_fs_local_LocalObservable_init(JNIEnv *env, jclass clazz) {
    method_onEvent = (*env)->GetMethodID(
            env, clazz, "onEvent", "(II[B)V");
    method_isClosed = (*env)->GetMethodID(env, clazz, "isClosed", "()Z");
}

jboolean Java_l_files_fs_local_LocalObservable_isProcfs(
        JNIEnv *env, jclass clazz, jbyteArray jpath) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    struct statfs buff;

    if (-1 == TEMP_RETRY(statfs(path, &buff))) {
        throw_errno_exception(env);
        return JNI_FALSE;

    } else {
        return (jboolean) (PROC_SUPER_MAGIC == buff.f_type);
    }

}


void Java_l_files_fs_local_LocalObservable_observe(
        JNIEnv *env, jobject object, jint fd) {

    size_t BUF_SIZE = 1024;
    ssize_t num_bytes;
    char buf[BUF_SIZE];
    char *p;
    struct inotify_event *event;

    while (!(*env)->CallBooleanMethod(env, object, method_isClosed)) {

        num_bytes = read(fd, buf, BUF_SIZE);
        if (num_bytes == -1) {

            if ((*env)->CallBooleanMethod(env, object, method_isClosed)) {
                return;
            }

            if (EINTR == errno) {
                continue;
            }

            throw_errno_exception(env);
            return;

        }

        for (p = buf; p < buf + num_bytes;) {
            event = (struct inotify_event *) p;

            jbyteArray path = NULL;
            if (event->len > 0) {
                jsize len = (jsize) strlen(event->name);
                path = (*env)->NewByteArray(env, len);
                (*env)->SetByteArrayRegion(env, path, 0, len, (const jbyte *) event->name);
            }

            (*env)->CallVoidMethod(
                    env, object, method_onEvent, event->wd, event->mask, path);

            if ((*env)->ExceptionCheck(env)) {
                (*env)->ExceptionDescribe(env);
                (*env)->ExceptionClear(env);
            }

            if (path != NULL) {
                (*env)->DeleteLocalRef(env, path);
            }

            p += sizeof(struct inotify_event) + event->len;
        }
    }
}
