package l.files.ui.app.files;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.FilesApp;
import l.files.R;
import l.files.Settings;
import l.files.media.ImageMap;
import l.files.settings.SortSetting;
import l.files.trash.TrashService.TrashMover;
import l.files.ui.app.BaseListFragment;
import l.files.ui.app.files.menu.BookmarkAction;
import l.files.ui.app.files.menu.NewDirectoryAction;
import l.files.ui.app.files.menu.SortByAction;
import l.files.ui.app.files.menu.SortByDialog;
import l.files.ui.app.files.mode.MoveToTrashAction;
import l.files.ui.app.files.mode.UpdateSelectedItemCountAction;
import l.files.ui.event.FileSelectedEvent;
import l.files.ui.menu.OptionsMenu;
import l.files.ui.mode.MultiChoiceModeDelegate;
import l.files.util.DateTimeFormat;
import l.files.util.FileSystem;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.BuildConfig.DEBUG;
import static l.files.FilesApp.getApp;
import static l.files.settings.SortSetting.Sort;
import static l.files.util.Files.listFiles;

public final class FilesFragment extends BaseListFragment implements OnScrollListener {

  public static final String ARG_DIRECTORY = "directory";

  private static final String TAG = FilesFragment.class.getSimpleName();

  FilesAdapter adapter;
  Bus bus;
  Settings settings;
  SortSetting sortSetting;

  private File dir;
  private DirectoryObserver fileObserver;

  private boolean showingHiddenFiles;
  private Sort currentSort;

  public static FilesFragment create(String directory) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, directory);

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = newListAdapter();
    settings = getApp(this).getSettings();
    sortSetting = getApp(this).getSortSetting();
    bus = FilesApp.BUS;
    dir = getDirectory();
    fileObserver = new DirectoryObserver(dir, new Handler(), new Runnable() {
      @Override public void run() {
        refresh();
      }
    });

    currentSort = sortSetting.get();
    showingHiddenFiles = settings.shouldShowHiddenFiles();

    configureListView();
    configureOptionsMenu();
    refresh();
  }

  private void configureOptionsMenu() {
    setOptionsMenu(new OptionsMenu(
        new BookmarkAction(dir, settings),
        new NewDirectoryAction(dir),
        new SortByAction(getFragmentManager(), SortByDialog.CREATOR)));
  }

  private void configureListView() {
    ListView list = getListView();
    list.setMultiChoiceModeListener(new MultiChoiceModeDelegate(
        new UpdateSelectedItemCountAction(list),
        new MoveToTrashAction(list, new TrashMover(getActivity()))));
    list.setOnScrollListener(this);
    setListAdapter(adapter);
  }

  private File getDirectory() {
    Bundle args = checkNotNull(getArguments(), "arguments");
    String path = checkNotNull(args.getString(ARG_DIRECTORY), ARG_DIRECTORY);
    return new File(path);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.files_fragment, container, false);
  }

  @Override public void onResume() {
    super.onResume();
    checkPreferences();
    fileObserver.startWatching();
    bus.register(this);
  }

  @Override public void onPause() {
    super.onPause();
    fileObserver.stopWatching();
    bus.unregister(this);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Object item = l.getItemAtPosition(pos);
    if (item instanceof File) bus.post(new FileSelectedEvent((File) item));
  }

  private void updateUnableToShowDirectoryError(File directory) {
    if (!directory.exists()) {
      overrideEmptyText(R.string.directory_doesnt_exist);
    } else if (!directory.isDirectory()) {
      overrideEmptyText(R.string.not_a_directory);
    } else {
      overrideEmptyText(R.string.permission_denied);
    }
  }

  private void overrideEmptyText(int resId) {
    View root = getView();
    if (root != null)
      ((TextView) root.findViewById(android.R.id.empty)).setText(resId);
  }

  void checkPreferences() { // TODO
    boolean show = settings.shouldShowHiddenFiles();
    Sort sort = sortSetting.get();
    if (showingHiddenFiles != show || currentSort != sort) {
      showingHiddenFiles = show;
      currentSort = sort;
      refresh();
    }
  }

  private void refresh() {
    if (DEBUG) Log.d(TAG, "refresh");
    setContent(dir);
  }

  private void setContent(File directory) {
    File[] children = listFiles(directory, showingHiddenFiles);
    if (children == null) {
      updateUnableToShowDirectoryError(directory);
    } else {
      adapter.replaceAll(currentSort.transform(getActivity(), children));
    }
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    adapter.clearPendingAnimations();
  }

  @Override public void onScroll(AbsListView view, int firstVisibleItem,
      int visibleItemCount, int totalItemCount) {
  }

  @Subscribe public void handle(Sort sort) {
    if (currentSort != sort) {
      currentSort = sort;
      adapter = newListAdapter();
      setListAdapter(adapter);
      refresh();
    }
  }

  private FilesAdapter newListAdapter() {
    return new FilesAdapter
        (getListView(),
        FileSystem.INSTANCE,
        ImageMap.INSTANCE,
        new DateTimeFormat(getActivity()));
  }
}
