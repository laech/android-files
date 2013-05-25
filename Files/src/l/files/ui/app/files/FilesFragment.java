package l.files.ui.app.files;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Bus;
import l.files.FilesApp;
import l.files.R;
import l.files.Settings;
import l.files.ui.action.MultiChoiceModeDelegate;
import l.files.ui.app.BaseListFragment;
import l.files.ui.event.FileSelectedEvent;
import l.files.ui.menu.OptionsMenu;

import java.io.File;

import static android.widget.AbsListView.OnScrollListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static l.files.BuildConfig.DEBUG;
import static l.files.FilesApp.getApp;
import static l.files.trash.TrashService.TrashMover;
import static l.files.util.FileSort.BY_NAME;
import static l.files.util.Files.listFiles;

public final class FilesFragment
    extends BaseListFragment implements OnScrollListener {

  public static final String ARG_DIRECTORY = "directory";

  private static final String TAG = FilesFragment.class.getSimpleName();

  FilesAdapter adapter;
  Bus bus;
  Settings settings;

  private boolean showingHiddenFiles;
  private File dir;
  private FileObserver fileObserver;

  public static FilesFragment create(String directory) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, directory);

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new FilesAdapter(getListView());
    settings = getApp(this).getSettings();
    bus = FilesApp.BUS;
    dir = getDirectory();
    fileObserver = new FilesAdapterObserver(dir, adapter, new Handler());

    configureListView();
    configureOptionsMenu();
    refresh();
  }

  private void configureOptionsMenu() {
    setOptionsMenu(new OptionsMenu(
        new FavoriteAction(dir, settings),
        new NewDirectoryAction(dir)
    ));
  }

  private void configureListView() {
    ListView list = getListView();
    list.setMultiChoiceModeListener(new MultiChoiceModeDelegate(
        new UpdateSelectedItemCountAction(list),
        new MoveToTrashAction(list, new TrashMover(getActivity()))
    ));
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
    checkShowHiddenFilesPreference();
    fileObserver.startWatching();
  }

  @Override public void onPause() {
    super.onPause();
    fileObserver.stopWatching();
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
    ((TextView) getView().findViewById(android.R.id.empty)).setText(resId);
  }

  void checkShowHiddenFilesPreference() {
    boolean show = settings.shouldShowHiddenFiles();
    if (showingHiddenFiles != show) refresh(show);
  }

  private void refresh() {
    refresh(showingHiddenFiles);
  }

  private void refresh(boolean showHiddenFiles) {
    if (DEBUG) Log.d(TAG, "refresh");
    setContent(dir, showHiddenFiles);
    this.showingHiddenFiles = showHiddenFiles;
  }

  private void setContent(File directory, boolean showHiddenFiles) {
    File[] children = listFiles(directory, showHiddenFiles);
    if (children == null) {
      updateUnableToShowDirectoryError(directory);
    } else {
      adapter.replaceAll(asList(children), BY_NAME);
    }
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    adapter.clearPendingAnimations();
  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
  }
}
