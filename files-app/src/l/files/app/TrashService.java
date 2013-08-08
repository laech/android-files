package l.files.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.forceDelete;

public final class TrashService extends IntentService {

  public static final String EXTRA_FILE = "file";

  private static final String TAG = TrashService.class.getSimpleName();

  public TrashService() {
    super("TrashService");
  }

  public static void delete(File file, Context context) {
    context.startService(new Intent(context, TrashService.class)
        .putExtra(EXTRA_FILE, file.getAbsolutePath()));
  }

  public static void delete(Iterable<File> files, Context context) {
    for (File file : files) {
      delete(file, context);
    }
  }

  @Override protected void onHandleIntent(Intent intent) {
    File file = new File(intent.getStringExtra(EXTRA_FILE));
    try {
      forceDelete(file);
    } catch (IOException e) {
      Log.e(TAG, "failed to delete " + file, e);
    }
  }
}
