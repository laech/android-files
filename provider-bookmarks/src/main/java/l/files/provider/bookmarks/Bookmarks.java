package l.files.provider.bookmarks;

import android.content.SharedPreferences;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static l.files.provider.FilesContract.getFileId;

final class Bookmarks {

  static final String KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.<String>builder()
      .add(getFileId(getExternalStorageDirectory()))
      .add(toLocation(DIRECTORY_DCIM))
      .add(toLocation(DIRECTORY_MUSIC))
      .add(toLocation(DIRECTORY_MOVIES))
      .add(toLocation(DIRECTORY_PICTURES))
      .add(toLocation(DIRECTORY_DOWNLOADS))
      .build();

  static boolean isBookmarksKey(String key) {
    return KEY.equals(key);
  }

  private static String toLocation(String name) {
    return getFileId(new File(getExternalStorageDirectory(), name));
  }

  static String[] bookmarks(SharedPreferences pref) {
    Set<String> set = pref.getStringSet(KEY, DEFAULTS);
    return set.toArray(new String[set.size()]);
  }

  static void add(SharedPreferences pref, String fileLocation) {
    addOrRemove(pref, fileLocation, true);
  }

  static void remove(SharedPreferences pref, String fileLocation) {
    addOrRemove(pref, fileLocation, false);
  }

  static void addOrRemove(SharedPreferences pref, String fileLocation, boolean add) {
    Set<String> bookmarks;
    do {
      bookmarks = new HashSet<>(pref.getStringSet(KEY, DEFAULTS));
      if (add) bookmarks.add(fileLocation);
      else bookmarks.remove(fileLocation);
    } while (!pref.edit().putStringSet(KEY, bookmarks).commit());
  }
}
