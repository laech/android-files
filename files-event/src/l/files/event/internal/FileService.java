package l.files.event.internal;

import static l.files.common.io.Files.getNonExistentDestinationFile;
import static org.apache.commons.io.FileUtils.*;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public abstract class FileService extends IntentService {

  // TODO show error on failure

  static abstract class CutOrCopy extends FileService {
    @Override protected final void handle(Intent intent) throws IOException {
      File source = new File(intent.getStringExtra(EXTRA_SOURCE));
      File dstDir = new File(intent.getStringExtra(EXTRA_DESTINATION));
      handle(source, getNonExistentDestinationFile(source, dstDir));
    }

    protected abstract void handle(File from, File to) throws IOException;
  }

  public static final class Cut extends CutOrCopy {
    @Override protected void handle(File from, File to) throws IOException {
      if (from.isDirectory()) {
        moveDirectory(from, to);
      } else {
        moveFile(from, to);
      }
    }
  }

  public static final class Copy extends CutOrCopy {
    @Override protected void handle(File from, File to) throws IOException {
      if (from.isDirectory()) {
        copyDirectory(from, to);
      } else {
        copyFile(from, to);
      }
    }
  }

  public static final class Delete extends FileService {
    @Override protected void handle(Intent intent) throws IOException {
      forceDelete(new File(intent.getStringExtra(EXTRA_SOURCE)));
    }
  }

  /**
   * Creates new a intent to move a file to new destination.
   * <p/>
   * If the destination already has a file with the same name, the new file will
   * be renamed.
   *
   * @param srcFile the source file/directory to be moved.
   * @param dstDir the destination directory to hold the moved file.
   */
  public static Intent cut(Context context, File srcFile, File dstDir) {
    return cutOrCopy(context, srcFile, dstDir, Cut.class);
  }

  /**
   * Creates new a intent to copy a file to new destination.
   * <p/>
   * If the destination already has a file with the same name, the new file will
   * be renamed.
   *
   * @param srcFile the source file/directory to be moved.
   * @param dstDir the destination directory to hold the moved file.
   */
  public static Intent copy(Context context, File srcFile, File dstDir) {
    return cutOrCopy(context, srcFile, dstDir, Copy.class);
  }

  private static Intent cutOrCopy(
      Context context, File srcFile, File dstDir, Class<?> service) {
    return new Intent(context, service)
        .putExtra(EXTRA_SOURCE, srcFile.getAbsolutePath())
        .putExtra(EXTRA_DESTINATION, dstDir.getAbsolutePath());
  }

  public static Intent delete(Context context, File file) {
    return new Intent(context, Delete.class)
        .putExtra(EXTRA_SOURCE, file.getAbsolutePath());
  }

  static final String EXTRA_SOURCE = "l.files.intent.extra.SOURCE";
  static final String EXTRA_DESTINATION = "l.files.intent.extra.DESTINATION";

  public FileService() {
    super("FileService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    try {
      handle(intent);
    } catch (IOException e) {
      Log.w(getClass().getSimpleName(), e);
    }
  }

  protected abstract void handle(Intent intent) throws IOException;
}
