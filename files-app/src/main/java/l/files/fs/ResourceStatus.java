package l.files.fs;

import com.google.common.net.MediaType;

public interface ResourceStatus {

    Resource getResource();

    String getName();

    long getSize();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    boolean isReadable();

    boolean isWritable();

    boolean isExecutable();

    Instant getAccessTime();

    Instant getModificationTime();

    MediaType getBasicMediaType();

}
