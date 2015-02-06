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

package l.files.fs.local;

import com.google.auto.value.AutoValue;

/**
 * @see <a href="http://www.opengroup.org/onlinepubs/000095399/basedefs/sys/stat.h.html">stat.h</a>
 */
@AutoValue
@SuppressWarnings("OctalInteger")
public abstract class Stat extends Native {

  /* See /usr/include/linux/stat.h for meaning of these constants. */

  public static final int S_IFMT = 00170000;
  public static final int S_IFSOCK = 0140000;
  public static final int S_IFLNK = 0120000;
  public static final int S_IFREG = 0100000;
  public static final int S_IFBLK = 0060000;
  public static final int S_IFDIR = 0040000;
  public static final int S_IFCHR = 0020000;
  public static final int S_IFIFO = 0010000;
  public static final int S_ISUID = 0004000;
  public static final int S_ISGID = 0002000;
  public static final int S_ISVTX = 0001000;

  public static boolean S_ISLNK(int m) {return (m & S_IFMT) == S_IFLNK;}
  public static boolean S_ISREG(int m) {return (m & S_IFMT) == S_IFREG;}
  public static boolean S_ISDIR(int m) {return (m & S_IFMT) == S_IFDIR;}
  public static boolean S_ISCHR(int m) {return (m & S_IFMT) == S_IFCHR;}
  public static boolean S_ISBLK(int m) {return (m & S_IFMT) == S_IFBLK;}
  public static boolean S_ISFIFO(int m) {return (m & S_IFMT) == S_IFIFO;}
  public static boolean S_ISSOCK(int m) {return (m & S_IFMT) == S_IFSOCK;}

  public static final int S_IRWXU = 00700;
  public static final int S_IRUSR = 00400;
  public static final int S_IWUSR = 00200;
  public static final int S_IXUSR = 00100;

  public static final int S_IRWXG = 00070;
  public static final int S_IRGRP = 00040;
  public static final int S_IWGRP = 00020;
  public static final int S_IXGRP = 00010;

  public static final int S_IRWXO = 00007;
  public static final int S_IROTH = 00004;
  public static final int S_IWOTH = 00002;
  public static final int S_IXOTH = 00001;

  /**
   * Device ID of device containing file.
   */
  public abstract long dev(); /*dev_t*/

  /**
   * File serial number (inode).
   */
  public abstract long ino(); /*ino_t*/

  /**
   * Mode (permissions) of file.
   */
  public abstract int mode(); /*mode_t*/

  /**
   * Number of hard links to the file.
   */
  public abstract long nlink(); /*nlink_t*/

  /**
   * User ID of file.
   */
  public abstract int uid(); /*uid_t*/

  /**
   * Group ID of file.
   */
  public abstract int gid(); /*gid_t*/

  /**
   * Device ID (if file is character or block special).
   */
  public abstract long rdev(); /*dev_t*/

  /**
   * For regular files, the file size in bytes.
   * <p/>
   * For symbolic links, the length in bytes of the pathname contained in the
   * symbolic link.
   * <p/>
   * For a shared memory object, the length in bytes. <p/>For a typed memory
   * object, the length in bytes.
   * <p/>
   * For other file types, the use of this field is unspecified.
   */
  public abstract long size(); /*off_t*/

  /**
   * Time of last access in seconds.
   */
  public abstract long atime(); /*time_t*/

  /**
   * Time of last data modification in seconds.
   */
  public abstract long mtime(); /*time_t*/

  /**
   * Time of last status change in seconds.
   */
  public abstract long ctime(); /*time_t*/

  /**
   * A file system-specific preferred I/O block size for this object. For some
   * file system types, this may vary from file to file.
   */
  public abstract long blksize(); /*blksize_t*/

  /**
   * Number of blocks allocated for this object.
   */
  public abstract long blocks(); /*blkcnt_t*/

  Stat() {}

  public static Stat create(
      long dev,
      long ino,
      int mode,
      long nlink,
      int uid,
      int gid,
      long rdev,
      long size,
      long atime,
      long mtime,
      long ctime,
      long blksize,
      long blocks) {
    return new AutoValue_Stat(
        dev,
        ino,
        mode,
        nlink,
        uid,
        gid,
        rdev,
        size,
        atime,
        mtime,
        ctime,
        blksize,
        blocks);
  }

  static {
    init();
  }

  public static native void init();

  /**
   * @see <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/stat.html">stat()</a>
   */
  public static native Stat stat(String path) throws ErrnoException;

  /**
   * @see <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/lstat.html">lstat()</a>
   */
  public static native Stat lstat(String path) throws ErrnoException;

}
