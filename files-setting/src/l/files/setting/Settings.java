package l.files.setting;

import static l.files.common.io.Files.toAbsolutePaths;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import java.io.File;
import java.util.Set;

public final class Settings {
  private Settings() {}

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
    Set<String> paths = toAbsolutePaths(defaults);
    new BookmarksProvider(paths).register(bus, pref);
  }

  public static void registerClipboardProvider(Bus bus, ClipboardManager manager) {
    bus.register(new ClipboardProvider(manager));
  }
}
