package l.files.fs;

import java.util.Set;

public interface Stat {

    long size();

    long sizeOnDisk();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    boolean isFifo();

    boolean isSocket();

    boolean isBlockDevice();

    boolean isCharacterDevice();

    Instant lastModifiedTime();

    long lastModifiedEpochSecond();

    int lastModifiedNanoOfSecond();

    Set<Permission> permissions();

}
