package l.files.fs.local;

import java.io.IOException;
import java.util.Set;

import auto.parcel.AutoParcel;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Stat;

import static android.system.OsConstants.S_ISBLK;
import static android.system.OsConstants.S_ISCHR;
import static android.system.OsConstants.S_ISDIR;
import static android.system.OsConstants.S_ISFIFO;
import static android.system.OsConstants.S_ISLNK;
import static android.system.OsConstants.S_ISREG;
import static android.system.OsConstants.S_ISSOCK;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.local.LocalResource.permissionsFromMode;

@AutoParcel
public abstract class LocalStat implements Stat
{

    private Instant atime;
    private Instant mtime;
    private Set<Permission> permissions;

    public abstract l.files.fs.local.Stat stat();

    public static LocalStat create(final l.files.fs.local.Stat stat)
    {
        return new AutoParcel_LocalStat(stat);
    }

    @Override
    public Instant accessTime()
    {
        if (atime == null)
        {
            atime = Instant.of(stat().atime(), stat().atime_nsec());
        }
        return atime;
    }

    @Override
    public Instant modificationTime()
    {
        if (mtime == null)
        {
            mtime = Instant.of(stat().mtime(), stat().mtime_nsec());
        }
        return mtime;
    }

    @Override
    public long size()
    {
        return stat().size();
    }

    @Override
    public boolean isSymbolicLink()
    {
        return S_ISLNK(stat().mode());
    }

    @Override
    public boolean isRegularFile()
    {
        return S_ISREG(stat().mode());
    }

    @Override
    public boolean isDirectory()
    {
        return S_ISDIR(stat().mode());
    }

    public long inode()
    {
        return stat().ino();
    }

    @Override
    public boolean isFifo()
    {
        return S_ISFIFO(stat().mode());
    }

    @Override
    public boolean isSocket()
    {
        return S_ISSOCK(stat().mode());
    }

    @Override
    public boolean isBlockDevice()
    {
        return S_ISBLK(stat().mode());
    }

    @Override
    public boolean isCharacterDevice()
    {
        return S_ISCHR(stat().mode());
    }

    @Override
    public Set<Permission> permissions()
    {
        if (permissions == null)
        {
            permissions = permissionsFromMode(stat().mode());
        }
        return permissions;
    }

    public static LocalStat stat(
            final LocalResource resource,
            final LinkOption option) throws IOException
    {
        requireNonNull(option, "option");

        final l.files.fs.local.Stat stat;
        try
        {
            if (option == FOLLOW)
            {
                stat = l.files.fs.local.Stat.stat(resource.path());
            }
            else
            {
                stat = l.files.fs.local.Stat.lstat(resource.path());
            }
        }
        catch (final ErrnoException e)
        {
            throw e.toIOException(resource.path());
        }

        return create(stat);
    }

}
