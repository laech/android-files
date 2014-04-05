package l.files.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

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
import l.files.provider.event.LoadFinished;
import l.files.provider.event.LoadProgress;
import l.files.provider.event.LoadStarted;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.lang.System.identityHashCode;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.app.Animations.animatePreDataSetChange;
import static l.files.app.Preferences.isShowHiddenFilesKey;
import static l.files.app.Preferences.isSortOrderKey;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.buildFileChildrenUri;

public final class FilesFragment extends BaseFileListFragment
    implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIRECTORY_LOCATION = "directory_location";

  private static final int LOADER_ID = identityHashCode(FilesFragment.class);

  private String directoryLocation;
  private ProgressBar progress;
  private Handler handler;
  private Runnable showProgressRunnable;

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
    progress = (ProgressBar) getView().findViewById(android.R.id.progress);
    handler = new Handler();
    showProgressRunnable = new Runnable() {
      @Override public void run() {
        progress.setVisibility(VISIBLE);
      }
    };

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

  @Override public void onStart() {
    super.onStart();
    getBus().register(this);
    progress.setVisibility(GONE);
  }

  @Override public void onStop() {
    getBus().unregister(this);
    super.onStop();
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

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Activity context = getActivity();
    boolean showHidden = Preferences.getShowHiddenFiles(context);
    String sortOrder = Preferences.getSortOrder(context);
    Uri uri = buildFileChildrenUri(context, getDirectoryLocation(), showHidden);
    return new CursorLoader(context, uri, null, null, null, sortOrder);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (getActivity() != null) {
      if (getListAdapter().getCursor() != null && isResumed()) {
        animatePreDataSetChange(getListView());
      }
      getListAdapter().setCursor(cursor, Preferences.getSortOrder(getActivity()));
      if (cursor.getCount() == 0) {
        overrideEmptyText(R.string.empty);
      }
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
    if (isSortOrderKey(key) || isShowHiddenFilesKey(key)) {
      getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
  }

  @Subscribe public void handle(LoadStarted event) {
    if (event.getUri().equals(getLoaderUri())) {
      handler.postDelayed(showProgressRunnable, SECONDS.toMillis(1));
      progress.setIndeterminate(true);
    }
  }

  @Subscribe public void handle(LoadProgress event) {
    if (event.getUri().equals(getLoaderUri())) {
      progress.setVisibility(VISIBLE);
      progress.setIndeterminate(false);
      progress.setMax(event.getTotalChildrenCount());
      progress.setProgress(event.getNumChildrenLoaded());
    }
  }

  @Subscribe public void handle(LoadFinished event) {
    if (event.getUri().equals(getLoaderUri())) {
      handler.removeCallbacks(showProgressRunnable);
      progress.setVisibility(GONE);
    }
  }

  private Uri getLoaderUri() {
    Loader<Cursor> loader = getLoaderManager().getLoader(LOADER_ID);
    if (loader != null) {
      return ((CursorLoader) loader).getUri();
    }
    return null;
  }
}
