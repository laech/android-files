package l.files.trash;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.BuildConfig.DEBUG;

public final class TrashService extends IntentService {

  // TODO test

  public static final String ACTION_MOVE_TO_TRASH =
      "l.files.intent.action.MOVE_TO_TRASH";

  public static final String EXTRA_FILE_PATH =
      "l.files.intent.extra.FILE_PATH";

  private static final String TAG = "TrashService";

  private TrashHelper helper;

  public TrashService() {
    super("TrashService");
  }

  @Override public void onCreate() {
    super.onCreate();
    helper = TrashHelper.create(this);
  }

  @Override protected void onHandleIntent(Intent intent) {
    if (ACTION_MOVE_TO_TRASH.equals(intent.getAction())) {
      try {
        handleMoveToTrash(intent);
      } catch (IOException e) {
        Log.w(TAG, e);
      }
    }
  }

  private void handleMoveToTrash(Intent intent) throws IOException {
    String path = intent.getStringExtra(EXTRA_FILE_PATH);
    if (path == null) {
      Log.d(TAG, "Not provided: " + EXTRA_FILE_PATH);
      return;
    }

    File file = new File(path);
    if (!file.exists()) {
      Log.d(TAG, "Ignored a file that doesn't exist: " + file);
      return;
    }

    helper.moveToTrash(file);

    if (DEBUG) Log.d(TAG, "Moved to trash: " + file);
  }

  public static class TrashMover {

    private final Context context;

    public TrashMover(Context context) {
      this.context = checkNotNull(context, "context");
    }

    public void moveToTrash(Iterable<File> files) {
      checkNotNull(files, "files");
      for (File file : files) moveToTrash(file);
    }

    public void moveToTrash(File file) {
      context.startService(newIntent(checkNotNull(file, "file")));
    }

    private Intent newIntent(File file) {
      return new Intent(context, TrashService.class)
          .setAction(ACTION_MOVE_TO_TRASH)
          .putExtra(EXTRA_FILE_PATH, file.getAbsolutePath());
    }
  }
}
