package l.files.provider;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.getFileLocation;

/**
 * Using a {@link SharedPreferences} for saving bookmarks.
 */
final class Bookmarks {

  private static final String TAG = Bookmarks.class.getSimpleName();

  private static final String KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.<String>builder()
      .add(getFileLocation(getExternalStorageDirectory()))
      .add(toLocation(DIRECTORY_DCIM))
      .add(toLocation(DIRECTORY_MUSIC))
      .add(toLocation(DIRECTORY_MOVIES))
      .add(toLocation(DIRECTORY_PICTURES))
      .add(toLocation(DIRECTORY_DOWNLOADS))
      .build();

  public static boolean isBookmarksKey(String key) {
    return KEY.equals(key);
  }

  private static String toLocation(String name) {
    return getFileLocation(new File(getExternalStorageDirectory(), name));
  }

  static File[] getBookmark(SharedPreferences pref, String fileLocation) {
    Set<String> bookmarks = newHashSet(pref.getStringSet(KEY, DEFAULTS));
    bookmarks.retainAll(asList(fileLocation));
    return toFiles(bookmarks);
  }

  static File[] getBookmarks(SharedPreferences pref) {
    Set<String> fileLocations = pref.getStringSet(KEY, DEFAULTS);
    return toFiles(fileLocations);
  }

  public static int getBookmarksCount(SharedPreferences pref) {
    return pref.getStringSet(KEY, DEFAULTS).size();
  }

  private static File[] toFiles(Set<String> locations) {
    List<File> bookmarks = newArrayListWithExpectedSize(locations.size());
    for (String location : locations) {
      try {
        bookmarks.add(new File(new URI(location)));
      } catch (URISyntaxException e) {
        Log.w(TAG, e);
      }
    }
    sort(bookmarks, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
    return bookmarks.toArray(new File[bookmarks.size()]);
  }

  /**
   * Bookmarks the given {@link FileInfo#LOCATION}.
   */
  static void add(SharedPreferences pref, String fileLocation) {
    addOrRemove(pref, fileLocation, true);
  }

  /**
   * Unbookmarks the given {@link FileInfo#LOCATION}.
   */
  static void remove(SharedPreferences pref, String fileLocation) {
    addOrRemove(pref, fileLocation, false);
  }

  static void addOrRemove(
      SharedPreferences pref, String fileLocation, boolean add) {
    Set<String> bookmarks;
    do {
      bookmarks = new HashSet<>(pref.getStringSet(KEY, DEFAULTS));
      if (add) bookmarks.add(fileLocation);
      else bookmarks.remove(fileLocation);
    } while (!pref.edit().putStringSet(KEY, bookmarks).commit());
  }
}
