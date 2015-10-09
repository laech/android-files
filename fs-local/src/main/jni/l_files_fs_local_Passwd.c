#include <jni.h>
#include <errno.h>
#include "util.h"
#include <pwd.h>

static jmethodID ctor;

void Java_l_files_fs_local_Passwd_init(JNIEnv *env, jclass clazz) {
  ctor = (*env)->GetStaticMethodID(env, clazz, "create",
      "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;)Ll/files/fs/local/Passwd;");
}

jobject Java_l_files_fs_local_Passwd_getpwuid(JNIEnv* env, jclass clazz, jint juid) {
    errno = 0;
    struct passwd *pw = getpwuid(juid);
    if (NULL == pw) {
      throw_errno_exception(env);
      return NULL;
    }
    jstring jname = (*env)->NewStringUTF(env, pw->pw_name);
    jstring jdir = (*env)->NewStringUTF(env, pw->pw_dir);
    jstring jshell = (*env)->NewStringUTF(env, pw->pw_shell);
    return (*env)->CallStaticObjectMethod(env, clazz, ctor,
        jname,
        (jint) pw->pw_uid,
        (jint) pw->pw_gid,
        jdir,
        jshell);
}
