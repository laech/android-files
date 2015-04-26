package l.files.fs.local;

import java.io.IOException;
import java.util.Set;

import auto.parcel.AutoParcel;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.ResourceStatus;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.local.LocalResource.mapPermissions;

@AutoParcel
public abstract class LocalResourceStatus implements ResourceStatus {

    private Instant atime;
    private Instant mtime;
    private Set<Permission> permissions;

    public abstract Stat getStat();

    public static LocalResourceStatus create(Stat stat) {
        return new AutoParcel_LocalResourceStatus(stat);
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

    @Override
    public boolean isFifo() {
        return Stat.S_ISFIFO(getStat().getMode());
    }

    @Override
    public boolean isSocket() {
        return Stat.S_ISSOCK(getStat().getMode());
    }

    @Override
    public boolean isBlockDevice() {
        return Stat.S_ISBLK(getStat().getMode());
    }

    @Override
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

    public static LocalResourceStatus stat(
            LocalResource resource,
            LinkOption option) throws IOException {

        requireNonNull(option, "option");
        Stat stat;
        try {
            if (option == FOLLOW) {
                stat = Stat.stat(resource.getPath());
            } else {
                stat = Stat.lstat(resource.getPath());
            }
        } catch (ErrnoException e) {
            throw e.toIOException(resource.getPath());
        }

        return create(stat);
    }

}
