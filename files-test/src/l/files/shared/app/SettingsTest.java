package l.files.shared.app;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.shared.test.Preferences.countDownOnChange;
import static l.files.shared.test.Preferences.newPreferences;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import l.files.shared.R;
import l.files.shared.app.Settings;

import android.app.Application;
import android.test.AndroidTestCase;

public final class SettingsTest extends AndroidTestCase {

  private Settings settings;

  @Override protected void setUp() throws Exception {
    super.setUp();
    settings = new Settings(getApplication(), newPreferences(getApplication()));
  }

  public void testFavoritesUpdatedTimestampStaysSameOnAddingDuplicate() {
    File file = new File("/");
    addFavoriteToUnderlyingPreferences(file);

    long old = settings.getFavoritesUpdatedTimestamp();
    settings.addFavorite(file);
    long updated = settings.getFavoritesUpdatedTimestamp();

    assertEquals(old, updated);
  }

  public void testFavoritesUpdatedTimestampChangesOnRemovingFavoriteThatDoesNotExist() {
    long old = settings.getFavoritesUpdatedTimestamp();
    settings.removeFavorite(new File("/no_such_favorite"));
    long updated = settings.getFavoritesUpdatedTimestamp();
    assertEquals(old, updated);
  }

  public void testFavoritesUpdatedTimestampChangesOnAddingFavorite() {
    long old = settings.getFavoritesUpdatedTimestamp();
    settings.addFavorite(new File("/"));
    long updated = settings.getFavoritesUpdatedTimestamp();
    assertFalse(old == updated);
  }

  public void testFavoritesUpdatedTimestampChangesOnRemovingFavorite() {
    File file = new File("/");
    addFavoriteToUnderlyingPreferences(file);

    long old = settings.getFavoritesUpdatedTimestamp();
    settings.removeFavorite(file);
    long updated = settings.getFavoritesUpdatedTimestamp();

    assertFalse(old == updated);
  }

  public void testFavoriteCanBeAdded() throws Exception {
    File expected = new File("/abc");
    CountDownLatch latch = countDownOnChange(newPreferences(getApplication()));

    settings.addFavorite(expected);

    latch.await(2, SECONDS);
    assertTrue(settings.isFavorite(expected));
  }

  public void testFavoriteCanBeRemoved() throws Exception {
    File expected = new File("/def");
    addFavoriteToUnderlyingPreferences(expected);
    CountDownLatch latch = countDownOnChange(settings.getPreferences());

    settings.removeFavorite(expected);

    latch.await(2, SECONDS);
    assertFalse(settings.isFavorite(expected));
  }

  private void addFavoriteToUnderlyingPreferences(File expected) {
    settings.getPreferences()
        .edit()
        .putStringSet(favoritesKey(), newHashSet(expected.getAbsolutePath()))
        .commit();
  }

  private String favoritesKey() {
    return getContext().getString(R.string.pref_favorites);
  }

  private Application getApplication() {
    return (Application) getContext().getApplicationContext();
  }
}
