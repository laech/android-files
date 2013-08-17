package l.files.app;

import static l.files.app.Intents.*;
import static org.apache.commons.io.FileUtils.*;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public final class FileService extends IntentService {

  private static final String TAG = FileService.class.getSimpleName();

  /**
   * Creates new a intent to move a file to new destination.
   * <p/>
   * If the destination already has a file with the same name, the new file will
   * be renamed.
   *
   * @param srcFile the source file/directory to be moved.
   * @param dstDir the destination directory to hold the moved file.
   */
  public static Intent cut(File srcFile, File dstDir, Context context) {
    return newIntent(srcFile, dstDir, context, ACTION_CUT);
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
  public static Intent copy(File srcFile, File dstDir, Context context) {
    return newIntent(srcFile, dstDir, context, ACTION_COPY);
  }

  private static Intent newIntent(
      File srcFile, File dstDir, Context context, String action) {
    return new Intent(action)
        .setClass(context, FileService.class)
        .putExtra(EXTRA_FILE, srcFile.getAbsolutePath())
        .putExtra(EXTRA_DESTINATION, dstDir.getAbsolutePath());
  }

  public FileService() {
    super("FileService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    String action = intent.getAction();
    File src = new File(intent.getStringExtra(EXTRA_FILE));
    File dst = new File(intent.getStringExtra(EXTRA_DESTINATION));
    try {
      if (ACTION_CUT.equals(action)) {
        cut(src, dst);
      } else if (ACTION_COPY.equals(action)) {
        copy(src, dst);
      }
    } catch (IOException e) {
      Log.w(TAG, e);
    }
  }

  private void cut(File src, File dst) throws IOException {
    if (src.isDirectory()) {
      moveDirectory(src, getDestinationFile(src, dst));
    } else {
      moveFile(src, getDestinationFile(src, dst));
    }
  }

  private void copy(File src, File dst) throws IOException {
    if (src.isDirectory()) {
      copyDirectory(src, getDestinationFile(src, dst));
    } else {
      copyFile(src, getDestinationFile(src, dst));
    }
  }

  private static File getDestinationFile(File srcFile, File dstDir) {
    File file = new File(dstDir, srcFile.getName());
    for (int i = 2; file.exists(); ++i) {
      file = new File(dstDir, srcFile.getName() + " " + i);
    }
    return file;
  }
}
