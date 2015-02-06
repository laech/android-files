package l.files.fs.local;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.Resource;

@AutoValue
abstract class LocalPathEntry implements PathEntry {
  LocalPathEntry() {}

  @Override public abstract LocalPath getPath();

  @Override public Resource getResource() {
    return new LocalResource(getPath());
  }

  abstract long ino();

  abstract boolean isDirectory();

  static LocalPathEntry create(File parent, Dirent entry) {
    return create(parent, entry.ino(), entry.name(), entry.type() == Dirent.DT_DIR);
  }

  static LocalPathEntry create(File parent, long ino, String name, boolean isDirectory) {
    LocalPath file = LocalPath.of(new File(parent, name));
    return new AutoValue_LocalPathEntry(file, ino, isDirectory);
  }

  static LocalPathEntry stat(File file) throws IOException {
    return stat(LocalPath.of(file));
  }

  static LocalPathEntry stat(Path path) throws IOException {
    LocalResourceStatus status = LocalResourceStatus.stat(path, false);
    return new AutoValue_LocalPathEntry((LocalPath) path, status.getInode(), status.getIsDirectory());
  }
}
