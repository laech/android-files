#include <jni.h>
#include <sys/vfs.h>
#include "util.h"

static jfieldID statfs_f_type;
static jfieldID statfs_f_bsize;
static jfieldID statfs_f_blocks;
static jfieldID statfs_f_bfree;
static jfieldID statfs_f_bavail;
static jfieldID statfs_f_files;
static jfieldID statfs_f_ffree;
static jfieldID statfs_f_namelen;
static jfieldID statfs_f_frsize;
static jfieldID statfs_f_flags;

void Java_linux_Vfs_init(JNIEnv *env, jclass class) {
    init_long_field(env, class, "ADFS_SUPER_MAGIC", ADFS_SUPER_MAGIC);
    init_long_field(env, class, "AFFS_SUPER_MAGIC", AFFS_SUPER_MAGIC);
    init_long_field(env, class, "BEFS_SUPER_MAGIC", BEFS_SUPER_MAGIC);
    init_long_field(env, class, "BFS_MAGIC", BFS_MAGIC);
    init_long_field(env, class, "CIFS_MAGIC_NUMBER", CIFS_MAGIC_NUMBER);
    init_long_field(env, class, "CODA_SUPER_MAGIC", CODA_SUPER_MAGIC);
    init_long_field(env, class, "COH_SUPER_MAGIC", COH_SUPER_MAGIC);
    init_long_field(env, class, "CRAMFS_MAGIC", CRAMFS_MAGIC);
    init_long_field(env, class, "DEVFS_SUPER_MAGIC", DEVFS_SUPER_MAGIC);
    init_long_field(env, class, "EFS_SUPER_MAGIC", EFS_SUPER_MAGIC);
    init_long_field(env, class, "EXT_SUPER_MAGIC", EXT_SUPER_MAGIC);
    init_long_field(env, class, "EXT2_OLD_SUPER_MAGIC", EXT2_OLD_SUPER_MAGIC);
    init_long_field(env, class, "EXT2_SUPER_MAGIC", EXT2_SUPER_MAGIC);
    init_long_field(env, class, "EXT3_SUPER_MAGIC", EXT3_SUPER_MAGIC);
    init_long_field(env, class, "HFS_SUPER_MAGIC", HFS_SUPER_MAGIC);
    init_long_field(env, class, "HPFS_SUPER_MAGIC", HPFS_SUPER_MAGIC);
    init_long_field(env, class, "HUGETLBFS_MAGIC", HUGETLBFS_MAGIC);
    init_long_field(env, class, "ISOFS_SUPER_MAGIC", ISOFS_SUPER_MAGIC);
    init_long_field(env, class, "JFFS2_SUPER_MAGIC", JFFS2_SUPER_MAGIC);
    init_long_field(env, class, "JFS_SUPER_MAGIC", JFS_SUPER_MAGIC);
    init_long_field(env, class, "MINIX_SUPER_MAGIC", MINIX_SUPER_MAGIC);
    init_long_field(env, class, "MINIX_SUPER_MAGIC2", MINIX_SUPER_MAGIC2);
    init_long_field(env, class, "MINIX2_SUPER_MAGIC", MINIX2_SUPER_MAGIC);
    init_long_field(env, class, "MINIX2_SUPER_MAGIC2", MINIX2_SUPER_MAGIC2);
    init_long_field(env, class, "MSDOS_SUPER_MAGIC", MSDOS_SUPER_MAGIC);
    init_long_field(env, class, "NCP_SUPER_MAGIC", NCP_SUPER_MAGIC);
    init_long_field(env, class, "NFS_SUPER_MAGIC", NFS_SUPER_MAGIC);
    init_long_field(env, class, "NTFS_SB_MAGIC", NTFS_SB_MAGIC);
    init_long_field(env, class, "OPENPROM_SUPER_MAGIC", OPENPROM_SUPER_MAGIC);
    init_long_field(env, class, "PROC_SUPER_MAGIC", PROC_SUPER_MAGIC);
    init_long_field(env, class, "QNX4_SUPER_MAGIC", QNX4_SUPER_MAGIC);
    init_long_field(env, class, "REISERFS_SUPER_MAGIC", REISERFS_SUPER_MAGIC);
    init_long_field(env, class, "ROMFS_MAGIC", ROMFS_MAGIC);
    init_long_field(env, class, "SMB_SUPER_MAGIC", SMB_SUPER_MAGIC);
    init_long_field(env, class, "SYSV2_SUPER_MAGIC", SYSV2_SUPER_MAGIC);
    init_long_field(env, class, "SYSV4_SUPER_MAGIC", SYSV4_SUPER_MAGIC);
    init_long_field(env, class, "TMPFS_MAGIC", TMPFS_MAGIC);
    init_long_field(env, class, "UDF_SUPER_MAGIC", UDF_SUPER_MAGIC);
    init_long_field(env, class, "UFS_MAGIC", UFS_MAGIC);
    init_long_field(env, class, "USBDEVICE_SUPER_MAGIC", USBDEVICE_SUPER_MAGIC);
    init_long_field(env, class, "VXFS_SUPER_MAGIC", VXFS_SUPER_MAGIC);
    init_long_field(env, class, "XENIX_SUPER_MAGIC", XENIX_SUPER_MAGIC);
    init_long_field(env, class, "XFS_SUPER_MAGIC", XFS_SUPER_MAGIC);

    jclass statfs_class = (*env)->FindClass(env, "linux/Vfs$Statfs");
    if (NULL != statfs_class) {
        statfs_f_type = (*env)->GetFieldID(env, statfs_class, "f_type", "J");
        statfs_f_bsize = (*env)->GetFieldID(env, statfs_class, "f_bsize", "J");
        statfs_f_blocks = (*env)->GetFieldID(env, statfs_class, "f_blocks", "J");
        statfs_f_bfree = (*env)->GetFieldID(env, statfs_class, "f_bfree", "J");
        statfs_f_bavail = (*env)->GetFieldID(env, statfs_class, "f_bavail", "J");
        statfs_f_files = (*env)->GetFieldID(env, statfs_class, "f_files", "J");
        statfs_f_ffree = (*env)->GetFieldID(env, statfs_class, "f_ffree", "J");
        statfs_f_namelen = (*env)->GetFieldID(env, statfs_class, "f_namelen", "J");
        statfs_f_frsize = (*env)->GetFieldID(env, statfs_class, "f_frsize", "J");
        statfs_f_flags = (*env)->GetFieldID(env, statfs_class, "f_flags", "J");
    }
}

void Java_linux_Vfs_statfs(JNIEnv *env, jclass class, jbyteArray jpath, jobject jstatfs) {

    if (NULL == jpath) {
        throw_null_pointer_exception(env, "Path is null");
        return;
    }

    if (NULL == jstatfs) {
        throw_null_pointer_exception(env, "Statfs is null");
        return;
    }

    JBYTE_ARRAY_TO_CHARS(env, path, jpath)

    struct statfs sb;
    int result = statfs(path, &sb);
    if (-1 == result) {
        throw_errno_exception(env);
        return;
    }

    (*env)->SetLongField(env, jstatfs, statfs_f_type, (jlong) sb.f_type);
    (*env)->SetLongField(env, jstatfs, statfs_f_bsize, (jlong) sb.f_bsize);
    (*env)->SetLongField(env, jstatfs, statfs_f_blocks, (jlong) sb.f_blocks);
    (*env)->SetLongField(env, jstatfs, statfs_f_bfree, (jlong) sb.f_bfree);
    (*env)->SetLongField(env, jstatfs, statfs_f_bavail, (jlong) sb.f_bavail);
    (*env)->SetLongField(env, jstatfs, statfs_f_files, (jlong) sb.f_files);
    (*env)->SetLongField(env, jstatfs, statfs_f_ffree, (jlong) sb.f_ffree);
    (*env)->SetLongField(env, jstatfs, statfs_f_namelen, (jlong) sb.f_namelen);
    (*env)->SetLongField(env, jstatfs, statfs_f_frsize, (jlong) sb.f_frsize);
    (*env)->SetLongField(env, jstatfs, statfs_f_flags, (jlong) sb.f_flags);

}
