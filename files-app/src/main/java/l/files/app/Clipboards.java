package l.files.app;

import android.content.ClipboardManager;
import android.content.Intent;

import java.util.Set;

import static android.content.ClipData.newIntent;

public final class Clipboards {

  private static final String ACTION_CUT = "l.files.intent.action.CUT";
  private static final String ACTION_COPY = "l.files.intent.action.COPY";
  private static final String EXTRA_FILE_IDS = "l.files.intent.extra.FILE_IDS";

  private Clipboards() {}

  public static boolean hasClip(ClipboardManager manager) {
    try {
      String action = manager.getPrimaryClip().getItemAt(0).getIntent().getAction();
      return action.equals(ACTION_CUT)
          || action.equals(ACTION_COPY);

    } catch (NullPointerException e) {
      return false;
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }

  public static void setCut(ClipboardManager manager, Set<String> fileIds) {
    setClipData(manager, fileIds, ACTION_CUT);
  }

  public static void setCopy(ClipboardManager manager, Set<String> fileIds) {
    setClipData(manager, fileIds, ACTION_COPY);
  }

  private static void setClipData(ClipboardManager manager, Set<String> fileIds, String action) {
    String[] data = fileIds.toArray(new String[fileIds.size()]);
    Intent intent = new Intent(action).putExtra(EXTRA_FILE_IDS, data);
    manager.setPrimaryClip(newIntent(null, intent));
  }
}
