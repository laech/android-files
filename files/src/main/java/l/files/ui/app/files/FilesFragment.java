package l.files.ui.app.files;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import l.files.R;
import l.files.event.Events;
import l.files.setting.Setting;
import l.files.setting.SortBy;
import l.files.trash.TrashService.TrashMover;
import l.files.ui.app.BaseListFragment;
import l.files.ui.app.files.sort.Sorter;
import l.files.ui.app.files.sort.Sorters;
import l.files.ui.event.FileSelectedEvent;
import l.files.ui.menu.OptionsMenus;
import l.files.ui.mode.MultiChoiceModes;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;

import java.io.File;
import java.util.List;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.tryFind;
import static java.util.Arrays.asList;
import static l.files.BuildConfig.DEBUG;
import static l.files.setting.Settings.getShowHiddenFilesSetting;
import static l.files.setting.Settings.getSortSetting;
import static l.files.ui.app.files.menu.Menus.*;
import static l.files.ui.app.files.mode.Modes.newCountSelectedItemsAction;
import static l.files.ui.app.files.mode.Modes.newMoveToTrashAction;
import static l.files.util.Files.listFiles;

public final class FilesFragment
    extends BaseListFragment implements OnSharedPreferenceChangeListener {

  public static final String ARG_DIRECTORY = "directory";

  private static final String TAG = FilesFragment.class.getSimpleName();

  FilesAdapter adapter;
  Bus bus = Events.bus();
  Setting<Boolean> settingShowHiddenFiles;
  Setting<SortBy> sortSetting;

  private File dir;
  private DirectoryObserver fileObserver;

  private boolean showingHiddenFiles;
  private SortBy currentSort;

  private List<Sorter> sorters;
  private SharedPreferences pref;

  public static FilesFragment create(String directory) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, directory);

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    pref = getDefaultSharedPreferences(getActivity());
    settingShowHiddenFiles = getShowHiddenFilesSetting(pref);
    sortSetting = getSortSetting(pref);
    sorters = Sorters.get(getResources());
    adapter = FilesAdapter.get(getActivity());
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
    setOptionsMenu(OptionsMenus.compose(
        newBookmarkMenu(bus, dir),
        newDirMenu(dir),
        newSortMenu(getFragmentManager())));
  }

  private void configureListView() {
    ListView list = getListView();
    list.setMultiChoiceModeListener(MultiChoiceModes.asListener(
        newCountSelectedItemsAction(list),
        newMoveToTrashAction(list, new TrashMover(getActivity()))));
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
    pref.registerOnSharedPreferenceChangeListener(this);
  }

  @Override public void onPause() {
    super.onPause();
    fileObserver.stopWatching();
    bus.unregister(this);
    pref.unregisterOnSharedPreferenceChangeListener(this);
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
    SortBy sort = sortSetting.get();
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
      Optional<Sorter> sorter = getSorter();
      if (sorter.isPresent()) {
        List<Object> items = sorter.get().apply(asList(children));
        adapter.replace(list, items, animate);
        if (hasHeaders(items)) {
          list.post(new Runnable() {
            @Override public void run() {
              list.setPinHeaders(true);
            }
          });
        }
      }
    }
  }

  private boolean hasHeaders(List<Object> items) {
    return tryFind(items, not(instanceOf(File.class))).isPresent();
  }

  private Optional<Sorter> getSorter() {
    for (Sorter sorter : sorters) {
      if (sorter.id().equals(currentSort)) {
        return Optional.of(sorter);
      }
    }
    return Optional.absent();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (sortSetting.key().equals(key)) {
      SortBy sort = sortSetting.get();
      if (currentSort != sort) {
        currentSort = sort;
        refresh(true);
      }
    }
  }
}
