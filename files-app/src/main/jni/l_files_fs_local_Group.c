#include <jni.h>
#include <errno.h>
#include "util.h"
#include <grp.h>

static jmethodID ctor;

void Java_l_files_fs_local_Group_init(JNIEnv *env, jclass clazz) {
  ctor = (*env)->GetMethodID(env, clazz, "<init>",
      "(Ljava/lang/String;I)V");
}

jobject Java_l_files_fs_local_Group_getgrgid(JNIEnv* env, jclass clazz, jint jgid) {
    errno = 0;
    struct group *gr = getgrgid(jgid);
    if (NULL == gr) {
      throw_errno_exception(env);
      return NULL;
    }
    jstring jname = (*env)->NewStringUTF(env, gr->gr_name);
    return (*env)->NewObject(env, clazz, ctor, jname, (jint) gr->gr_gid);
}
