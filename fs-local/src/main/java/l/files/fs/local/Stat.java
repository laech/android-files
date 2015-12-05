package l.files.fs.local;

import android.os.Parcel;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.util.Set;

import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.local.ErrnoException.EAGAIN;
import static l.files.fs.local.LocalFileSystem.permissionsFromMode;

@AutoValue
@SuppressWarnings("OctalInteger")
abstract class Stat extends Native implements l.files.fs.Stat {

    static final int S_IFMT = 00170000;
    static final int S_IFSOCK = 0140000;
    static final int S_IFLNK = 0120000;
    static final int S_IFREG = 0100000;
    static final int S_IFBLK = 0060000;
    static final int S_IFDIR = 0040000;
    static final int S_IFCHR = 0020000;
    static final int S_IFIFO = 0010000;
    static final int S_ISUID = 0004000;
    static final int S_ISGID = 0002000;
    static final int S_ISVTX = 0001000;

    static boolean S_ISLNK(int m) {
        return (((m) & S_IFMT) == S_IFLNK);
    }

    static boolean S_ISREG(int m) {
        return (((m) & S_IFMT) == S_IFREG);
    }

    static boolean S_ISDIR(int m) {
        return (((m) & S_IFMT) == S_IFDIR);
    }

    static boolean S_ISCHR(int m) {
        return (((m) & S_IFMT) == S_IFCHR);
    }

    static boolean S_ISBLK(int m) {
        return (((m) & S_IFMT) == S_IFBLK);
    }

    static boolean S_ISFIFO(int m) {
        return (((m) & S_IFMT) == S_IFIFO);
    }

    static boolean S_ISSOCK(int m) {
        return (((m) & S_IFMT) == S_IFSOCK);
    }

    static final int S_IRWXU = 00700;
    static final int S_IRUSR = 00400;
    static final int S_IWUSR = 00200;
    static final int S_IXUSR = 00100;

    static final int S_IRWXG = 00070;
    static final int S_IRGRP = 00040;
    static final int S_IWGRP = 00020;
    static final int S_IXGRP = 00010;

    static final int S_IRWXO = 00007;
    static final int S_IROTH = 00004;
    static final int S_IWOTH = 00002;
    static final int S_IXOTH = 00001;

    Stat() {
    }

    abstract int mode();

    abstract int uid();

    abstract int gid();

    @Override
    public abstract long size();

    abstract long mtime();

    abstract int mtime_nsec();

    abstract long blocks();

    static Stat create(
            int mode,
            int uid,
            int gid,
            long size,
            long mtime,
            int mtime_nsec,
            long blocks) {

        return new AutoValue_Stat(
                mode,
                uid,
                gid,
                size,
                mtime,
                mtime_nsec,
                blocks);
    }

    static {
        init();
    }

    private static native void init();

    static native Stat stat(byte[] path) throws ErrnoException;

    static native Stat lstat(byte[] path) throws ErrnoException;

    static native Stat fstat(int fd) throws ErrnoException;

    static native void chmod(byte[] path, int mode) throws ErrnoException;

    static native void mkdir(byte[] path, int mode) throws ErrnoException;

    static Stat stat(LocalPath path, LinkOption option) throws IOException {
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
                    throw e.toIOException(path);
                }
            }
        }
    }

    private Instant lastModifiedTime;

    @Override
    public Instant lastModifiedTime() {
        if (lastModifiedTime == null) {
            lastModifiedTime = Instant.of(mtime(), mtime_nsec());
        }
        return lastModifiedTime;
    }

    @Override
    public long sizeOnDisk() {
        return blocks() * 512;
    }

    @Override
    public boolean isSymbolicLink() {
        return S_ISLNK(mode());
    }

    @Override
    public boolean isRegularFile() {
        return S_ISREG(mode());
    }

    @Override
    public boolean isDirectory() {
        return S_ISDIR(mode());
    }

    @Override
    public boolean isFifo() {
        return S_ISFIFO(mode());
    }

    @Override
    public boolean isSocket() {
        return S_ISSOCK(mode());
    }

    @Override
    public boolean isBlockDevice() {
        return S_ISBLK(mode());
    }

    @Override
    public boolean isCharacterDevice() {
        return S_ISCHR(mode());
    }

    private Set<Permission> permissions;

    @Override
    public Set<Permission> permissions() {
        if (permissions == null) {
            permissions = permissionsFromMode(mode());
        }
        return permissions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode());
        dest.writeInt(uid());
        dest.writeInt(gid());
        dest.writeLong(size());
        dest.writeLong(mtime());
        dest.writeInt(mtime_nsec());
        dest.writeLong(blocks());
    }

    public static final Creator<Stat> CREATOR = new Creator<Stat>() {

        @Override
        public Stat createFromParcel(Parcel source) {
            int mode = source.readInt();
            int uid = source.readInt();
            int gid = source.readInt();
            long size = source.readLong();
            long mtime = source.readLong();
            int mtimeNs = source.readInt();
            long blocks = source.readLong();
            return create(mode, uid, gid, size, mtime, mtimeNs, blocks);
        }

        @Override
        public Stat[] newArray(int size) {
            return new Stat[size];
        }
    };
}
