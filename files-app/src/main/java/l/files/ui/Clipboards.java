package l.files.ui;

import android.content.ClipboardManager;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Resource;

import static android.content.ClipData.newIntent;
import static android.content.ClipData.newPlainText;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public final class Clipboards {

  private static final String ACTION_CUT = "l.files.intent.action.CUT";
  private static final String ACTION_COPY = "l.files.intent.action.COPY";
  private static final String EXTRA_RESOURCES = "l.files.intent.extra.RESOURCES";

  private Clipboards() {
  }

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

  public static Set<Resource> getResources(ClipboardManager manager) {
    Intent intent = getClipboardIntent(manager);
    if (intent == null) {
      return emptySet();
    }
    intent.setExtrasClassLoader(Clipboards.class.getClassLoader());
    ArrayList<Resource> extras = intent.getParcelableArrayListExtra(EXTRA_RESOURCES);
    if (extras == null) {
      return emptySet();
    }
    return unmodifiableSet(new HashSet<>(extras));
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

  public static void setCut(ClipboardManager manager, Collection<? extends Resource> resources) {
    setClipData(manager, resources, ACTION_CUT);
  }

  public static void setCopy(ClipboardManager manager, Collection<? extends Resource> resources) {
    setClipData(manager, resources, ACTION_COPY);
  }

  private static void setClipData(
      ClipboardManager manager,
      Collection<? extends Resource> resources,
      String action) {
    Intent intent = new Intent(action);
    intent.putParcelableArrayListExtra(EXTRA_RESOURCES, new ArrayList<>(resources));
    manager.setPrimaryClip(newIntent(null, intent));
  }
}
