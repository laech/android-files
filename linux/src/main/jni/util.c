#include <jni.h>
#include <stdio.h>
#include <errno.h>

void throw_errno_exception(JNIEnv *env) {
    jclass clazz = (*env)->FindClass(env, "linux/ErrnoException");
    if (NULL == clazz) {
        return;
    }

    jmethodID constructor = (*env)->GetMethodID(env, clazz, "<init>", "(I)V");
    if (NULL == constructor) {
        return;
    }

    jobject exception = (*env)->NewObject(env, clazz, constructor, errno);
    if (NULL == exception) {
        return;
    }

    (*env)->Throw(env, exception);
}

void throw_illegal_state_exception(JNIEnv *env, const char *message) {
    jclass clazz = (*env)->FindClass(env, "java/lang/IllegalStateException");
    if (NULL == clazz) {
        return;
    }
    (*env)->ThrowNew(env, clazz, message);
}

void throw_null_pointer_exception(JNIEnv *env, const char *message) {
    jclass clazz = (*env)->FindClass(env, "java/lang/NullPointerException");
    if (NULL == clazz) {
        return;
    }
    (*env)->ThrowNew(env, clazz, message);
}

void init_int_field(JNIEnv *env, jclass class, const char *name, jint value) {
    jfieldID fieldId = (*env)->GetStaticFieldID(env, class, name, "I");
    if (NULL != fieldId) {
        (*env)->SetStaticIntField(env, class, fieldId, value);
    }
}

void init_byte_field(JNIEnv *env, jclass class, const char *name, jbyte value) {
    jfieldID fieldId = (*env)->GetStaticFieldID(env, class, name, "B");
    if (NULL != fieldId) {
        (*env)->SetStaticByteField(env, class, fieldId, value);
    }
}
