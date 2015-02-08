package l.files.ui;

import android.content.ClipboardManager;
import android.content.Intent;

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Set;

import l.files.fs.Path;

import static android.content.ClipData.newIntent;
import static android.content.ClipData.newPlainText;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptySet;

public final class Clipboards {

  private static final String ACTION_CUT = "l.files.intent.action.CUT";
  private static final String ACTION_COPY = "l.files.intent.action.COPY";
  private static final String EXTRA_PATHS = "l.files.intent.extra.PATHS";

  private Clipboards() {}

  public static void clear(ClipboardManager manager) {
    manager.setPrimaryClip(newPlainText("", ""));
  }

  public static boolean hasClip(ClipboardManager manager) {
    String action = getAction(manager);
    return ACTION_CUT.equals(action)
        || ACTION_COPY.equals(action);
  }

  public static boolean isCut(ClipboardManager manager) {
    return ACTION_CUT.equals(getAction(manager));
  }

  public static boolean isCopy(ClipboardManager manager) {
    return ACTION_COPY.equals(getAction(manager));
  }

  public static Set<Path> getPaths(ClipboardManager manager) {
    Intent intent = getClipboardIntent(manager);
    if (intent == null) {
      return emptySet();
    }
    intent.setExtrasClassLoader(Clipboards.class.getClassLoader());
    ArrayList<Path> extras = intent.getParcelableArrayListExtra(EXTRA_PATHS);
    if (extras == null) {
      return emptySet();
    }
    return ImmutableSet.copyOf(extras);
  }

  private static Intent getClipboardIntent(ClipboardManager manager) {
    try {
      return manager.getPrimaryClip().getItemAt(0).getIntent();
    } catch (NullPointerException e) {
      return null;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  private static String getAction(ClipboardManager manager) {
    Intent intent = getClipboardIntent(manager);
    if (intent == null) {
      return null;
    }
    return intent.getAction();
  }

  public static void setCut(ClipboardManager manager, Iterable<Path> paths) {
    setClipData(manager, paths, ACTION_CUT);
  }

  public static void setCopy(ClipboardManager manager, Iterable<Path> paths) {
    setClipData(manager, paths, ACTION_COPY);
  }

  private static void setClipData(
      ClipboardManager manager, Iterable<Path> paths, String action) {
    Intent intent = new Intent(action).putParcelableArrayListExtra(EXTRA_PATHS, newArrayList(paths));
    manager.setPrimaryClip(newIntent(null, intent));
  }
}
