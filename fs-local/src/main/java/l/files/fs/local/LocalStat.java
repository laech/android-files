package l.files.fs.local;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.util.Set;

import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Stat;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.local.ErrnoException.EAGAIN;
import static l.files.fs.local.LocalFile.permissionsFromMode;
import static l.files.fs.local.Stat.S_ISBLK;
import static l.files.fs.local.Stat.S_ISCHR;
import static l.files.fs.local.Stat.S_ISDIR;
import static l.files.fs.local.Stat.S_ISFIFO;
import static l.files.fs.local.Stat.S_ISLNK;
import static l.files.fs.local.Stat.S_ISREG;
import static l.files.fs.local.Stat.S_ISSOCK;

@AutoValue
abstract class LocalStat implements Stat {

    private Instant lastModifiedTime;
    private Set<Permission> permissions;

    public abstract l.files.fs.local.Stat stat();

    static LocalStat create(final l.files.fs.local.Stat stat) {
        return new AutoValue_LocalStat(stat);
    }

    @Override
    public Instant lastModifiedTime() {
        if (lastModifiedTime == null) {
            lastModifiedTime = Instant.of(stat().mtime(), stat().mtime_nsec());
        }
        return lastModifiedTime;
    }

    @Override
    public long size() {
        return stat().size();
    }

    @Override
    public boolean isSymbolicLink() {
        return S_ISLNK(stat().mode());
    }

    @Override
    public boolean isRegularFile() {
        return S_ISREG(stat().mode());
    }

    @Override
    public boolean isDirectory() {
        return S_ISDIR(stat().mode());
    }

    @Override
    public boolean isFifo() {
        return S_ISFIFO(stat().mode());
    }

    @Override
    public boolean isSocket() {
        return S_ISSOCK(stat().mode());
    }

    @Override
    public boolean isBlockDevice() {
        return S_ISBLK(stat().mode());
    }

    @Override
    public boolean isCharacterDevice() {
        return S_ISCHR(stat().mode());
    }

    @Override
    public Set<Permission> permissions() {
        if (permissions == null) {
            permissions = permissionsFromMode(stat().mode());
        }
        return permissions;
    }

    static LocalStat stat(LocalFile file, LinkOption option) throws IOException {
        requireNonNull(option, "option");

        l.files.fs.local.Stat stat = null;
        while (stat == null) {
            try {
                if (option == FOLLOW) {
                    stat = l.files.fs.local.Stat.stat(file.path());
                } else {
                    stat = l.files.fs.local.Stat.lstat(file.path());
                }
            } catch (final ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e.toIOException(file.path());
                }
            }
        }
        return create(stat);
    }

}
