package l.files.fs;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nullable;

import linux.ErrnoException;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static linux.Errno.EAGAIN;

public class Stat extends Native implements Parcelable {

    static int placeholder() {
        return -1;
    }

    private static final int S_IFMT = placeholder();
    private static final int S_IFSOCK = placeholder();
    private static final int S_IFLNK = placeholder();
    private static final int S_IFREG = placeholder();
    private static final int S_IFBLK = placeholder();
    private static final int S_IFDIR = placeholder();
    private static final int S_IFCHR = placeholder();
    private static final int S_IFIFO = placeholder();
    private static final int S_ISUID = placeholder();
    private static final int S_ISGID = placeholder();
    private static final int S_ISVTX = placeholder();

    private static boolean S_ISLNK(int m) { return (((m) & S_IFMT) == S_IFLNK); }
    private static boolean S_ISREG(int m) { return (((m) & S_IFMT) == S_IFREG); }
    private static boolean S_ISDIR(int m) { return (((m) & S_IFMT) == S_IFDIR); }
    private static boolean S_ISCHR(int m) { return (((m) & S_IFMT) == S_IFCHR); }
    private static boolean S_ISBLK(int m) { return (((m) & S_IFMT) == S_IFBLK); }
    private static boolean S_ISFIFO(int m) { return (((m) & S_IFMT) == S_IFIFO); }
    private static boolean S_ISSOCK(int m) { return (((m) & S_IFMT) == S_IFSOCK); }

    static final int S_IRWXU = placeholder();
    static final int S_IRUSR = placeholder();
    static final int S_IWUSR = placeholder();
    static final int S_IXUSR = placeholder();

    static final int S_IRWXG = placeholder();
    static final int S_IRGRP = placeholder();
    static final int S_IWGRP = placeholder();
    static final int S_IXGRP = placeholder();

    static final int S_IRWXO = placeholder();
    static final int S_IROTH = placeholder();
    static final int S_IWOTH = placeholder();
    static final int S_IXOTH = placeholder();

    static {
        init();
    }

    private static native void init();

    static native Stat stat(byte[] path) throws ErrnoException;

    static native Stat lstat(byte[] path) throws ErrnoException;

    static native Stat fstat(int fd) throws ErrnoException;

    static native void chmod(byte[] path, int mode) throws ErrnoException;

    static native void mkdir(byte[] path, int mode) throws ErrnoException;

    private final int mode;
    private final long size;
    private final long mtime;
    private final int mtime_nsec;
    private final long blocks;

    private Stat(
            int mode,
            long size,
            long mtime,
            int mtime_nsec,
            long blocks) {

        this.mode = mode;
        this.size = size;
        this.mtime = mtime;
        this.mtime_nsec = mtime_nsec;
        this.blocks = blocks;
    }

    static Stat stat(Path path, LinkOption option) throws IOException {
        requireNonNull(option, "option");

        while (true) {
            try {

                if (option == FOLLOW) {
                    return stat(path.toByteArray());
                } else {
                    return lstat(path.toByteArray());
                }

            } catch (final ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw ErrnoExceptions.toIOException(e, path);
                }
            }
        }
    }

    int mode() {
        return mode;
    }

    public Instant lastModifiedTime() {
        return Instant.of(mtime, mtime_nsec);
    }

    public long lastModifiedEpochSecond() {
        return mtime;
    }

    public int lastModifiedNanoOfSecond() {
        return mtime_nsec;
    }

    public long size() {
        return this.size;
    }

    public long sizeOnDisk() {
        return blocks * 512;
    }

    public boolean isSymbolicLink() {
        return S_ISLNK(mode);
    }

    public boolean isRegularFile() {
        return S_ISREG(mode);
    }

    public boolean isDirectory() {
        return S_ISDIR(mode);
    }

    public boolean isFifo() {
        return S_ISFIFO(mode);
    }

    public boolean isSocket() {
        return S_ISSOCK(mode);
    }

    public boolean isBlockDevice() {
        return S_ISBLK(mode);
    }

    public boolean isCharacterDevice() {
        return S_ISCHR(mode);
    }

    public Set<Permission> permissions() {
        return Permission.fromStatMode(mode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "mode=" + mode +
                ", size=" + size +
                ", mtime=" + mtime +
                ", mtime_nsec=" + mtime_nsec +
                ", blocks=" + blocks +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Stat that = (Stat) o;

        return mode == that.mode &&
                size == that.size &&
                mtime == that.mtime &&
                mtime_nsec == that.mtime_nsec &&
                blocks == that.blocks;
    }

    @Override
    public int hashCode() {
        int result = mode;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (mtime ^ (mtime >>> 32));
        result = 31 * result + mtime_nsec;
        result = 31 * result + (int) (blocks ^ (blocks >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeLong(size);
        dest.writeLong(mtime);
        dest.writeInt(mtime_nsec);
        dest.writeLong(blocks);
    }

    public static final Creator<Stat> CREATOR = new Creator<Stat>() {

        @Override
        public Stat createFromParcel(Parcel source) {
            int mode = source.readInt();
            long size = source.readLong();
            long mtime = source.readLong();
            int mtimeNs = source.readInt();
            long blocks = source.readLong();
            return new Stat(mode, size, mtime, mtimeNs, blocks);
        }

        @Override
        public Stat[] newArray(int size) {
            return new Stat[size];
        }
    };
}
