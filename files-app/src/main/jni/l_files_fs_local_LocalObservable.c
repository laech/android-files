#include <jni.h>
#include <errno.h>
#include <sys/inotify.h>
#include "util.h"
#include <android/log.h>

static jmethodID method_onEvent;
static jmethodID method_isClosed;
static jmethodID method_sleep;

void Java_l_files_fs_local_LocalObservable_init(JNIEnv* env, jclass clazz) {
    method_onEvent = (*env)->GetMethodID(env, clazz, "onEvent", "(IILjava/lang/String;)V");
    method_isClosed = (*env)->GetMethodID(env, clazz, "isClosed", "()Z");
    method_sleep = (*env)->GetMethodID(env, clazz, "sleep", "()V");
}


void Java_l_files_fs_local_LocalObservable_observe(
        JNIEnv* env, jobject object, jint fd) {

    int BUF_SIZE = 1024;
    int num_bytes;
    char buf[BUF_SIZE];
    char *p;
    struct inotify_event* event;

    while (!(*env)->CallBooleanMethod(env, object, method_isClosed)) {
        num_bytes = read(fd, buf, BUF_SIZE);
        if (num_bytes == -1) {
            if (EAGAIN == errno) {
                (*env)->CallVoidMethod(env, object, method_sleep);
                continue;
            }
            throw_errno_exception(env);
            return;
        }

        for (p = buf; p < buf + num_bytes; ) {
            event = (struct inotify_event *) p;

            jstring path = NULL;
            if (event->len > 0) {
                path = (*env)->NewStringUTF(env, event->name);
            }
            (*env)->CallVoidMethod(env, object, method_onEvent, event->wd, event->mask, path);
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
