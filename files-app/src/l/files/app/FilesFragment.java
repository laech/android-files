package l.files.app;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceAction;
import l.files.common.widget.MultiChoiceActions;
import l.files.setting.ShowHiddenFilesSetting;
import l.files.setting.SortSetting;
import l.files.setting.Value;
import l.files.sort.Sorters;

import java.io.File;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.menu.Menus.*;
import static l.files.app.mode.Modes.newCountSelectedItemsAction;
import static l.files.app.mode.Modes.newDeleteAction;
import static l.files.common.io.Files.listFiles;

public final class FilesFragment
    extends BaseFileListFragment implements MultiChoiceAction {

  public static final String ARG_DIRECTORY = "directory";

  FileObserver observer;

  private File dir;
  private Value<String> sort;
  private Value<Boolean> showHiddenFiles;
  private ActionMode mode;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  public static FilesFragment create(File dir) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, dir.getAbsolutePath());

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    dir = new File(getArguments().getString(ARG_DIRECTORY));
    observer = new DirObserver(dir, new Handler(), new Runnable() {
      @Override public void run() {
        refresh(true);
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

  @Subscribe public void handle(ShowHiddenFilesSetting setting) {
    showHiddenFiles = setting;
    refresh(!getListAdapter().isEmpty());
  }

  @Subscribe public void handle(SortSetting setting) {
    sort = setting;
    refresh(!getListAdapter().isEmpty());
  }

  @Subscribe public void handle(DeleteFilesRequest request) {
    if (null != mode) mode.finish();
  }

  private void refresh(boolean animate) {
    if (showHiddenFiles == null || sort == null) {
      return;
    }
    File[] children = listFiles(dir, showHiddenFiles.value());
    if (children == null) {
      updateUnableToShowDirectoryError(dir);
    } else {
      List<?> items = Sorters.apply(sort.value(), getResources(), children);
      getListAdapter().replace(getListView(), items, animate);
    }
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
    if (DEBUG) {
      setOptionsMenu(OptionsMenus.compose(
          newBookmarkMenu(getBus(), dir),
          newDirMenu(dir),
          newSortMenu(getFragmentManager()),
          newShowHiddenFilesMenu(getBus())));
    } else {
      setOptionsMenu(OptionsMenus.compose(
          newBookmarkMenu(getBus(), dir),
          newSortMenu(getFragmentManager()),
          newShowHiddenFilesMenu(getBus())));
    }
  }

  private void configureListView() {
    ListView listView = getListView();
    listView.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    listView.setMultiChoiceModeListener(MultiChoiceActions.asListener(
        this,
        newCountSelectedItemsAction(listView),
        newDeleteAction(getActivity().getSupportFragmentManager(), listView)));
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    this.mode = mode;
  }

  @Override public void onDestroy(ActionMode mode) {
    this.mode = null;
  }

  @Override
  public void onChange(ActionMode mode, int position, long id, boolean checked) {}
}
