package l.files.event;

import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

final class BookmarksProvider extends SettingProvider<BookmarksSetting> {

  private final Set<String> defaults;

  BookmarksProvider(Set<String> defaults) {
    super("bookmarks");
    this.defaults = ImmutableSet.copyOf(defaults);
  }

  @Override @Produce public BookmarksSetting get() {
    return new BookmarksSetting(toBookmarkFiles(getBookmarkPaths()));
  }

  @Subscribe public void handle(AddBookmarkRequest request) {
    Set<String> paths = newHashSet(getBookmarkPaths());
    paths.add(request.value().getAbsolutePath());
    save(paths);
  }

  @Subscribe public void handle(RemoveBookmarkRequest request) {
    Set<String> paths = newHashSet(getBookmarkPaths());
    paths.remove(request.value().getAbsolutePath());
    save(paths);
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
    return pref.getStringSet(key, defaults);
  }

  private void save(Set<String> paths) {
    pref.edit()
        .putStringSet(key, paths)
        .apply();
  }
}
