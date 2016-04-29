#include <jni.h>
#include <sys/stat.h>
#include "util.h"

static jfieldID stat_mode;
static jfieldID stat_size;
static jfieldID stat_mtime;
static jfieldID stat_mtime_nsec;
static jfieldID stat_blocks;

void Java_linux_Stat_init(JNIEnv *env, jclass class) {

    stat_mode = (*env)->GetFieldID(env, class, "st_mode", "I");
    stat_size = (*env)->GetFieldID(env, class, "st_size", "J");
    stat_mtime = (*env)->GetFieldID(env, class, "st_mtime", "J");
    stat_mtime_nsec = (*env)->GetFieldID(env, class, "st_mtime_nsec", "I");
    stat_blocks = (*env)->GetFieldID(env, class, "st_blocks", "J");

}

void do_stat(JNIEnv *env, jclass class, jbyteArray jpath, jobject jstat, jboolean is_lstat) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    if (NULL == jstat) {
        throw_null_pointer_exception(env, "Stat is null");
        return;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

    struct stat sb;
    int rc = (JNI_TRUE == is_lstat) ? lstat(path, &sb) : stat(path, &sb);

    if (-1 == rc) {
        throw_errno_exception(env);
        return;
    }

    (*env)->SetIntField(env, jstat, stat_mode, sb.st_mode);
    (*env)->SetLongField(env, jstat, stat_size, sb.st_size);
    (*env)->SetLongField(env, jstat, stat_mtime, sb.st_mtime);
    (*env)->SetIntField(env, jstat, stat_mtime_nsec, (jint) sb.st_mtime_nsec);
    (*env)->SetLongField(env, jstat, stat_blocks, sb.st_blocks);

}

void Java_linux_Stat_stat(JNIEnv *env, jclass class, jbyteArray jpath, jobject jstat) {
    do_stat(env, class, jpath, jstat, JNI_FALSE);
}

void Java_linux_Stat_lstat(JNIEnv *env, jclass class, jbyteArray jpath, jobject jstat) {
    do_stat(env, class, jpath, jstat, JNI_TRUE);
}
