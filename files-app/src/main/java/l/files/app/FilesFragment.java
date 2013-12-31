package l.files.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import l.files.R;
import l.files.app.menu.BookmarkMenu;
import l.files.app.menu.NewDirMenu;
import l.files.app.menu.PasteMenu;
import l.files.app.menu.ShowHiddenFilesMenu;
import l.files.app.menu.SortMenu;
import l.files.app.mode.CopyAction;
import l.files.app.mode.CountSelectedItemsAction;
import l.files.app.mode.CutAction;
import l.files.app.mode.DeleteAction;
import l.files.app.mode.RenameAction;
import l.files.app.mode.SelectAllAction;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceModeListeners;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.lang.System.identityHashCode;
import static l.files.app.Animations.animatePreDataSetChange;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.buildFileChildrenUri;

public final class FilesFragment extends BaseFileListFragment
    implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIRECTORY_LOCATION = "directory_location";

  private static final int LOADER_ID = identityHashCode(FilesFragment.class);

  private String directoryLocation;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  /**
   * Creates a fragment to show the contents under the directory's {@link
   * FileInfo#LOCATION}.
   */
  public static FilesFragment create(String directoryLocation) {
    return Fragments.setArgs(
        new FilesFragment(),
        ARG_DIRECTORY_LOCATION,
        directoryLocation);
  }

  /**
   * Gets the {@link FileInfo#LOCATION} of the directory that this fragment is
   * currently showing.
   */
  public String getDirectoryLocation() {
    return directoryLocation;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    directoryLocation = getArguments().getString(ARG_DIRECTORY_LOCATION);

    setupListView();
    setupOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));

    getLoaderManager().initLoader(LOADER_ID, null, this);
    Preferences.register(getActivity(), this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Preferences.unregister(getActivity(), this);
  }

  @Override public FilesAdapter getListAdapter() {
    return (FilesAdapter) super.getListAdapter();
  }

  private void setupOptionsMenu() {
    Activity context = getActivity();
    setOptionsMenu(OptionsMenus.compose(
        BookmarkMenu.create(context, directoryLocation),
        NewDirMenu.create(context, directoryLocation),
        PasteMenu.create(context, directoryLocation),
        SortMenu.create(context),
        ShowHiddenFilesMenu.create(context)
    ));
  }

  private void setupListView() {
    ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    FragmentManager fragmentManager = getActivityFragmentManager();
    list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
        CountSelectedItemsAction.create(list),
        SelectAllAction.create(list),
        CutAction.create(list),
        CopyAction.create(list),
        DeleteAction.create(list),
        RenameAction.create(list, fragmentManager, directoryLocation)
    ));
  }

  private FragmentManager getActivityFragmentManager() {
    return getActivity().getFragmentManager();
  }

  /*
   * Loading files from a large directory (e.g. /storage/emulated/0/DCIM/.thumbnails
   * with ~20,000 files) will be an expensive and long running operation, by
   * using the CursorLoader (the one from Android SDK, not the one from
   * support-v4) and implementing CancellationSignal support in the
   * ContentProvider, the load is cancelled as soon as the user no longer wants
   * to wait anymore and leaves (by pressing the back button).
   * <p/>
   * This can be tested by enabling the "Show CPU usage" option in "Developer
   * options" on the device, and navigate to the large directory, something like
   * "sdcard" will jump to the top of the CPU usage list, while still loading,
   * hit the back button, "sdcard" should disappear and CPU usage should return
   * to normal - as the loading operation should have been cancelled.
   */
  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    boolean showHidden = Preferences.getShowHiddenFiles(getActivity());
    String sortOrder = Preferences.getSortOrder(getActivity());
    Uri uri = buildFileChildrenUri(directoryLocation, showHidden);
    return new CursorLoader(getActivity(), uri, null, null, null, sortOrder);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (getListAdapter().getCursor() != null && isResumed()) {
      animatePreDataSetChange(getListView());
    }
    getListAdapter().setCursor(cursor, Preferences.getSortOrder(getActivity()));
    if (cursor.getCount() == 0) {
      overrideEmptyText(R.string.empty);
    }
  }

  private void overrideEmptyText(int resId) {
    View root = getView();
    if (root != null) {
      ((TextView) root.findViewById(android.R.id.empty)).setText(resId);
    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {
    getListAdapter().setCursor(null);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (Preferences.isSortOrderKey(key) ||
        Preferences.isShowHiddenFilesKey(key)) {
      getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
  }
}
