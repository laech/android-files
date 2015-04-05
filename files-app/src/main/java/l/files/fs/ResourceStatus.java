package l.files.fs;

import com.google.common.net.MediaType;

public interface ResourceStatus extends PathEntry {

    String getName();

    long getSize();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    boolean isReadable();

    boolean isWritable();

    boolean isExecutable();

    long getLastModifiedTime();

    MediaType getBasicMediaType();

}
