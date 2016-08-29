#include <jni.h>
#include "include/fpdfview.h"
#include <fcntl.h>
#include <android/bitmap.h>

void throw_io_exception(JNIEnv *env, const char *msg) {
    jclass clazz = (*env)->FindClass(env, "java/io/IOException");
    if (NULL == clazz) {
        return;
    }

    jmethodID constructor = (*env)->GetMethodID(env, clazz, "<init>", "(Ljava/lang/String;)V");
    if (NULL == constructor) {
        return;
    }

    jstring jmsg = (*env)->NewStringUTF(env, msg);
    if (NULL == jmsg) {
        return;
    }

    jobject exception = (*env)->NewObject(env, clazz, constructor, jmsg);
    if (NULL == exception) {
        return;
    }

    (*env)->Throw(env, exception);
}

void Java_l_files_thumbnail_Pdf_init(JNIEnv *env, jclass clazz) {
    FPDF_InitLibrary();
}

jdouble Java_l_files_thumbnail_Pdf_getPageHeightInPoints(
        JNIEnv *env, jclass clazz, jlong jpage) {

    return FPDF_GetPageHeight((FPDF_PAGE) (intptr_t) jpage);
}

jdouble Java_l_files_thumbnail_Pdf_getPageWidthInPoints(
        JNIEnv *env, jclass clazz, jlong jpage) {

    return FPDF_GetPageWidth((FPDF_PAGE) (intptr_t) jpage);
}

void Java_l_files_thumbnail_Pdf_close(
        JNIEnv *env, jclass clazz, jlong jdoc) {

    FPDF_CloseDocument((FPDF_DOCUMENT) (intptr_t) jdoc);
}

void Java_l_files_thumbnail_Pdf_closePage(
        JNIEnv *env, jclass clazz, jlong jpage) {

    FPDF_ClosePage((FPDF_PAGE) (intptr_t) jpage);
}

jlong Java_l_files_thumbnail_Pdf_open(
        JNIEnv *env, jclass clazz, jbyteArray jpath) {

    jsize len = (*env)->GetArrayLength(env, jpath);
    char path[len + 1];
    (*env)->GetByteArrayRegion(env, jpath, 0, len, path);
    path[len] = '\0';

    FPDF_DOCUMENT document = FPDF_LoadDocument(path, NULL);
    if (NULL == document) {

        switch (FPDF_GetLastError()) {
            case FPDF_ERR_UNKNOWN:
                throw_io_exception(env, "FPDF_ERR_UNKNOWN");
                break;
            case FPDF_ERR_FILE:
                throw_io_exception(env, "FPDF_ERR_FILE");
                break;
            case FPDF_ERR_FORMAT:
                throw_io_exception(env, "FPDF_ERR_FORMAT");
                break;
            case FPDF_ERR_PASSWORD:
                throw_io_exception(env, "FPDF_ERR_PASSWORD");
                break;
            case FPDF_ERR_SECURITY:
                throw_io_exception(env, "FPDF_ERR_SECURITY");
                break;
            case FPDF_ERR_PAGE:
                throw_io_exception(env, "FPDF_ERR_PAGE");
                break;
        }

        return -1;
    }

    return (jlong) (intptr_t) document;

}

jlong Java_l_files_thumbnail_Pdf_openPage(
        JNIEnv *env, jclass clazz, jlong jdoc, jint i) {

    FPDF_PAGE page = FPDF_LoadPage((FPDF_DOCUMENT) (intptr_t) jdoc, i);
    if (NULL == page) {
        throw_io_exception(env, "FPDF_LoadPage");
        return -1;
    }
    return (jlong) (intptr_t) page;
}

void Java_l_files_thumbnail_Pdf_render(
        JNIEnv *env, jclass clazz, jlong jpage, jobject jbitmap) {

    FPDF_PAGE page = (FPDF_PAGE) (intptr_t) jpage;

    AndroidBitmapInfo info;
    void *pixels;

    if (0 != AndroidBitmap_getInfo(env, jbitmap, &info)) {
        throw_io_exception(env, "AndroidBitmap_getInfo");
        return;
    }

    if (ANDROID_BITMAP_FORMAT_RGBA_8888 != info.format) {
        throw_io_exception(env, "not ANDROID_BITMAP_FORMAT_RGBA_8888");
        return;
    }

    if (0 != AndroidBitmap_lockPixels(env, jbitmap, &pixels)) {
        throw_io_exception(env, "AndroidBitmap_lockPixels");
        return;
    }

    FPDF_BITMAP bitmap = FPDFBitmap_CreateEx(
            info.width, info.height, FPDFBitmap_BGRA, pixels, info.stride);

    if (NULL == bitmap) {
        throw_io_exception(env, "FPDFBitmap_CreateEx");
        return;
    }

    FPDF_RenderPageBitmap(bitmap, page, 0, 0, info.width, info.height, 0, FPDF_REVERSE_BYTE_ORDER);

    if (0 != AndroidBitmap_unlockPixels(env, jbitmap)) {
        throw_io_exception(env, "AndroidBitmap_unlockPixels");
    }

}
