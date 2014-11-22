package l.files.ui;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import l.files.R;
import l.files.ui.menu.BookmarkMenu;
import l.files.ui.menu.NewDirMenu;
import l.files.ui.menu.PasteMenu;
import l.files.ui.menu.ShowHiddenFilesMenu;
import l.files.ui.menu.SortMenu;
import l.files.ui.mode.CopyAction;
import l.files.ui.mode.CountSelectedItemsAction;
import l.files.ui.mode.CutAction;
import l.files.ui.mode.DeleteAction;
import l.files.ui.mode.RenameAction;
import l.files.ui.mode.SelectAllAction;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceModeListeners;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.view.View.GONE;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.lang.System.identityHashCode;
import static l.files.ui.Preferences.isShowHiddenFilesKey;
import static l.files.ui.Preferences.isSortOrderKey;
import static l.files.provider.FilesContract.getFilesUri;

public final class FilesFragment extends BaseFileListFragment
    implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

  // TODO implement progress

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIR_ID = "dir_id";

  private static final int LOADER_ID = identityHashCode(FilesFragment.class);

  private String dirId;
  private ProgressBar progress;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  /**
   * Creates a fragment to show the contents under the directory's ID.
   */
  public static FilesFragment create(String dirId) {
    return Fragments.setArgs(new FilesFragment(), ARG_DIR_ID, dirId);
  }

  /**
   * Gets the ID of the directory that this fragment is currently showing.
   */
  public String getDirectoryId() {
    return dirId;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    dirId = getArguments().getString(ARG_DIR_ID);
    progress = (ProgressBar) getView().findViewById(android.R.id.progress);

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
    progress.setVisibility(GONE);
  }

  @Override public FilesAdapter getListAdapter() {
    return (FilesAdapter) super.getListAdapter();
  }

  private void setupOptionsMenu() {
    Activity context = getActivity();
    setOptionsMenu(OptionsMenus.compose(
        BookmarkMenu.create(context, dirId),
        NewDirMenu.create(context, dirId),
        PasteMenu.create(context, dirId),
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
        RenameAction.create(list, fragmentManager, dirId)
    ));
  }

  private FragmentManager getActivityFragmentManager() {
    return getActivity().getFragmentManager();
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Activity context = getActivity();
    boolean showHidden = Preferences.getShowHiddenFiles(context);
    String sortOrder = Preferences.getSortOrder(context);
    Uri uri = getFilesUri(context, getDirectoryId(), showHidden);
    return new CursorLoader(context, uri, null, null, null, sortOrder);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (getActivity() != null && !getActivity().isFinishing()) {
      if (getListAdapter().getCursor() != null && isResumed()) {
        Animations.animatePreDataSetChange(getListView());
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
}