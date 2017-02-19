#include <jni.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include "util.h"

static jclass dir_class;
static jmethodID dir_constructor;
static jfieldID dir_field_address;
static jfieldID dir_field_closed;

static jfieldID dirent_field_ino;
static jfieldID dirent_field_type;
static jfieldID dirent_field_name;
static jfieldID dirent_field_name_len;

void Java_linux_Dirent_init(JNIEnv *env, jclass class) {

    init_byte_field(env, class, "DT_UNKNOWN", DT_UNKNOWN);
    init_byte_field(env, class, "DT_FIFO", DT_FIFO);
    init_byte_field(env, class, "DT_CHR", DT_CHR);
    init_byte_field(env, class, "DT_DIR", DT_DIR);
    init_byte_field(env, class, "DT_BLK", DT_BLK);
    init_byte_field(env, class, "DT_REG", DT_REG);
    init_byte_field(env, class, "DT_LNK", DT_LNK);
    init_byte_field(env, class, "DT_SOCK", DT_SOCK);
    init_byte_field(env, class, "DT_WHT", DT_WHT);

    dirent_field_ino = (*env)->GetFieldID(env, class, "d_ino", "J");
    dirent_field_type = (*env)->GetFieldID(env, class, "d_type", "B");
    dirent_field_name = (*env)->GetFieldID(env, class, "d_name", "[B");
    dirent_field_name_len = (*env)->GetFieldID(env, class, "d_name_len", "I");

    dir_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "linux/Dirent$DIR"));
    if (NULL != dir_class) {
        dir_constructor = (*env)->GetMethodID(env, dir_class, "<init>", "(J)V");
        dir_field_address = (*env)->GetFieldID(env, dir_class, "address", "J");
        dir_field_closed = (*env)->GetFieldID(env, dir_class, "closed", "Z");
    }
}

jobject Java_linux_Dirent_opendir(JNIEnv *env, jclass class, jbyteArray jpath) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return NULL;
    }

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    DIR *dir = opendir(path);
    if (NULL == dir) {
        throw_errno_exception(env);
        return NULL;
    }

    return (*env)->NewObject(env, dir_class, dir_constructor, (jlong) (intptr_t) dir);

}

DIR *get_dir(JNIEnv *env, jobject dir) {

    if (NULL == dir) {
        throw_null_pointer_exception(env, "Dir is null");
        return NULL;
    }

    jboolean closed = (*env)->GetBooleanField(env, dir, dir_field_closed);

    if (JNI_TRUE == closed) {
        throw_illegal_state_exception(env, "DIR is closed.");
        return NULL;
    }

    return (DIR *) (intptr_t) (*env)->GetLongField(env, dir, dir_field_address);
}

void Java_linux_Dirent_closedir(JNIEnv *env, jclass class, jobject dir) {

    if (NULL == dir) {
        throw_null_pointer_exception(env, "Dir is null");
        return;
    }

    DIR *dirp = get_dir(env, dir);
    if (NULL != dirp) {
        closedir(dirp);
        (*env)->SetBooleanField(env, dir, dir_field_closed, JNI_TRUE);
    }

}

jobject Java_linux_Dirent_readdir(JNIEnv *env, jclass class, jobject jdir, jobject jentry) {

    if (NULL == jdir) {
        throw_null_pointer_exception(env, "Dir is null");
        return NULL;
    }

    if (NULL == jentry) {
        throw_null_pointer_exception(env, "Entry is null");
        return NULL;
    }

    DIR *dirp = get_dir(env, jdir);
    if (NULL == dirp) {
        return NULL;
    }

    struct dirent entry;
    struct dirent *result;

    if (0 != readdir_r(dirp, &entry, &result)) {
        throw_errno_exception(env);
        return NULL;
    }

    if (NULL == result) {
        return NULL;
    }

    jbyteArray name = (*env)->GetObjectField(env, jentry, dirent_field_name);
    jint name_len = (jint) strlen(entry.d_name);
    (*env)->SetIntField(env, jentry, dirent_field_name_len, (jint) name_len);
    (*env)->SetByteArrayRegion(env, name, 0, name_len, (const jbyte *) entry.d_name);
    (*env)->SetLongField(env, jentry, dirent_field_ino, (jlong) entry.d_ino);
    (*env)->SetByteField(env, jentry, dirent_field_type, (jbyte) entry.d_type);

    return jentry;

}
