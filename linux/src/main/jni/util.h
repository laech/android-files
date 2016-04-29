#include <jni.h>

void throw_errno_exception(JNIEnv *env);

void throw_illegal_state_exception(JNIEnv *env, const char *message);

void throw_null_pointer_exception(JNIEnv *env, const char *message);
