package l.files.trash;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

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

  public static void moveToTrash(Iterable<File> files, Context context) {
    for (File file : files) moveToTrash(file, context);
  }

  public static void moveToTrash(File file, Context context) {
    checkNotNull(file, "file");
    checkNotNull(context, "context");
    context.startService(new Intent(context, TrashService.class)
        .setAction(ACTION_MOVE_TO_TRASH)
        .putExtra(EXTRA_FILE_PATH, file.getAbsolutePath()));
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
    if (path == null) return;

    File file = new File(path);
    if (!file.exists()) return;

    helper.moveToTrash(file);
  }
}
