package l.files.fs.local;

import android.os.Parcel;

import java.io.IOException;
import java.util.Set;

import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import linux.ErrnoException;
import linux.Stat;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.local.LocalFileSystem.permissionsFromMode;
import static linux.Errno.EAGAIN;
import static linux.Stat.S_ISBLK;
import static linux.Stat.S_ISCHR;
import static linux.Stat.S_ISDIR;
import static linux.Stat.S_ISFIFO;
import static linux.Stat.S_ISLNK;
import static linux.Stat.S_ISREG;
import static linux.Stat.S_ISSOCK;

final class LocalStat implements l.files.fs.Stat {

    private static final ThreadLocal<Stat> buffers = new ThreadLocal<Stat>() {
        @Override
        protected Stat initialValue() {
            return new Stat();
        }
    };

    private int mode;
    private long size;
    private long mtime;
    private int mtime_nsec;
    private long blocks;

    private LocalStat(
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

    LocalStat() {
    }

    void set(Stat stat) {
        this.mode = stat.st_mode;
        this.size = stat.st_size;
        this.mtime = stat.st_mtime;
        this.mtime_nsec = stat.st_mtime_nsec;
        this.blocks = stat.st_blocks;
    }

    int mode() {
        return this.mode;
    }

    @Override
    public long size() {
        return this.size;
    }

    long mtime() {
        return this.mtime;
    }

    int mtime_nsec() {
        return this.mtime_nsec;
    }

    long blocks() {
        return this.blocks;
    }

    static LocalStat stat(LocalPath path, LinkOption option) throws IOException {
        LocalStat stat = new LocalStat();
        stat(path, option, stat);
        return stat;
    }

    static void stat(LocalPath path, LinkOption option, LocalStat buffer) throws IOException {
        requireNonNull(option, "option");
        requireNonNull(buffer, "buffer");

        linux.Stat stat = buffers.get();
        while (true) {
            try {

                if (option == FOLLOW) {
                    linux.Stat.stat(path.path, stat);
                } else {
                    linux.Stat.lstat(path.path, stat);
                }
                buffer.set(stat);
                break;

            } catch (final ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw ErrnoExceptions.toIOException(e, path);
                }
            }
        }
    }

    @Override
    public Instant lastModifiedTime() {
        return Instant.of(mtime(), mtime_nsec());
    }

    @Override
    public long lastModifiedEpochSecond() {
        return mtime();
    }

    @Override
    public int lastModifiedNanoOfSecond() {
        return mtime_nsec();
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

    @Override
    public Set<Permission> permissions() {
        return permissionsFromMode(mode());
    }

    @Override
    public String toString() {
        return "LocalStat{" +
                "mode=" + mode +
                ", size=" + size +
                ", mtime=" + mtime +
                ", mtime_nsec=" + mtime_nsec +
                ", blocks=" + blocks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocalStat that = (LocalStat) o;

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
        dest.writeInt(mode());
        dest.writeLong(size());
        dest.writeLong(mtime());
        dest.writeInt(mtime_nsec());
        dest.writeLong(blocks());
    }

    public static final Creator<LocalStat> CREATOR = new Creator<LocalStat>() {

        @Override
        public LocalStat createFromParcel(Parcel source) {
            int mode = source.readInt();
            long size = source.readLong();
            long mtime = source.readLong();
            int mtimeNs = source.readInt();
            long blocks = source.readLong();
            return new LocalStat(mode, size, mtime, mtimeNs, blocks);
        }

        @Override
        public LocalStat[] newArray(int size) {
            return new LocalStat[size];
        }
    };
}
