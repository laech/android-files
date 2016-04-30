#include <jni.h>

void throw_errno_exception(JNIEnv *env);

void throw_illegal_state_exception(JNIEnv *env, const char *message);

void throw_null_pointer_exception(JNIEnv *env, const char *message);

void init_long_field(JNIEnv *env, jclass class, const char *name, jlong value);

void init_int_field(JNIEnv *env, jclass class, const char *name, jint value);

void init_byte_field(JNIEnv *env, jclass class, const char *name, jbyte value);
