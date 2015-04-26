package l.files.fs;

import com.google.common.net.MediaType;

import java.util.Set;

public interface ResourceStatus {

    Resource getResource();

    String getName();

    long getSize();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    Instant getAccessTime();

    Instant getModificationTime();

    MediaType getBasicMediaType();

    Set<Permission> getPermissions();

}
