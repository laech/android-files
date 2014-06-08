package l.files.io.file;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.IOException;

import l.files.io.os.ErrnoException;
import l.files.io.os.Stat;

import static l.files.io.os.Stat.S_ISBLK;
import static l.files.io.os.Stat.S_ISCHR;
import static l.files.io.os.Stat.S_ISDIR;
import static l.files.io.os.Stat.S_ISFIFO;
import static l.files.io.os.Stat.S_ISLNK;
import static l.files.io.os.Stat.S_ISREG;
import static l.files.io.os.Stat.S_ISSOCK;
import static org.apache.commons.io.FilenameUtils.concat;

/**
 * Information regarding a file at a point in time.
 * <h3>
 * Difference between this class and {@link java.io.File}
 * </h3>
 * {@link File} represents a handle to the underlying file,
 * methods such as {@link File#lastModified()} will always return the
 * latest value by querying the underlying file.
 * <p/>
 * {@link FileInfo} represents a snapshot of the file information at
 * the time an instance is created, therefore if the underlying file changes
 * after an instance of this class is created, the change won't be reflected
 * by the existing instance.
 */
@AutoValue
public abstract class FileInfo {
    FileInfo() {
    }

  public abstract String getPath();

  abstract Stat getStat();

    /**
     * @throws IOException includes path is not accessible or doesn't exist
     */
    public static FileInfo get(String parent, String child) throws IOException {
        String path = concat(parent, child);
        return get(path);
    }

    /**
     * @throws IOException includes path is not accessible or doesn't exist
     */
    public static FileInfo get(String path) throws IOException {
        try {
            Stat stat = Stat.lstat(path);
            return new AutoValue_FileInfo(path, stat);
        } catch (ErrnoException e) {
            throw new IOException("Failed to get file info: " + path, e);
        }
    }

    /**
     * Gets the ID of the device containing this file.
     */
    public long getDeviceId() {
        return getStat().dev;
    }

    /**
     * File serial number (inode) of this file.
     */
    public long getInodeNumber() {
        return getStat().ino;
    }

    /**
     * Size of this file in bytes.
     */
    public long getSize() {
        return getStat().size;
    }

    /**
     * Last modified time of this file in milliseconds.
     */
    public long getLastModified() {
        return getStat().mtime * 1000L;
    }

    public boolean isSocket() {
        return S_ISSOCK(getStat().mode);
    }

    public boolean isSymbolicLink() {
        return S_ISLNK(getStat().mode);
    }

    public boolean isRegularFile() {
        return S_ISREG(getStat().mode);
    }

    public boolean isBlockDevice() {
        return S_ISBLK(getStat().mode);
    }

    public boolean isDirectory() {
        return S_ISDIR(getStat().mode);
    }

    public boolean isCharacterDevice() {
        return S_ISCHR(getStat().mode);
    }

    public boolean isFifo() {
        return S_ISFIFO(getStat().mode);
    }

    public File toFile() {
        return new File(getPath());
    }
}
