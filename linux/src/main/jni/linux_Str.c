#include <jni.h>
#include <string.h>

jstring Java_linux_Str_strerror(JNIEnv *env, jclass clazz, jint errnum) {

    size_t size = 1024;
    char buf[size];

    strerror_r(errnum, buf, size);

    return (*env)->NewStringUTF(env, buf);
}
