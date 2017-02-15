#include <jni.h>
#include <sys/inotify.h>
#include "util.h"

void Java_linux_Inotify_init(JNIEnv *env, jclass class) {
    init_int_field(env, class, "IN_ACCESS", IN_ACCESS);
    init_int_field(env, class, "IN_MODIFY", IN_MODIFY);
    init_int_field(env, class, "IN_ATTRIB", IN_ATTRIB);
    init_int_field(env, class, "IN_CLOSE_WRITE", IN_CLOSE_WRITE);
    init_int_field(env, class, "IN_CLOSE_NOWRITE", IN_CLOSE_NOWRITE);
    init_int_field(env, class, "IN_OPEN", IN_OPEN);
    init_int_field(env, class, "IN_MOVED_FROM", IN_MOVED_FROM);
    init_int_field(env, class, "IN_MOVED_TO", IN_MOVED_TO);
    init_int_field(env, class, "IN_CREATE", IN_CREATE);
    init_int_field(env, class, "IN_DELETE", IN_DELETE);
    init_int_field(env, class, "IN_DELETE_SELF", IN_DELETE_SELF);
    init_int_field(env, class, "IN_MOVE_SELF", IN_MOVE_SELF);
    init_int_field(env, class, "IN_UNMOUNT", IN_UNMOUNT);
    init_int_field(env, class, "IN_Q_OVERFLOW", IN_Q_OVERFLOW);
    init_int_field(env, class, "IN_IGNORED", IN_IGNORED);
    init_int_field(env, class, "IN_CLOSE", IN_CLOSE);
    init_int_field(env, class, "IN_MOVE", IN_MOVE);
    init_int_field(env, class, "IN_ONLYDIR", IN_ONLYDIR);
    init_int_field(env, class, "IN_DONT_FOLLOW", IN_DONT_FOLLOW);
    init_int_field(env, class, "IN_MASK_ADD", IN_MASK_ADD);
    init_int_field(env, class, "IN_ISDIR", IN_ISDIR);
    init_int_field(env, class, "IN_ONESHOT", IN_ONESHOT);
    init_int_field(env, class, "IN_ALL_EVENTS", IN_ALL_EVENTS);
}

jint Java_linux_Inotify_inotify_1init(JNIEnv *env, jclass class) {
    int fd = inotify_init();
    if (-1 == fd) {
        throw_errno_exception(env);
    }
    return fd;
}

jint Java_linux_Inotify_inotify_1add_1watch(
        JNIEnv *env, jclass class, jint fd, jbyteArray jpath, jint mask) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return -1;
    }

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    int wd = inotify_add_watch(fd, path, (uint32_t) mask);
    if (wd == -1) {
        throw_errno_exception(env);
    }
    return wd;
}

void Java_linux_Inotify_inotify_1rm_1watch(JNIEnv *env, jclass class, jint fd, jint wd) {
    int result = inotify_rm_watch(fd, (uint32_t) wd);
    if (-1 == result) {
        throw_errno_exception(env);
    }
}
