package l.files.ui.app.settings;

import android.app.Application;
import android.test.AndroidTestCase;
import l.files.R;
import l.files.Settings;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.test.Preferences.countDownOnChange;
import static l.files.test.Preferences.newPreferences;

public final class SettingsTest extends AndroidTestCase {

  private Settings settings;

  @Override protected void setUp() throws Exception {
    super.setUp();
    settings = new Settings(getApplication(), newPreferences(getApplication()));
  }

  public void testBookmarksUpdatedTimestampStaysSameOnAddingDuplicate() {
    File file = new File("/");
    addBookmarkToUnderlyingPreferences(file);

    long old = settings.getFavoritesUpdatedTimestamp();
    settings.addFavorite(file);
    long updated = settings.getFavoritesUpdatedTimestamp();

    assertEquals(old, updated);
  }

  public void testBookmarksUpdatedTimestampChangesOnRemovingFavoriteThatDoesNotExist() {
    long old = settings.getFavoritesUpdatedTimestamp();
    settings.removeFavorite(new File("/no_such_favorite"));
    long updated = settings.getFavoritesUpdatedTimestamp();
    assertEquals(old, updated);
  }

  public void testBookmarksUpdatedTimestampChangesOnAddingFavorite() {
    long old = settings.getFavoritesUpdatedTimestamp();
    settings.addFavorite(new File("/"));
    long updated = settings.getFavoritesUpdatedTimestamp();
    assertFalse(old == updated);
  }

  public void testBookmarksUpdatedTimestampChangesOnRemovingFavorite() {
    File file = new File("/");
    addBookmarkToUnderlyingPreferences(file);

    long old = settings.getFavoritesUpdatedTimestamp();
    settings.removeFavorite(file);
    long updated = settings.getFavoritesUpdatedTimestamp();

    assertFalse(old == updated);
  }

  public void testBookmarkCanBeAdded() throws Exception {
    File expected = new File("/abc");
    CountDownLatch latch = countDownOnChange(newPreferences(getApplication()));

    settings.addFavorite(expected);

    latch.await(2, SECONDS);
    assertTrue(settings.isBookmark(expected));
  }

  public void testBookmarkCanBeRemoved() throws Exception {
    File expected = new File("/def");
    addBookmarkToUnderlyingPreferences(expected);
    CountDownLatch latch = countDownOnChange(settings.getPreferences());

    settings.removeFavorite(expected);

    latch.await(2, SECONDS);
    assertFalse(settings.isBookmark(expected));
  }

  private void addBookmarkToUnderlyingPreferences(File expected) {
    settings.getPreferences()
        .edit()
        .putStringSet(bookmarksKey(), newHashSet(expected.getAbsolutePath()))
        .commit();
  }

  private String bookmarksKey() {
    return getContext().getString(R.string.pref_bookmarks);
  }

  private Application getApplication() {
    return (Application) getContext().getApplicationContext();
  }
}
