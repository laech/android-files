package l.files.ui;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Supplier;

import java.util.List;
import java.util.Set;

import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceModeListeners;
import l.files.fs.FileStatus;
import l.files.fs.Path;
import l.files.provider.bookmarks.BookmarkManagerImpl;
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
import static android.view.View.GONE;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Collections.emptyList;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.common.widget.ListViews.getCheckedItemPositions;
import static l.files.ui.Preferences.getSort;
import static l.files.ui.Preferences.isShowHiddenFilesKey;
import static l.files.ui.Preferences.isSortKey;

public final class FilesFragment extends BaseFileListFragment
    implements LoaderCallbacks<List<Object>>, OnSharedPreferenceChangeListener, Supplier<Set<Path>> {

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
  private ProgressBar progress;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  public Path getPath() {
    return path;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    path = getArguments().getParcelable(ARG_PATH);
    progress = (ProgressBar) getView().findViewById(android.R.id.progress);

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

  @Override public void onStart() {
    super.onStart();
    progress.setVisibility(GONE);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    FileStatus item = (FileStatus) l.getItemAtPosition(pos);
    getBus().post(OpenFileRequest.create(item));
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
        new RenameAction(context.getFragmentManager(), list)
    ));
  }

  @Override public Set<Path> get() {
    ListView list = getListView();
    List<Integer> positions = getCheckedItemPositions(list);
    Set<Path> paths = newHashSetWithExpectedSize(positions.size());
    for (int position : positions) {
      Object item = list.getItemAtPosition(position);
      if (item instanceof FileStatus) {
        paths.add(((FileStatus) item).path());
      }
    }
    return paths;
  }

  @Override public Loader<List<Object>> onCreateLoader(int id, Bundle bundle) {
    Activity context = getActivity();
    FileSort sort = getSort(context);
    boolean showHidden = Preferences.getShowHiddenFiles(context);
    return new FilesLoader(context, path, sort, showHidden);
  }

  @Override public void onLoadFinished(Loader<List<Object>> loader, List<Object> data) {
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

  @Override public void onLoaderReset(Loader<List<Object>> loader) {
    getListAdapter().setItems(emptyList());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (isSortKey(key) || isShowHiddenFilesKey(key)) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }
}
