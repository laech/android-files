#include <jni.h>
#include <linux/limits.h>
#include "util.h"

void Java_linux_Limits_init(JNIEnv *env, jclass class) {
    init_int_field(env, class, "NAME_MAX", NAME_MAX);
}
