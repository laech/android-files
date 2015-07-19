package l.files.fs;

import java.util.Set;

public interface Stat
{
    long size();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    boolean isFifo();

    boolean isSocket();

    boolean isBlockDevice();

    boolean isCharacterDevice();

    @Deprecated
    Instant accessed();

    @Deprecated
    Instant modified();

    Instant atime();

    Instant mtime();

    Instant ctime();

    Set<Permission> permissions();
}
