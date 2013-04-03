package com.example.files.app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.Preferences.countDownOnChange;
import static com.example.files.test.Preferences.newPreferences;
import static com.example.files.test.Preferences.newSettings;
import static com.example.files.test.TempDirectory.newTempDirectory;
import static com.example.files.test.TestFilesFragmentActivity.DIRECTORY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.event.EventBus;
import com.example.files.event.FileSelectedEvent;
import com.example.files.test.TempDirectory;
import com.example.files.test.TestFilesFragmentActivity;

public final class FilesFragmentTest
    extends ActivityInstrumentationTestCase2<TestFilesFragmentActivity> {

  private TempDirectory directory;

  public FilesFragmentTest() {
    super(TestFilesFragmentActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    directory = newTempDirectory();
    setTestIntent(directory.get());
  }

  @Override protected void tearDown() throws Exception {
    directory.delete();
    super.tearDown();
  }

  public void testFavoritesMenuItemIsCheckedIfDirectoryIsFavorite() {
    testFavoriteMenuItemChecked(true);
  }

  public void testFavoritesMenuItemIsUncheckedIfDirectoryIsNotFavorite() {
    testFavoriteMenuItemChecked(false);
  }

  private void testFavoriteMenuItemChecked(boolean checked) {
    Settings settings = mock(Settings.class);
    given(settings.isFavorite(directory.get())).willReturn(checked);
    getActivity().getFragment().settings = settings;

    MenuItem item = mock(MenuItem.class);
    Menu menu = mock(Menu.class);
    given(menu.findItem(R.id.favorite)).willReturn(item);

    getActivity().getFragment().onPrepareOptionsMenu(menu);

    verify(item).setChecked(checked);
  }

  public void testFavoritesMenuItemIsOptional() {
    Menu menu = mock(Menu.class);
    given(menu.findItem(R.id.favorite)).willReturn(null);
    getActivity().getFragment().onPrepareOptionsMenu(menu);
    // No crash
  }

  public void testFavoriteCanBeAdded() throws Exception {
    testFavorite(true);
  }

  public void testFavoriteCanBeRemoved() throws Exception {
    testFavorite(false);
  }

  private void testFavorite(boolean add) throws Exception {
    SharedPreferences pref = mockFragmentSettings();
    CountDownLatch latch = countDownOnChange(pref);
    MenuItem item = mockCheckedMenuItem(!add, R.id.favorite);

    getActivity().getFragment().onOptionsItemSelected(item);
    latch.await(2, SECONDS);

    String expected = directory.get().getAbsolutePath();
    Set<String> actuals = getSet(pref, R.string.pref_favorites);
    if (add) {
      assertTrue(actuals.contains(expected));
    } else {
      assertFalse(actuals.contains(expected));
    }
  }

  private SharedPreferences mockFragmentSettings() {
    SharedPreferences pref = newPreferences(getActivity());
    getActivity().getFragment().settings =
        newSettings(getActivity().getApplication(), pref);
    return pref;
  }

  public void testHiddenFilesAreNotShownByDefault() {
    mockFragmentSettings();
    directory.newFile(".abc");
    assertEquals(0, getListView().getCount());
  }

  public void testHiddenFilesAreHiddenWhenSettingsSaySo() throws Throwable {
    directory.newFile(".abc");
    Settings s = getActivity().getFragment().settings = mock(Settings.class);
    given(s.shouldShowHiddenFiles()).willReturn(false);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getActivity().getFragment().checkShowHiddenFilesPreference();
      }
    });

    assertEquals(0, getListView().getCount());
  }

  public void testHiddenFilesAreShownWhenSettingsSaySo() throws Throwable {
    directory.newFile(".def");
    Settings s = getActivity().getFragment().settings = mock(Settings.class);
    given(s.shouldShowHiddenFiles()).willReturn(true);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getActivity().getFragment().checkShowHiddenFilesPreference();
      }
    });

    assertEquals(1, getListView().getCount());
  }

  public void testSortsFilesByName() {
    File z = directory.newFile("z");
    File a = directory.newFile("a");
    File c = directory.newDirectory("C");

    ListView list = getListView();
    assertEquals(a, list.getItemAtPosition(0));
    assertEquals(c, list.getItemAtPosition(1));
    assertEquals(z, list.getItemAtPosition(2));
  }

  public void testShowsCorrectNumSelectedItemsOnRotation() throws Throwable {
    directory.newFile();

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getListView().setItemChecked(0, true);
        rotate(getActivity());
      }
    });

    assertEquals(
        getString(R.string.n_selected, 1),
        getActivity().getActionMode().getTitle());
  }

  public void testShowsCorrectNumSelectedItemsOnSelection() throws Throwable {
    directory.newFile();
    directory.newFile();

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getListView().setItemChecked(0, true);
        getListView().setItemChecked(1, true);
      }
    });

    assertEquals(
        getString(R.string.n_selected, 2),
        getActivity().getActionMode().getTitle());
  }

  public void testHidesEmptyViewIfDirectoryHasFile() throws Exception {
    directory.newFile();
    assertEmptyViewIsNotVisible();
  }

  public void testPostsEventOnItemClick() throws Throwable {
    final File file = directory.newFile();
    EventBus bus = getActivity().getFragment().bus = mock(EventBus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        clickFirstListItem();
      }
    });

    verify(bus).post(new FileSelectedEvent(file));
  }

  public void testShowsEmptyListViewIfDirectoryHasNoFile() {
    assertEquals(0, getListView().getChildCount());
  }

  public void testShowsEmptyMessageIfNoFiles() {
    assertEmptyViewIsVisible(R.string.empty);
  }

  public void testShowsDirectoryNotExistsIfDirectoryDoesNotExist() {
    directory.delete();
    assertEmptyViewIsVisible(R.string.directory_doesnt_exist);
  }

  public void testShowsNotDirectoryMessageIfArgIsNotDirectory() {
    setTestIntent(directory.newFile());
    assertEmptyViewIsVisible(R.string.not_a_directory);
  }

  private void assertEmptyViewIsNotVisible() {
    assertEquals(GONE, getEmptyView().getVisibility());
  }

  private void assertEmptyViewIsVisible(int msgId) {
    assertEquals(VISIBLE, getEmptyView().getVisibility());
    assertEquals(getString(msgId), getEmptyView().getText().toString());
  }

  private void clickFirstListItem() {
    assertTrue(getListView()
        .performItemClick(getListView().getChildAt(0), 0, 0));
  }

  private TextView getEmptyView() {
    return (TextView) getActivity().findViewById(android.R.id.empty);
  }

  private FilesFragment getFragment() {
    return getActivity().getFragment();
  }

  private String getString(int resId) {
    return getActivity().getString(resId);
  }

  private String getString(int resId, Object... args) {
    return getActivity().getString(resId, args);
  }

  private ListView getListView() {
    return getFragment().getListView();
  }

  private void setTestIntent(File directory) {
    setActivityIntent(new Intent()
        .putExtra(DIRECTORY, directory.getAbsolutePath()));
  }

  private MenuItem mockCheckedMenuItem(boolean checked, int id) {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(id);
    given(item.isChecked()).willReturn(checked);
    return item;
  }

  private Set<String> getSet(SharedPreferences pref, int key) {
    return pref.getStringSet(getString(key), Collections.<String>emptySet());
  }
}
