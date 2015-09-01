package l.files.fs;

import java.util.Set;

public interface Stat {

  long size();

  boolean isRegularFile();

  boolean isDirectory();

  boolean isSymbolicLink();

  boolean isFifo();

  boolean isSocket();

  boolean isBlockDevice();

  boolean isCharacterDevice();

  Instant lastAccessedTime();

  Instant lastModifiedTime();

  Set<Permission> permissions();

}
