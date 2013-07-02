package l.files.ui.app.files;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.tryFind;
import static l.files.BuildConfig.DEBUG;
import static l.files.FilesApp.getApp;
import static l.files.setting.Settings.getBookmarksSetting;
import static l.files.setting.Settings.getShowHiddenFilesSetting;
import static l.files.util.Files.listFiles;

import java.io.File;
import java.util.List;

import l.files.FilesApp;
import l.files.R;
import l.files.setting.Setting;
import l.files.settings.SortSetting;
import l.files.settings.SortSetting.Sort;
import l.files.trash.TrashService.TrashMover;
import l.files.ui.FileDrawableProvider;
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
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public final class FilesFragment extends BaseListFragment {

  public static final String ARG_DIRECTORY = "directory";

  private static final String TAG = FilesFragment.class.getSimpleName();

  FilesAdapter adapter;
  Bus bus;
  Setting<Boolean> settingShowHiddenFiles;
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
    settingShowHiddenFiles = getShowHiddenFilesSetting(getDefaultSharedPreferences(getActivity()));
    sortSetting = getApp(this).getSortSetting();
    bus = FilesApp.BUS;
    dir = getDirectory();
    fileObserver = new DirectoryObserver(dir, new Handler(), new Runnable() {
      @Override public void run() {
        refresh(true);
      }
    });

    currentSort = sortSetting.get();
    showingHiddenFiles = settingShowHiddenFiles.get();

    configureListView();
    configureOptionsMenu();
    refresh(false);
  }

  private void configureOptionsMenu() {
    setOptionsMenu(new OptionsMenu(
        new BookmarkAction(dir,
            getBookmarksSetting(getDefaultSharedPreferences(getActivity()))),
        new NewDirectoryAction(dir),
        new SortByAction(getFragmentManager(), SortByDialog.CREATOR)));
  }

  private void configureListView() {
    ListView list = getListView();
    list.setMultiChoiceModeListener(new MultiChoiceModeDelegate(
        new UpdateSelectedItemCountAction(list),
        new MoveToTrashAction(list, new TrashMover(getActivity()))));
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
    boolean show = settingShowHiddenFiles.get();
    Sort sort = sortSetting.get();
    if (showingHiddenFiles != show || currentSort != sort) {
      showingHiddenFiles = show;
      currentSort = sort;
      refresh(false);
    }
  }

  private void refresh(boolean animate) {
    if (DEBUG) Log.d(TAG, "refresh");
    setContent(dir, animate);
  }

  private void setContent(File directory, boolean animate) {
    final PinnedHeaderListView list = (PinnedHeaderListView) getListView();
    list.setPinHeaders(false);

    File[] children = listFiles(directory, showingHiddenFiles);
    if (children == null) {
      updateUnableToShowDirectoryError(directory);
    } else {
      List<Object> items = currentSort.transform(getActivity(), children);
      adapter.replaceAll(list, items, animate);
      if (hasHeaders(items)) {
        list.post(new Runnable() {
          @Override public void run() {
            list.setPinHeaders(true);
          }
        });
      }
    }
  }

  private boolean hasHeaders(List<Object> items) {
    return tryFind(items, not(instanceOf(File.class))).isPresent();
  }

  @Subscribe public void handle(Sort sort) {
    if (currentSort != sort) {
      currentSort = sort;
      refresh(true);
    }
  }

  private FilesAdapter newListAdapter() {
    return new FilesAdapter(
        new FileDrawableProvider(getResources()),
        new DateTimeFormat(getActivity()));
  }
}
