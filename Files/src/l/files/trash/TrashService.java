package l.files.trash;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public final class TrashService extends IntentService {

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
    if (path == null) return;

    File file = new File(path);
    if (!file.exists()) return;

    helper.moveToTrash(file);
  }
}
