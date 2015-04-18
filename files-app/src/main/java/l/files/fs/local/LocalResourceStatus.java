package l.files.fs.local;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.util.Set;

import auto.parcel.AutoParcel;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.ResourceStatus;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.local.LocalResource.mapPermissions;

@AutoParcel
public abstract class LocalResourceStatus implements ResourceStatus {

    private Boolean readable;
    private Boolean writable;
    private Boolean executable;
    private Instant atime;
    private Instant mtime;
    private Set<Permission> permissions;

    @Override
    public abstract LocalResource getResource();

    public abstract Stat getStat();

    public static LocalResourceStatus create(LocalResource resource, Stat stat) {
        return new AutoParcel_LocalResourceStatus(resource, stat);
    }

    @Override
    public String getName() {
        return getResource().getName();
    }

    @Override
    public boolean isReadable() {
        if (readable == null) {
            readable = access(Unistd.R_OK);
        }
        return readable;
    }

    @Override
    public boolean isWritable() {
        if (writable == null) {
            writable = access(Unistd.W_OK);
        }
        return writable;
    }

    @Override
    public boolean isExecutable() {
        if (executable == null) {
            executable = access(Unistd.X_OK);
        }
        return executable;
    }

    private boolean access(int mode) {
        try {
            Unistd.access(getResource().getFile().getPath(), mode);
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    @Override
    public Instant getAccessTime() {
        if (atime == null) {
            atime = Instant.of(getStat().getAtime(), getStat().getAtimeNsec());
        }
        return atime;
    }

    @Override
    public Instant getModificationTime() {
        if (mtime == null) {
            mtime = Instant.of(getStat().getMtime(), getStat().getMtimeNsec());
        }
        return mtime;
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
    public Set<Permission> getPermissions() {
        if (permissions == null) {
            permissions = mapPermissions(getStat().getMode());
        }
        return permissions;
    }

    @Override
    public MediaType getBasicMediaType() {
        try {
            return BasicFileTypeDetector.INSTANCE.detect(this);
        } catch (IOException e) {
            return OCTET_STREAM;
        }
    }

    public static LocalResourceStatus stat(
            LocalResource resource, boolean followLink) throws IOException {

        Stat stat;
        try {
            if (followLink) {
                stat = Stat.stat(resource.getFile().getPath());
            } else {
                stat = Stat.lstat(resource.getFile().getPath());
            }
        } catch (ErrnoException e) {
            throw e.toIOException(resource.getPath());
        }

        return create(resource, stat);
    }

}
