package l.files.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.FileSelectedEvent;
import l.files.trash.TrashService;
import l.files.util.DirectoryObserver;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.FilesApp.getApp;
import static l.files.util.DirectoryObserver.DirectoryChangedEvent;
import static l.files.util.FileFilters.HIDE_HIDDEN_FILES;
import static l.files.util.FileSort.BY_NAME;
import static l.files.widget.ListViews.getCheckedItems;

public final class FilesFragment
    extends ListFragment implements MultiChoiceModeListener {

  public static final String ARG_DIRECTORY = "directory";
  FilesAdapter adapter;
  Bus bus;
  Settings settings;
  DirectoryObserver observer;
  private boolean showingHiddenFiles;
  private File directoryInDisplay;

  public static FilesFragment create(String directory) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, directory);

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    adapter = new FilesAdapter(getApp(this));
    settings = getApp(this).getSettings();
    bus = FilesApp.BUS;
    directoryInDisplay = getDirectory();
    observer = new DirectoryObserver(directoryInDisplay, bus, new Handler());
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getListView().setMultiChoiceModeListener(this);
    refresh(settings.shouldShowHiddenFiles());
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
    checkShowHiddenFilesPreference();
    bus.register(this);
    observer.startWatching();
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
    observer.stopWatching();
  }

  void checkShowHiddenFilesPreference() {
    boolean showHiddenFiles = settings.shouldShowHiddenFiles();
    if (isShowingHiddenFiles() != showHiddenFiles) refresh(showHiddenFiles);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Object item = l.getItemAtPosition(pos);
    if (item instanceof File) bus.post(new FileSelectedEvent((File) item));
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.files_fragment, menu);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem fav = menu.findItem(R.id.favorite);
    if (fav != null) fav.setChecked(settings.isFavorite(directoryInDisplay));
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.favorite) {
      return handleFavoriteChange(!item.isChecked());
    }
    return super.onOptionsItemSelected(item);
  }

  private boolean handleFavoriteChange(boolean favorite) {
    if (favorite) {
      settings.addFavorite(directoryInDisplay);
    } else {
      settings.removeFavorite(directoryInDisplay);
    }
    return true;
  }

  private void overrideEmptyText(int resId) {
    ((TextView) getView().findViewById(android.R.id.empty)).setText(resId);
  }

  private void setContent(File directory, boolean showHiddenFiles) {
    File[] children = listFiles(directory, showHiddenFiles);
    if (children == null) {
      updateUnableToShowDirectoryError(directory);
    } else {
      updateAdapterContent(children);
    }
  }

  private File[] listFiles(File directory, boolean showHiddenFiles) {
    return directory.listFiles(showHiddenFiles ? null : HIDE_HIDDEN_FILES);
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

  private void updateAdapterContent(File[] children) {
    sort(children, BY_NAME);
    adapter.setNotifyOnChange(false);
    adapter.clear();
    adapter.addAll(asList(children));
    adapter.notifyDataSetChanged();
  }

  @Override public FilesAdapter getListAdapter() {
    return adapter;
  }

  boolean isShowingHiddenFiles() {
    return showingHiddenFiles;
  }

  void refresh(boolean showHiddenFiles) {
    if (DEBUG) Log.d("FilesFragment", "refresh");
    setContent(directoryInDisplay, showHiddenFiles);
    this.showingHiddenFiles = showHiddenFiles;
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(R.menu.files_fragment_action_mode, menu);
    updateActionModeTitle(mode);
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    updateActionModeTitle(mode);
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.move_to_trash) {
      return moveCheckedFilesToTrash(mode);
    }
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
  }

  private boolean moveCheckedFilesToTrash(ActionMode mode) {
    TrashService.moveToTrash(getCheckedFiles(), getActivity());
    mode.finish();
    return true;
  }

  private Iterable<File> getCheckedFiles() {
    return getCheckedItems(getListView(), File.class);
  }

  private void updateActionModeTitle(ActionMode mode) {
    int n = getListView().getCheckedItemCount();
    mode.setTitle(getString(R.string.n_selected, n));
  }

  @Subscribe public void handle(DirectoryChangedEvent event) {

//    File[] newFiles = listFiles(directoryInDisplay, showingHiddenFiles);
//    if (newFiles == null) {
//      newFiles = new File[0];
//    }
//    Set<File> files = newHashSet(adapter.getFiles());
//    files.removeAll(asList(newFiles));
//
//    ListViews.removeItems(getListView(), adapter, files, new Runnable() {
//      @Override public void run() {
    refresh(showingHiddenFiles);
//      }
//    });

  }
}
