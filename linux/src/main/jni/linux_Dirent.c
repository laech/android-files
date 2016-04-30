#include <jni.h>
#include <string.h>
#include <dirent.h>
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

    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_UNKNOWN", "B"), DT_UNKNOWN);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_FIFO", "B"), DT_FIFO);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_CHR", "B"), DT_CHR);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_DIR", "B"), DT_DIR);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_BLK", "B"), DT_BLK);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_REG", "B"), DT_REG);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_LNK", "B"), DT_LNK);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_SOCK", "B"), DT_SOCK);
    (*env)->SetStaticByteField(env, class, (*env)->GetStaticFieldID(env, class, "DT_WHT", "B"), DT_WHT);

    dirent_field_ino = (*env)->GetFieldID(env, class, "d_ino", "J");
    dirent_field_type = (*env)->GetFieldID(env, class, "d_type", "B");
    dirent_field_name = (*env)->GetFieldID(env, class, "d_name", "[B");
    dirent_field_name_len = (*env)->GetFieldID(env, class, "d_name_len", "I");

    dir_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "linux/Dirent$DIR"));
    dir_constructor = (*env)->GetMethodID(env, dir_class, "<init>", "(J)V");
    dir_field_address = (*env)->GetFieldID(env, dir_class, "address", "J");
    dir_field_closed = (*env)->GetFieldID(env, dir_class, "closed", "Z");

}

jobject Java_linux_Dirent_fdopendir(JNIEnv *env, jclass class, jint fd) {

    DIR *dir = fdopendir(fd);
    if (NULL == dir) {
        throw_errno_exception(env);
        return NULL;
    }

    return (*env)->NewObject(env, dir_class, dir_constructor, (jlong) (intptr_t) dir);

}

jobject Java_linux_Dirent_opendir(JNIEnv *env, jclass class, jbyteArray jpath) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return NULL;
    }

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, (jbyte *) path);
    path[len] = '\0';

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
    }

    (*env)->SetBooleanField(env, dir, dir_field_closed, JNI_TRUE);

}

jobject  Java_linux_Dirent_readdir(JNIEnv *env, jclass class, jobject jdir, jobject jentry) {

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
    (*env)->SetIntField(env, jentry, dirent_field_name_len, name_len);
    (*env)->SetByteArrayRegion(env, name, 0, name_len, (const jbyte *) entry.d_name);
    (*env)->SetLongField(env, jentry, dirent_field_ino, (jlong) entry.d_ino);
    (*env)->SetByteField(env, jentry, dirent_field_type, entry.d_type);

    return jentry;

}
