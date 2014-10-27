package l.files.fs.local;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.net.MediaType;

import l.files.fs.FileId;
import l.files.fs.FileStatus;
import l.files.fs.FileTypeDetector;
import l.files.fs.LinkOption;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;

abstract class LocalFileTypeDetector implements FileTypeDetector {

  // Media types for file types, kept consistent with the linux "file" command
  private static final MediaType INODE_DIRECTORY = MediaType.parse("inode/directory");
  private static final MediaType INODE_BLOCKDEVICE = MediaType.parse("inode/blockdevice");
  private static final MediaType INODE_CHARDEVICE = MediaType.parse("inode/chardevice");
  private static final MediaType INODE_FIFO = MediaType.parse("inode/fifo");
  private static final MediaType INODE_SYMLINK = MediaType.parse("inode/symlink");
  private static final MediaType INODE_SOCKET = MediaType.parse("inode/socket");

  private static final LoadingCache<String, MediaType> cache =
      CacheBuilder
          .newBuilder()
          .build(new CacheLoader<String, MediaType>() {
            @Override public MediaType load(String key) {
              try {
                return MediaType.parse(key);
              } catch (IllegalArgumentException e) {
                return OCTET_STREAM;
              }
            }
          });

  /**
   * Returns a loading cache that maps string media types to their parsed form.
   */
  static LoadingCache<String, MediaType> cache() {
    return cache;
  }

  private final LocalFileSystem fs;

  LocalFileTypeDetector(LocalFileSystem fs) {
    this.fs = checkNotNull(fs, "fs");
  }

  @Override public MediaType detect(FileId file, LinkOption option) {
    LocalFileStatus stat = fs.stat(file, option);
    if (stat.isRegularFile()) {
      return detectRegularFile(stat);
    }
    if (stat.isDirectory()) {
      return INODE_DIRECTORY;
    }
    if (stat.isSymbolicLink()) {
      return INODE_SYMLINK;
    }
    if (stat.isBlockDevice()) {
      return INODE_BLOCKDEVICE;
    }
    if (stat.isCharacterDevice()) {
      return INODE_CHARDEVICE;
    }
    if (stat.isFifo()) {
      return INODE_FIFO;
    }
    if (stat.isSocket()) {
      return INODE_SOCKET;
    }
    return OCTET_STREAM;
  }

  /**
   * Called when the given file is a regular file
   * ({@link FileStatus#isRegularFile()}).
   */
  protected abstract MediaType detectRegularFile(FileStatus stat);

}
