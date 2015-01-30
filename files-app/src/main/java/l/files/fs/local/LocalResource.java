package l.files.fs.local;

import com.google.auto.value.AutoValue;
import com.google.common.net.MediaType;

import org.apache.tika.Tika;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import l.files.fs.DirectoryStream;
import l.files.fs.FileStatus;
import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.WatchService;

@AutoValue
abstract class LocalResource implements Resource {
  LocalResource() {}

  public abstract LocalPath path();

  public static LocalResource create(LocalPath path) {
    return new AutoValue_LocalResource(path);
  }

  @Override public String name() {
    return path().getName();
  }

  @Override public Resource resolve(String other) {
    return create(path().resolve(other));
  }

  @Override public FileStatus stat() throws IOException {
    return LocalFileStatus.stat(path(), false);
  }

  @Override public boolean exists() {
    try {
      Unistd.access(path().toString(), Unistd.F_OK);
      return true;
    } catch (ErrnoException e) {
      return false;
    }
  }

  @Override public DirectoryStream newDirectoryStream() {
    return LocalDirectoryStream.open(path());
  }

  @Override public InputStream newInputStream() throws IOException {
    return new FileInputStream(path().toString());
  }

  @Override public void createDirectory() throws IOException {
    createDirectory(path());
  }

  private void createDirectory(LocalPath path) throws IOException {
    if (path.getResource().exists()) {
      return;
    }
    createDirectory(path.getParent());
    if (!new java.io.File(path.toString()).mkdir()) {
      throw new IOException(); // TODO use native code to get errno
    }
  }

  @Override public void move(Path dst) throws IOException {
    LocalPath.check(dst);
    try {
      Stdio.rename(path().toString(), dst.toString());
    } catch (ErrnoException e) {
      throw e.toFileSystemException(); // TODO
    }
  }

  @Override public MediaType detectMediaType() throws IOException {
    try (InputStream in = newInputStream()) {
      return MediaType.parse(TikaHolder.TIKA.detect(in));
    }
  }

  @Override public WatchService watcher() {
    return LocalWatchService.get();
  }

  private static class TikaHolder {
    static final Tika TIKA = new Tika();
  }
}
