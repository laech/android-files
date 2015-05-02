package l.files.fs.local;

import java.io.IOException;
import java.util.Set;

import auto.parcel.AutoParcel;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Stat;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.local.LocalResource.mapPermissions;

@AutoParcel
public abstract class LocalStat implements Stat
{

    private Instant atime;
    private Instant mtime;
    private Set<Permission> permissions;

    public abstract l.files.fs.local.Stat getStat();

    public static LocalStat create(l.files.fs.local.Stat stat) {
        return new AutoParcel_LocalStat(stat);
    }

    @Override
    public Instant accessTime() {
        if (atime == null) {
            atime = Instant.of(getStat().getAtime(), getStat().getAtimeNsec());
        }
        return atime;
    }

    @Override
    public Instant modificationTime() {
        if (mtime == null) {
            mtime = Instant.of(getStat().getMtime(), getStat().getMtimeNsec());
        }
        return mtime;
    }

    @Override
    public long size() {
        return getStat().getSize();
    }

    @Override
    public boolean isSymbolicLink() {
        return l.files.fs.local.Stat.S_ISLNK(getStat().getMode());
    }

    @Override
    public boolean isRegularFile() {
        return l.files.fs.local.Stat.S_ISREG(getStat().getMode());
    }

    @Override
    public boolean isDirectory() {
        return l.files.fs.local.Stat.S_ISDIR(getStat().getMode());
    }

    public long getDevice() {
        return getStat().getDev();
    }

    public long getInode() {
        return getStat().getIno();
    }

    @Override
    public boolean isFifo() {
        return l.files.fs.local.Stat.S_ISFIFO(getStat().getMode());
    }

    @Override
    public boolean isSocket() {
        return l.files.fs.local.Stat.S_ISSOCK(getStat().getMode());
    }

    @Override
    public boolean isBlockDevice() {
        return l.files.fs.local.Stat.S_ISBLK(getStat().getMode());
    }

    @Override
    public boolean isCharacterDevice() {
        return l.files.fs.local.Stat.S_ISCHR(getStat().getMode());
    }

    @Override
    public Set<Permission> permissions() {
        if (permissions == null) {
            permissions = mapPermissions(getStat().getMode());
        }
        return permissions;
    }

    public static LocalStat stat(
            LocalResource resource,
            LinkOption option) throws IOException {

        requireNonNull(option, "option");
        l.files.fs.local.Stat stat;
        try {
            if (option == FOLLOW) {
                stat = l.files.fs.local.Stat.stat(resource.path());
            } else {
                stat = l.files.fs.local.Stat.lstat(resource.path());
            }
        } catch (ErrnoException e) {
            throw e.toIOException(resource.path());
        }

        return create(stat);
    }

}
