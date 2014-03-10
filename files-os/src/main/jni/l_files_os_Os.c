#include <jni.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>

jlong Java_l_files_os_Os_inode(JNIEnv *env, jobject clazz, jstring jpath) {
	const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
	if (NULL == path) {
		return -1;
	}
	struct stat sb;
	int result = TEMP_FAILURE_RETRY(stat(path, &sb));
	(*env)->ReleaseStringUTFChars(env, jpath, path);
  return (-1 == result) ? -1 : sb.st_ino;
}

jboolean Java_l_files_os_Os_symlink(JNIEnv *env, jclass clazz, jstring jpath1, jstring jpath2) {
  const char *src = (*env)->GetStringUTFChars(env, jsrc, NULL);
  const char *dst = (*env)->GetStringUTFChars(env, jdst, NULL);
  int result = TEMP_FAILURE_RETRY(symlink(src, dst));
  (*env)->ReleaseStringUTFChars(env, jsrc, src);
  (*env)->ReleaseStringUTFChars(env, jdst, dst);
  return (-1 == result) ? JNI_FALSE : JNI_TRUE;
}
