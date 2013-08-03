package l.files.setting;

import android.content.SharedPreferences;
import com.google.common.collect.Sets;
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
    Set<String> paths = getAbsolutePaths(defaults);
    new BookmarksProvider(paths).register(bus, pref);
  }

  private static Set<String> getAbsolutePaths(Set<File> files) {
    Set<String> paths = Sets.newHashSetWithExpectedSize(files.size());
    for (File bookmark : files) {
      paths.add(bookmark.getAbsolutePath());
    }
    return paths;
  }
}
