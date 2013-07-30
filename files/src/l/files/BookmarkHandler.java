package l.files;

import android.content.SharedPreferences;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import l.files.event.AddBookmarkRequest;
import l.files.event.BookmarksEvent;
import l.files.event.RemoveBookmarkRequest;

import java.io.File;
import java.util.Set;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static l.files.io.UserDirs.*;

final class BookmarkHandler
    implements Supplier<BookmarksEvent>, OnSharedPreferenceChangeListener {

  private static final String KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.of(
      DIR_DCIM.getAbsolutePath(),
      DIR_MUSIC.getAbsolutePath(),
      DIR_MOVIES.getAbsolutePath(),
      DIR_PICTURES.getAbsolutePath(),
      DIR_DOWNLOADS.getAbsolutePath());

  static BookmarkHandler register(Bus bus, SharedPreferences pref) {
    BookmarkHandler handler = new BookmarkHandler(bus, pref);
    pref.registerOnSharedPreferenceChangeListener(handler);
    bus.register(handler);
    return handler;
  }

  private final SharedPreferences pref;
  private final Bus bus;

  private BookmarkHandler(Bus bus, SharedPreferences pref) {
    this.bus = checkNotNull(bus, "bus");
    this.pref = checkNotNull(pref, "pref");
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
    return pref.getStringSet(KEY, DEFAULTS);
  }

  private void save(Set<String> paths) {
    pref.edit()
        .putStringSet(KEY, paths)
        .apply();
  }
}
