#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#define TEMP_RETRY(exp) ({                                      \
    __typeof__(exp) _rc;                                        \
    do {                                                        \
        _rc = (exp);                                            \
    } while (_rc == -1 && (errno == EINTR || errno == EAGAIN)); \
    _rc; })

void throw_errno_exception(JNIEnv *env);
