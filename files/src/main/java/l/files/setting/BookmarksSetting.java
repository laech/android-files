package l.files.setting;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.singleton;
import static l.files.ui.UserDirs.DIR_DCIM;
import static l.files.ui.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.UserDirs.DIR_MOVIES;
import static l.files.ui.UserDirs.DIR_MUSIC;
import static l.files.ui.UserDirs.DIR_PICTURES;

import java.io.File;
import java.util.Set;

import android.content.SharedPreferences;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

final class BookmarksSetting implements SetSetting<File> {

  private static final Set<String> DEFAULTS = ImmutableSet.of(
      getPath(DIR_DCIM),
      getPath(DIR_DOWNLOADS),
      getPath(DIR_MOVIES),
      getPath(DIR_MUSIC),
      getPath(DIR_PICTURES));

  private static String getPath(File file) {
    return file.getAbsolutePath();
  }

  private final SharedPreferences pref;

  public BookmarksSetting(SharedPreferences pref) {
    this.pref = checkNotNull(pref, "pref");
  }

  @Override public String key() {
    return "bookmarks";
  }

  @Override public Set<File> get() {
    return ImmutableSet.copyOf(toFiles(getPaths()));
  }

  @Override public void set(Set<File> value) {
    save(newHashSet(toPaths(value)));
  }

  @Override public void add(File file) {
    save(union(getPaths(), singleton(getPath(file))));
  }

  @Override public void remove(File file) {
    save(difference(getPaths(), singleton(getPath(file))));
  }

  @Override public boolean contains(File file) {
    return getPaths().contains(getPath(file));
  }

  private void save(Set<String> paths) {
    pref.edit()
        .putStringSet(key(), paths)
        .apply();
  }

  private Set<String> getPaths() {
    return pref.getStringSet(key(), DEFAULTS);
  }

  private Iterable<String> toPaths(Iterable<File> value) {
    return transform(value, new Function<File, String>() {
      @Override public String apply(File file) {
        return getPath(file);
      }
    });
  }

  private Iterable<File> toFiles(Iterable<String> paths) {
    Iterable<File> files = transform(paths, new Function<String, File>() {
      @Override public File apply(String path) {
        File file = new File(path);
        return (file.exists() && file.canRead()) ? file : null;
      }
    });
    return filter(files, notNull());
  }

}
