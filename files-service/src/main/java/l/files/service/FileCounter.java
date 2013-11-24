package l.files.service;

import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.apache.commons.io.FileUtils.isSymlink;

abstract class FileCounter
    extends ProgressService.Task<Object, FileCounter.Result, FileCounter.Result> {

  private final Set<File> files;

  FileCounter(int id, ProgressService service, Set<File> files) {
    super(id, service);
    this.files = files;
  }

  @Override protected Result doInBackground(Object... params) {
    try {
      return new Walker().walk(files);
    } catch (IOException e) {
      return null;
    }
  }

  public static final class Result {
    private final int count;
    private final long size;

    public Result(int count, long size) {
      this.count = count;
      this.size = size;
    }

    public int filesCount() {
      return count;
    }

    public long filesSize() {
      return size;
    }
  }

  private class BaseWalker<T> extends DirectoryWalker<T> {

    @Override protected boolean handleIsCancelled(
        File file, int depth, Collection<T> results) throws IOException {
      return isCancelled();
    }

    @Override protected boolean handleDirectory(
        File directory, int depth, Collection<T> results) throws IOException {
      return !isSymlink(directory);
    }
  }

  private final class Walker extends BaseWalker<Object> {
    private long size;
    private int count;

    public Result walk(Set<File> roots) throws IOException {
      for (File root : roots) {
        walk(root, null);
      }
      return new Result(count, size);
    }

    @Override protected void handleFile(
        File file, int depth, Collection<Object> results) throws IOException {

      count++;
      size += file.length();

      if (setAndGetUpdateProgress()) {
        publishProgress(new Result(count, size));
      }
    }
  }
}
