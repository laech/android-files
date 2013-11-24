package l.files.service;

import android.os.AsyncTask;
import android.os.SystemClock;

import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.io.FileUtils.isSymlink;

class FileCounter
    extends AsyncTask<File, FileCounter.Result, FileCounter.Result> {

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final int id;

  FileCounter(int id) {
    this.id = id;
  }

  public int id() {
    return id;
  }

  @Override protected Result doInBackground(File... files) {
    try {
      return new Walker().walk(newHashSet(files));
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
    private long start;
    private long size;
    private int count;

    public Result walk(Set<File> roots) throws IOException {
      start = now();
      for (File root : roots) {
        walk(root, null);
      }
      return new Result(count, size);
    }

    private long now() {
      return SystemClock.elapsedRealtime();
    }

    @Override protected void handleFile(
        File file, int depth, Collection<Object> results) throws IOException {

      count++;
      size += file.length();

      long now = now();
      if (now - start >= PROGRESS_UPDATE_DELAY_MILLIS) {
        start = now;
        publishProgress(new Result(count, size));
      }
    }
  }
}
