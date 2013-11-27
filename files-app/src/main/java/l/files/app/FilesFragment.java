package l.files.app;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.net.URI;

import l.files.R;
import l.files.common.app.BaseListFragment;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceModeListeners;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.lang.System.identityHashCode;
import static l.files.app.Animations.animatePreDataSetChange;
import static l.files.app.FilesApp.getBus;
import static l.files.app.menu.Menus.newBookmarkMenu;
import static l.files.app.menu.Menus.newDirMenu;
import static l.files.app.menu.Menus.newPasteMenu;
import static l.files.app.menu.Menus.newShowHiddenFilesMenu;
import static l.files.app.menu.Menus.newSortMenu;
import static l.files.app.mode.Modes.newCopyAction;
import static l.files.app.mode.Modes.newCountSelectedItemsAction;
import static l.files.app.mode.Modes.newCutAction;
import static l.files.app.mode.Modes.newDeleteAction;
import static l.files.app.mode.Modes.newRenameAction;
import static l.files.app.mode.Modes.newSelectAllAction;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.buildFileChildrenUri;

public final class FilesFragment extends BaseListFragment
    implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIRECTORY_ID = "directory_id";

  private static final int LOADER_ID = identityHashCode(FilesFragment.class);

  private String directoryId;

  public static FilesFragment create(String directoryId) {
    return Fragments.setArgs(new FilesFragment(), ARG_DIRECTORY_ID,
        directoryId);
  }

  public String getDirectoryId() {
    return directoryId;
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.files_fragment, container, false);
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

  private void setupOptionsMenu() {
    FragmentManager manager = getActivityFragmentManager();
    FragmentActivity context = getActivity();
    LoaderManager loaders = getLoaderManager();
    ContentResolver resolver = context.getContentResolver();
    setOptionsMenu(OptionsMenus.compose(
        newBookmarkMenu(context, loaders, resolver, directoryId),
        newDirMenu(manager, directoryId),
        newPasteMenu(getClipboardManager(context), directoryId),
        newSortMenu(manager),
        newShowHiddenFilesMenu(getActivity())
    ));
  }

  private void setupListView() {
    ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    FragmentManager fragmentManager = getActivityFragmentManager();
    Context context = getActivity();
    ClipboardManager clipboardManager = getClipboardManager(context);
    list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
        newCountSelectedItemsAction(list),
        newSelectAllAction(list),
        newCutAction(list, clipboardManager),
        newCopyAction(list, clipboardManager),
        newDeleteAction(list, context.getContentResolver()),
        newRenameAction(list, fragmentManager, directoryId)
    ));
  }

  private FragmentManager getActivityFragmentManager() {
    return getActivity().getSupportFragmentManager();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Cursor cursor = (Cursor) l.getItemAtPosition(position);
    String uri = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
    getBus(this).post(new OpenFileRequest(new File(URI.create(uri))));
    // TODO
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
