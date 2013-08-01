package l.files.setting;

import android.content.SharedPreferences;
import com.squareup.otto.Bus;

import java.io.File;
import java.util.Set;

public final class Settings {
  private Settings() {}

  /**
   * Registers a provide to handle bookmarks.
   * <p/>
   * After registration, the following events will be handled if they are posted
   * to the bus: {@link AddBookmarkRequest}, {@link RemoveBookmarkRequest}.
   * <p/>
   * And the following event will be posted to the bus when the preference
   * changes: {@link BookmarksEvent}.
   */
  public static void registerBookmarksProvider(
      Bus bus, SharedPreferences pref, Set<File> defaultsBookmarks) {
    BookmarksProvider.register(bus, pref, defaultsBookmarks);
  }

  /**
   * Registers a provide to handle view configurations.
   * <p/>
   * After registration, the following event will be handled if they are posted
   * to the bus: {@link SortRequest}, {@link ShowHiddenFilesRequest}.
   * <p/>
   * And the following event will be posted to the bus when the preference
   * changes: {@link ViewOptionsEvent}.
   */
  public static void registerViewOptionsProvider(Bus bus, SharedPreferences pref) {
    ViewOptionsProvider.register(bus, pref);
  }
}
