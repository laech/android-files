package l.files.provider;

import android.content.SharedPreferences;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static l.files.provider.FilesContract.toFileId;
import static l.files.provider.FilesContract.toURI;

final class Bookmarks {

  private static final String KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.<String>builder()
      .add(toId(getExternalStorageDirectory()))
      .add(toId(DIRECTORY_DCIM))
      .add(toId(DIRECTORY_MUSIC))
      .add(toId(DIRECTORY_MOVIES))
      .add(toId(DIRECTORY_PICTURES))
      .add(toId(DIRECTORY_DOWNLOADS))
      .build();

  private static String toId(String name) {
    return toId(new File(getExternalStorageDirectory(), name));
  }

  private static String toId(File file) {
    return toFileId(file);
  }

  static File[] getBookmark(SharedPreferences pref, String fileId) {
    Set<String> bookmarks = new HashSet<>(pref.getStringSet(KEY, DEFAULTS));
    bookmarks.retainAll(asList(fileId));
    return toFiles(bookmarks);
  }

  static File[] getBookmarks(SharedPreferences pref) {
    Set<String> fileIds = pref.getStringSet(KEY, DEFAULTS);
    return toFiles(fileIds);
  }

  private static File[] toFiles(Set<String> fileIds) {
    List<File> bookmarks = new ArrayList<>(fileIds.size());
    for (String fileId : fileIds) {
      try {
        bookmarks.add(new File(toURI(fileId)));
      } catch (IllegalArgumentException ignored) {
      }
    }
    sort(bookmarks, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
    return bookmarks.toArray(new File[bookmarks.size()]);
  }

  static void add(SharedPreferences pref, String fileId) {
    addOrRemove(pref, fileId, true);
  }

  static void remove(SharedPreferences pref, String fileId) {
    addOrRemove(pref, fileId, false);
  }

  static void addOrRemove(SharedPreferences pref, String fileId, boolean add) {
    Set<String> bookmarks;
    do {
      bookmarks = new HashSet<>(pref.getStringSet(KEY, DEFAULTS));
      if (add) bookmarks.add(fileId);
      else bookmarks.remove(fileId);
    } while (!pref.edit().putStringSet(KEY, bookmarks).commit());
  }
}
