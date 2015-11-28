package l.files.fs;

import android.os.Parcelable;

import java.util.Set;

public interface Stat extends Parcelable {

    long size();

    boolean isRegularFile();

    boolean isDirectory();

    boolean isSymbolicLink();

    boolean isFifo();

    boolean isSocket();

    boolean isBlockDevice();

    boolean isCharacterDevice();

    Instant lastModifiedTime();

    Set<Permission> permissions();

}
