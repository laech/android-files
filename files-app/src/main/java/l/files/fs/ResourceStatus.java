package l.files.fs;

import java.util.Set;

public interface ResourceStatus {

    Resource getResource();

    String getName();

    long getSize();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    boolean isFifo();

    boolean isSocket();

    boolean isBlockDevice();

    boolean isCharacterDevice();

    Instant getAccessTime();

    Instant getModificationTime();

    Set<Permission> getPermissions();

}
