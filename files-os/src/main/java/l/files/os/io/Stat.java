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

package l.files.os.io;

/**
 * File information returned by fstat(2), lstat(2), and stat(2). Corresponds to
 * C's {@code struct stat} from <a href="http://www.opengroup.org/onlinepubs/000095399/basedefs/sys/stat.h.html">&lt;stat.h&gt;</a>
 */
public final class Stat {

  /**
   * Device ID of device containing file.
   */
  public final long dev; /*dev_t*/

  /**
   * File serial number (inode).
   */
  public final long ino; /*ino_t*/

  /**
   * Mode (permissions) of file.
   */
  public final int mode; /*mode_t*/

  /**
   * Number of hard links to the file.
   */
  public final long nlink; /*nlink_t*/

  /**
   * User ID of file.
   */
  public final int uid; /*uid_t*/

  /**
   * Group ID of file.
   */
  public final int gid; /*gid_t*/

  /**
   * Device ID (if file is character or block special).
   */
  public final long rdev; /*dev_t*/

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
  public final long size; /*off_t*/

  /**
   * Time of last access in seconds.
   */
  public final long atime; /*time_t*/

  /**
   * Time of last data modification in seconds.
   */
  public final long mtime; /*time_t*/

  /**
   * Time of last status change in seconds.
   */
  public final long ctime; /*time_t*/

  /**
   * A file system-specific preferred I/O block size for this object. For some
   * file system types, this may vary from file to file.
   */
  public final long blksize; /*blksize_t*/

  /**
   * Number of blocks allocated for this object.
   */
  public final long blocks; /*blkcnt_t*/

  public Stat(
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
    this.dev = dev;
    this.ino = ino;
    this.mode = mode;
    this.nlink = nlink;
    this.uid = uid;
    this.gid = gid;
    this.rdev = rdev;
    this.size = size;
    this.atime = atime;
    this.mtime = mtime;
    this.ctime = ctime;
    this.blksize = blksize;
    this.blocks = blocks;
  }
}
