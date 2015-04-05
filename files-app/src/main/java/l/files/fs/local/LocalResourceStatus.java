package l.files.fs.local;

import com.google.common.net.MediaType;

import java.io.File;
import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Path;
import l.files.fs.ResourceStatus;

import static com.google.common.net.MediaType.OCTET_STREAM;

@AutoParcel
public abstract class LocalResourceStatus implements ResourceStatus {

    private final Lazy<Boolean> readable = new Lazy<Boolean>() {
        @Override
        protected Boolean doGet() {
            return access(Unistd.R_OK);
        }
    };

    private final Lazy<Boolean> writable = new Lazy<Boolean>() {
        @Override
        protected Boolean doGet() {
            return access(Unistd.W_OK);
        }
    };

    private final Lazy<Boolean> executable = new Lazy<Boolean>() {
        @Override
        protected Boolean doGet() {
            return access(Unistd.X_OK);
        }
    };

    @Override
    public abstract LocalPath getPath();

    public abstract Stat getStat();

    public static LocalResourceStatus create(LocalPath path, Stat stat) {
        return new AutoParcel_LocalResourceStatus(path, stat);
    }

    @Override
    public String getName() {
        return getPath().getName();
    }

    @Override
    public LocalResource getResource() {
        return getPath().getResource();
    }

    @Override
    public boolean isReadable() {
        return readable.get();
    }

    @Override
    public boolean isWritable() {
        return writable.get();
    }

    @Override
    public boolean isExecutable() {
        return executable.get();
    }

    private boolean access(int mode) {
        try {
            Unistd.access(getPath().toString(), mode);
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    @Override
    public long getLastModifiedTime() {
        return getStat().getMtime() * 1000;
    }

    @Override
    public long getSize() {
        return getStat().getSize();
    }

    @Override
    public boolean isSymbolicLink() {
        return Stat.S_ISLNK(getStat().getMode());
    }

    @Override
    public boolean isRegularFile() {
        return Stat.S_ISREG(getStat().getMode());
    }

    @Override
    public boolean isDirectory() {
        return Stat.S_ISDIR(getStat().getMode());
    }

    public long getDevice() {
        return getStat().getDev();
    }

    public long getInode() {
        return getStat().getIno();
    }

    public boolean isFifo() {
        return Stat.S_ISFIFO(getStat().getMode());
    }

    public boolean isSocket() {
        return Stat.S_ISSOCK(getStat().getMode());
    }

    public boolean isBlockDevice() {
        return Stat.S_ISBLK(getStat().getMode());
    }

    public boolean isCharacterDevice() {
        return Stat.S_ISCHR(getStat().getMode());
    }

    @Override
    public MediaType getBasicMediaType() {
        try {
            return BasicFileTypeDetector.INSTANCE.detect(this);
        } catch (IOException e) {
            return OCTET_STREAM;
        }
    }

    public static LocalResourceStatus stat(File file, boolean followLink) throws IOException {
        return stat(LocalPath.of(file), followLink);
    }

    public static LocalResourceStatus stat(Path path, boolean followLink) throws IOException {
        LocalPath.check(path);
        Stat stat;
        try {
            if (followLink) {
                stat = Stat.stat(path.toString());
            } else {
                stat = Stat.lstat(path.toString());
            }
        } catch (ErrnoException e) {
            throw e.toIOException();
        }

        return create((LocalPath) path, stat);
    }
}
