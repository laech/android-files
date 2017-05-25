cmake_minimum_required(VERSION 3.4.1)
add_library(fs
            SHARED
            src/main/jni/l_files_fs_Observable.c
            src/main/jni/l_files_fs_Path.c
            src/main/jni/l_files_fs_Stat.c
            src/main/jni/linux_Dirent.c
            src/main/jni/linux_Errno.c
            src/main/jni/linux_Fcntl.c
            src/main/jni/linux_Inotify.c
            src/main/jni/linux_Limits.c
            src/main/jni/linux_Stdio.c
            src/main/jni/linux_Str.c
            src/main/jni/linux_Unistd.c
            src/main/jni/linux_Vfs.c
            src/main/jni/util.c)
