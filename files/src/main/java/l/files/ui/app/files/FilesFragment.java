package l.files.ui.app.files;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.ViewEvent;
import l.files.trash.TrashService.TrashMover;
import l.files.ui.app.BaseFileListFragment;
import l.files.ui.app.files.sort.Sorter;
import l.files.ui.app.files.sort.Sorters;
import l.files.ui.menu.OptionsMenus;
import l.files.ui.mode.MultiChoiceModes;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static l.files.ui.app.files.menu.Menus.*;
import static l.files.ui.app.files.mode.Modes.newCountSelectedItemsAction;
import static l.files.ui.app.files.mode.Modes.newMoveToTrashAction;
import static l.files.io.Files.listFiles;

public final class FilesFragment extends BaseFileListFragment {

  public static final String ARG_DIRECTORY = "directory";

  public static FilesFragment create(String directory) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, directory);

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private File dir;
  private DirObserver observer;
  private ViewEvent current;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    dir = new File(getArguments().getString(ARG_DIRECTORY));
    observer = new DirObserver(dir, new Handler(), new Runnable() {
      @Override public void run() {
        if (current != null) refresh(current, true);
      }
    });

    configureListView();
    configureOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));
  }

  @Override public void onResume() {
    super.onResume();
    observer.startWatching();
  }

  @Override public void onPause() {
    super.onPause();
    observer.stopWatching();
  }

  @Override public FilesAdapter getListAdapter() {
    return (FilesAdapter) super.getListAdapter();
  }

  @Subscribe public void handle(ViewEvent event) {
    current = event;
    refresh(event, false);
  }

  private void refresh(ViewEvent event, boolean animate) {
    File[] children = listFiles(dir, event.showHiddenFiles());
    if (children == null) {
      updateUnableToShowDirectoryError(dir);
      return;
    }

    Sorter sorter = Sorters.get(getResources(), event.sort());
    List<Object> items = sorter.apply(asList(children));
    getListAdapter().replace(getListView(), items, animate);
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

  private void configureOptionsMenu() {
    setOptionsMenu(OptionsMenus.compose(
        newBookmarkMenu(getBus(), dir),
        newDirMenu(dir),
        newSortMenu(getFragmentManager())));
  }

  private void configureListView() {
    ListView listView = getListView();
    listView.setMultiChoiceModeListener(MultiChoiceModes.asListener(
        newCountSelectedItemsAction(listView),
        newMoveToTrashAction(listView, new TrashMover(getActivity()))));
  }

}
