package l.files.app;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import l.files.R;
import l.files.analytics.Analytics;
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

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.lang.System.identityHashCode;
import static l.files.app.Animations.animatePreDataSetChange;
import static l.files.provider.FilesContract.buildFileChildrenUri;

public final class FilesFragment extends BaseFileListFragment
    implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIRECTORY_ID = "directory_id";

  private static final int LOADER_ID = identityHashCode(FilesFragment.class);

  private String directoryId;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  public static FilesFragment create(String directoryId) {
    return Fragments.setArgs(new FilesFragment(), ARG_DIRECTORY_ID,
        directoryId);
  }

  public String getDirectoryId() {
    return directoryId;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    directoryId = getArguments().getString(ARG_DIRECTORY_ID);

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

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Analytics.onEvent(getActivity(), "files", "click");
  }

  private void setupOptionsMenu() {
    FragmentActivity context = getActivity();
    setOptionsMenu(OptionsMenus.compose(
        BookmarkMenu.create(context, directoryId),
        NewDirMenu.create(context, directoryId),
        PasteMenu.create(context, directoryId),
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
        RenameAction.create(list, fragmentManager, directoryId)
    ));
  }

  private FragmentManager getActivityFragmentManager() {
    return getActivity().getSupportFragmentManager();
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    boolean showHidden = Preferences.getShowHiddenFiles(getActivity());
    String sortOrder = Preferences.getSortOrder(getActivity());
    Uri uri = buildFileChildrenUri(directoryId, showHidden);
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
