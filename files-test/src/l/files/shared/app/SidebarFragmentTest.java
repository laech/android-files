package l.files.shared.app;

import static java.lang.System.nanoTime;
import static java.util.Collections.singleton;
import static l.files.shared.test.Preferences.newPreferences;
import static l.files.shared.test.Preferences.newSettings;
import static l.files.shared.test.TempDirectory.newTempDirectory;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.util.Set;

import l.files.shared.R;
import l.files.shared.app.FilesAdapter;
import l.files.shared.app.Settings;
import l.files.shared.app.SidebarFragment;
import l.files.shared.event.EventBus;
import l.files.shared.event.FileSelectedEvent;
import l.files.shared.test.TempDirectory;
import l.files.shared.test.TestSidebarFragmentActivity;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public final class SidebarFragmentTest
    extends ActivityInstrumentationTestCase2<TestSidebarFragmentActivity> {

  private TempDirectory directory;

  public SidebarFragmentTest() {
    super(TestSidebarFragmentActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    directory = newTempDirectory();
  }

  @Override protected void tearDown() throws Exception {
    directory.delete();
    super.tearDown();
  }

  public void testBusIsNotifiedOnFileSelection() throws Throwable {
    final File file = new File("/");
    EventBus bus = getActivity().getFragment().bus = mock(EventBus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getAdapter().clear();
        getAdapter().add(file);
        getListView().performItemClick(null, 0, 0);
      }
    });

    verify(bus).post(new FileSelectedEvent(file));
  }

  public void testBusIsNotNotifiedOnNonFileSelection() throws Throwable {
    EventBus bus = getActivity().getFragment().bus = mock(EventBus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getAdapter().clear();
        getAdapter().add("hello");
        getListView().performItemClick(null, 0, 0);
      }
    });

    verifyZeroInteractions(bus);
  }

  public void testPreferenceListenerIsRegisteredOnResume() throws Throwable {
    testPreferenceListener(true);
  }

  public void testPreferenceListenerIsUnregisteredOnPause() throws Throwable {
    testPreferenceListener(false);
  }

  private SharedPreferences mockFragmentSettings() {
    SharedPreferences pref = newPreferences(getActivity());
    getActivity().getFragment().settings =
        newSettings(getActivity().getApplication(), pref);
    return pref;
  }

  private void testPreferenceListener(final boolean register) throws Throwable {
    final SharedPreferences pf = mock(SharedPreferences.class);
    Settings settings = mock(Settings.class);
    given(settings.getPreferences()).willReturn(pf);
    getFragment().settings = settings;

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        if (register) {
          getFragment().onResume();
        } else {
          getFragment().onPause();
        }
      }
    });

    if (register) {
      verify(pf).registerOnSharedPreferenceChangeListener(getFragment());
    } else {
      verify(pf).unregisterOnSharedPreferenceChangeListener(getFragment());
    }
  }

  public void testRefreshCalledOnResumeIfFavoritesChanged() throws Exception {
    SharedPreferences preferences = mockFragmentSettings();
    updateFavoritesTimestamp(preferences);
    getFragment().adapter = mock(FilesAdapter.class);

    getFragment().onResume();

    verify(getFragment().adapter).notifyDataSetChanged();
  }

  private void updateFavoritesTimestamp(SharedPreferences preferences) {
    String key = getActivity().getString(
        R.string.pref_favorites_updated_timestamp);
    preferences
        .edit()
        .putLong(key, nanoTime())
        .commit();
  }

  public void testRefreshNotCalledOnResumeIfFavoritesNotChanged()
      throws Throwable {
    getInstrumentation().callActivityOnPause(getActivity());
    getFragment().adapter = mock(FilesAdapter.class);

    getFragment().onResume();

    verifyZeroInteractions(getFragment().adapter);
  }

  public void testRefreshesOnFavoritesChange() throws Throwable {
    getFragment().adapter = mock(FilesAdapter.class);

    final SharedPreferences preferences = mockFragmentSettings();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getFragment().onSharedPreferenceChanged(preferences,
            getActivity().getString(R.string.pref_favorites_updated_timestamp));
        verify(getFragment().adapter).notifyDataSetChanged();
      }
    });
  }

  public void testExcludesFilesFromSidebar() throws Throwable {
    final File file = directory.newFile();
    SharedPreferences preferences = mockFragmentSettings();
    addFavorite(preferences, file);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getFragment().refresh();
        assertAdapterDoesNotContain(file);
      }
    });
  }

  public void testExcludesNonExistentDirectoryFromSidebar() throws Throwable {
    final File dir = directory.newDirectory();
    SharedPreferences preferences = mockFragmentSettings();
    assertTrue(dir.delete());
    addFavorite(preferences, dir);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getFragment().refresh();
        assertAdapterDoesNotContain(dir);
      }
    });
  }

  public void testExcludesUnreadableDirectoriesFromSidebar() throws Throwable {
    final File dir = directory.newDirectory();
    SharedPreferences preferences = mockFragmentSettings();
    dir.setReadable(false, false);
    dir.setExecutable(false, false);
    addFavorite(preferences, dir);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getFragment().refresh();
        assertAdapterDoesNotContain(dir);
      }
    });
  }

  public void testIncludesFavoriteInSidebar() throws Throwable {
    final File dir = directory.newDirectory();
    SharedPreferences preferences = mockFragmentSettings();
    addFavorite(preferences, dir);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getFragment().refresh();
        assertAdapterContains(dir);
      }
    });
  }

  private void assertAdapterContains(File file) {
    for (int i = 0; i < getAdapter().getCount(); i++)
      if (file.equals(getAdapter().getItem(i))) return;

    fail();
  }

  private void assertAdapterDoesNotContain(File file) {
    for (int i = 0; i < getAdapter().getCount(); i++) {
      assertFalse(file.equals(getAdapter().getItem(i)));
    }
  }

  private void addFavorite(final SharedPreferences preferences, File file)
      throws Throwable {
    final String key = getActivity().getString(R.string.pref_favorites);
    final Set<String> favorites = singleton(file.getAbsolutePath());
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        preferences
            .edit()
            .putStringSet(key, favorites)
            .commit();
      }
    });
  }

  private SidebarFragment getFragment() {
    return getActivity().getFragment();
  }

  private ListView getListView() {
    return getFragment().getListView();
  }

  private FilesAdapter getAdapter() {
    return getFragment().adapter;
  }
}
