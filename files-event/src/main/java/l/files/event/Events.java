package l.files.event;

import static l.files.common.io.Files.toAbsolutePaths;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import java.io.File;
import java.util.Set;

public final class Events {
  private Events() {}

  public static void registerShowHiddenFilesProvider(
      Bus bus, SharedPreferences pref, boolean showByDefault) {
    new ShowHiddenFilesProvider(showByDefault).register(bus, pref);
  }

  public static void registerSortProvider(
      Bus bus, SharedPreferences pref, String defaultSort) {
    new SortProvider(defaultSort).register(bus, pref);
  }

  public static void registerBookmarksProvider(
      Bus bus, SharedPreferences pref, Set<File> defaults) {
    String[] paths = toAbsolutePaths(defaults);
    new BookmarksProvider(ImmutableSet.copyOf(paths)).register(bus, pref);
  }

  public static void registerClipboardProvider(Bus bus, ClipboardManager manager) {
    bus.register(new ClipboardProvider(manager));
  }

  public static void registerIoProvider(Bus bus, Application context) {
    bus.register(new IoProvider(context));
  }
}
