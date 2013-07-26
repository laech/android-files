package l.files;

import android.content.SharedPreferences;
import com.google.common.base.Function;
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
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.singleton;
import static l.files.io.FilePredicates.canRead;
import static l.files.io.FilePredicates.exists;
import static l.files.ui.UserDirs.*;

final class BookmarkHandler
    implements Supplier<BookmarksEvent>, OnSharedPreferenceChangeListener {

  private static final String KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.of(
      toPath(DIR_DCIM),
      toPath(DIR_DOWNLOADS),
      toPath(DIR_MOVIES),
      toPath(DIR_MUSIC),
      toPath(DIR_PICTURES));

  public static BookmarkHandler register(Bus bus, SharedPreferences pref) {
    BookmarkHandler handler = new BookmarkHandler(bus, pref);
    pref.registerOnSharedPreferenceChangeListener(handler);
    bus.register(handler);
    return handler;
  }

  private static String toPath(File file) {
    return file.getAbsolutePath();
  }

  private static Iterable<File> toFiles(Iterable<String> paths) {
    Iterable<File> files = transform(paths, new Function<String, File>() {
      @Override public File apply(String path) {
        return new File(path);
      }
    });
    return filter(files, and(canRead(), exists()));
  }

  private final SharedPreferences pref;
  private final Bus bus;

  private BookmarkHandler(Bus bus, SharedPreferences pref) {
    this.bus = checkNotNull(bus, "bus");
    this.pref = checkNotNull(pref, "pref");
  }

  @Subscribe public void handle(AddBookmarkRequest request) {
    save(union(getBookmarkFilePaths(), singleton(toPath(request.file()))));
  }

  @Subscribe public void handle(RemoveBookmarkRequest request) {
    save(difference(getBookmarkFilePaths(), singleton(toPath(request.file()))));
  }

  @Override @Produce public BookmarksEvent get() {
    return new BookmarksEvent(getBookmarkFiles());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (KEY.equals(key)) bus.post(get());
  }

  private Set<File> getBookmarkFiles() {
    return ImmutableSet.copyOf(toFiles(getBookmarkFilePaths()));
  }

  private Set<String> getBookmarkFilePaths() {
    return ImmutableSet.copyOf(pref.getStringSet(KEY, DEFAULTS));
  }

  private void save(Set<String> paths) {
    pref.edit()
        .putStringSet(KEY, paths)
        .apply();
  }

}
