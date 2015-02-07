package l.files.ui.browser;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.ListViews;
import l.files.common.widget.MultiChoiceModeListeners;
import l.files.fs.Path;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.ui.Animations;
import l.files.ui.BaseFileListFragment;
import l.files.ui.ListProvider;
import l.files.ui.OpenFileRequest;
import l.files.ui.Preferences;
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

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.ui.Preferences.getSort;
import static l.files.ui.Preferences.isShowHiddenFilesKey;
import static l.files.ui.Preferences.isSortKey;

public final class FilesFragment extends BaseFileListFragment implements
    LoaderCallbacks<List<FileListItem>>,
    OnSharedPreferenceChangeListener,
    ListProvider<Path> {

  // TODO implement progress

  public static final String TAG = FilesFragment.class.getSimpleName();

  private static final String ARG_PATH = "path";

  public static FilesFragment create(Path path) {
    Bundle bundle = new Bundle(1);
    bundle.putParcelable(ARG_PATH, path);

    FilesFragment browser = new FilesFragment();
    browser.setArguments(bundle);
    return browser;
  }

  private Path path;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  public Path getPath() {
    return path;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    path = getArguments().getParcelable(ARG_PATH);

    setupListView();
    setupOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));

    getLoaderManager().initLoader(0, null, this);
    Preferences.register(getActivity(), this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Preferences.unregister(getActivity(), this);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    FileListItem.File item = (FileListItem.File) l.getItemAtPosition(pos);
    getBus().post(new OpenFileRequest(item.getPath()));
  }

  @Override public FilesAdapter getListAdapter() {
    return (FilesAdapter) super.getListAdapter();
  }

  private void setupOptionsMenu() {
    Activity context = getActivity();
    setOptionsMenu(OptionsMenus.compose(
        new BookmarkMenu(BookmarkManagerImpl.get(context), path),
        new NewDirMenu(context.getFragmentManager(), path),
        new PasteMenu(context, getClipboardManager(context), path),
        new SortMenu(context.getFragmentManager()),
        new ShowHiddenFilesMenu(context)
    ));
  }

  private void setupListView() {
    final Activity context = getActivity();
    final ClipboardManager clipboard = getClipboardManager(context);
    final ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
        new CountSelectedItemsAction(list),
        new SelectAllAction(list),
        new CutAction(context, clipboard, this),
        new CopyAction(context, clipboard, this),
        new DeleteAction(context, this),
        new RenameAction(context.getFragmentManager(), this)
    ));
  }

  @Override public int getCheckedItemCount() {
    return getListView().getCheckedItemCount();
  }

  @Override public int getCheckedItemPosition() {
    return ListViews.getCheckedItemPosition(getListView());
  }

  @Override public List<Integer> getCheckedItemPositions() {
    return ListViews.getCheckedItemPositions(getListView());
  }

  @Override public Path getCheckedItem() {
    int position = getCheckedItemPosition();
    return ((FileListItem.File) getListView().getItemAtPosition(position)).getPath();
  }

  @Override public List<Path> getCheckedItems() {
    List<Integer> positions = getCheckedItemPositions();
    List<Path> paths = new ArrayList<>(positions.size());
    for (int position : positions) {
      FileListItem item = (FileListItem) getListView().getItemAtPosition(position);
      if (item.getIsFile()) {
        paths.add(((FileListItem.File) item).getPath());
      }
    }
    return paths;
  }

  @Override public Loader<List<FileListItem>> onCreateLoader(int id, Bundle bundle) {
    Activity context = getActivity();
    FileSort sort = getSort(context);
    boolean showHidden = Preferences.getShowHiddenFiles(context);
    return new FilesLoader(context, path, sort, showHidden);
  }

  @Override public void onLoadFinished(Loader<List<FileListItem>> loader, List<FileListItem> data) {
    if (getActivity() != null && !getActivity().isFinishing()) {
      if (!getListAdapter().isEmpty() && isResumed()) {
        Animations.animatePreDataSetChange(getListView());
      }
      getListAdapter().setItems(data);
      if (getListAdapter().isEmpty()) {
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

  @Override public void onLoaderReset(Loader<List<FileListItem>> loader) {
    getListAdapter().setItems(Collections.<FileListItem>emptyList());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (isSortKey(key) || isShowHiddenFilesKey(key)) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }
}