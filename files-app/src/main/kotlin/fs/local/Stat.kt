/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package l.files.fs.local

import kotlin.platform.platformStatic

/**
 * http://www.opengroup.org/onlinepubs/000095399/basedefs/sys/stat.h.html
 */
data class Stat(
        val dev: Long,
        val ino: Long,
        val mode: Int,
        val nlink: Long,
        val uid: Int,
        val gid: Int,
        val rdev: Long,
        val size: Long,
        val atime: Long,
        val mtime: Long,
        val ctime: Long,
        val blksize: Long,

        val blocks: Long) : Native() {

    class object {

        /* See /usr/include/linux/stat.h for meaning of these constants. */

        val S_IFMT = 61440
        val S_IFSOCK = 49152
        val S_IFLNK = 40960
        val S_IFREG = 32768
        val S_IFBLK = 24576
        val S_IFDIR = 16384
        val S_IFCHR = 8192
        val S_IFIFO = 4096
        val S_ISUID = 2048
        val S_ISGID = 1024
        val S_ISVTX = 512

        fun S_ISLNK(m: Int) = (m and S_IFMT) == S_IFLNK
        fun S_ISREG(m: Int) = (m and S_IFMT) == S_IFREG
        fun S_ISDIR(m: Int) = (m and S_IFMT) == S_IFDIR
        fun S_ISCHR(m: Int) = (m and S_IFMT) == S_IFCHR
        fun S_ISBLK(m: Int) = (m and S_IFMT) == S_IFBLK
        fun S_ISFIFO(m: Int) = (m and S_IFMT) == S_IFIFO
        fun S_ISSOCK(m: Int) = (m and S_IFMT) == S_IFSOCK

        val S_IRWXU = 448
        val S_IRUSR = 256
        val S_IWUSR = 128
        val S_IXUSR = 64

        val S_IRWXG = 56
        val S_IRGRP = 32
        val S_IWGRP = 16
        val S_IXGRP = 8

        val S_IRWXO = 7
        val S_IROTH = 4
        val S_IWOTH = 2
        val S_IXOTH = 1

        {
            init()
        }

        platformStatic native fun init()

        /**
         * http://pubs.opengroup.org/onlinepubs/000095399/functions/stat.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun stat(path: String): Stat

        /**
         * http://pubs.opengroup.org/onlinepubs/000095399/functions/lstat.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun lstat(path: String): Stat
    }

}
