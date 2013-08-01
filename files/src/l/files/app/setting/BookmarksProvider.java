package l.files.app.setting;

import android.content.SharedPreferences;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.Set;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

final class BookmarksProvider
    implements Supplier<BookmarksEvent>, OnSharedPreferenceChangeListener {

  private static final String KEY = "bookmarks";

  static BookmarksProvider register(Bus bus, SharedPreferences pref, Set<File> defaults) {
    Set<String> paths = getAbsolutePaths(defaults);
    BookmarksProvider handler = new BookmarksProvider(bus, pref, paths);
    pref.registerOnSharedPreferenceChangeListener(handler);
    bus.register(handler);
    return handler;
  }

  private static Set<String> getAbsolutePaths(Set<File> files) {
    Set<String> paths = newHashSetWithExpectedSize(files.size());
    for (File file : files) {
      paths.add(file.getAbsolutePath());
    }
    return paths;
  }

  private final Set<String> defaults;
  private final SharedPreferences pref;
  private final Bus bus;

  private BookmarksProvider(Bus bus, SharedPreferences pref, Set<String> defaults) {
    this.bus = checkNotNull(bus, "bus");
    this.pref = checkNotNull(pref, "pref");
    this.defaults = ImmutableSet.copyOf(checkNotNull(defaults, "defaults"));
  }

  @Subscribe public void handle(AddBookmarkRequest request) {
    Set<String> paths = newHashSet(getBookmarkPaths());
    paths.add(request.file().getAbsolutePath());
    save(paths);
  }

  @Subscribe public void handle(RemoveBookmarkRequest request) {
    Set<String> paths = newHashSet(getBookmarkPaths());
    paths.remove(request.file().getAbsolutePath());
    save(paths);
  }

  @Override @Produce public BookmarksEvent get() {
    return new BookmarksEvent(toBookmarkFiles(getBookmarkPaths()));
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (KEY.equals(key)) bus.post(get());
  }

  private Set<File> toBookmarkFiles(Set<String> paths) {
    Set<File> files = newHashSetWithExpectedSize(paths.size());
    for (String path : paths) {
      File file = new File(path);
      if (file.canRead()) {
        files.add(file);
      }
    }
    return files;
  }

  private Set<String> getBookmarkPaths() {
    return pref.getStringSet(KEY, defaults);
  }

  private void save(Set<String> paths) {
    pref.edit()
        .putStringSet(KEY, paths)
        .apply();
  }
}
