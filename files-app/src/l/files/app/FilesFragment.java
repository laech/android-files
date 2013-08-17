package l.files.app;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static com.google.common.collect.Lists.newArrayList;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.menu.Menus.*;
import static l.files.app.mode.Modes.*;
import static l.files.common.io.Files.listFiles;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.List;
import l.files.R;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenus;
import l.files.common.base.Value;
import l.files.common.widget.MultiChoiceAction;
import l.files.common.widget.MultiChoiceActions;
import l.files.setting.ShowHiddenFilesSetting;
import l.files.setting.SortSetting;
import l.files.sort.Sorters;

public final class FilesFragment
    extends BaseFileListFragment implements MultiChoiceAction {

  public static final String ARG_DIRECTORY = "directory";

  FileObserver observer;

  private File dir;
  private Value<String> sort;
  private Value<Boolean> showHiddenFiles;
  private ActionMode mode;

  /**
   * Flag to control whether list data changes should be animated.
   * <p/>
   * When users changes view settings on activity A, animation should happen on
   * activity A, but when user clicks the back button to go back to activity B,
   * B will receive the new view settings, but B should not animate, should just
   * show the latest view.
   */
  private boolean animate;

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
        refresh(animate);
      }
    });

    configureListView();
    configureOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));
  }

  @Override public void onResume() {
    animate = false;
    {
      super.onResume();
      observer.startWatching();
    }
    animate = true;
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
    refresh(animate);
  }

  @Subscribe public void handle(SortSetting setting) {
    sort = setting;
    refresh(animate);
  }

  @Subscribe public void handle(DeleteFilesRequest request) {
    if (null != mode) mode.finish(); // TODO better handle this
  }

  private void refresh(boolean animate) { // TODO do this in async task for large dir
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
    List<OptionsMenu> menus = newArrayList(
        newBookmarkMenu(getBus(), dir),
        newSortMenu(getFragmentManager()),
        newShowHiddenFilesMenu(getBus()));

    if (DEBUG) {
      menus.add(newDirMenu(dir));
    }

    setOptionsMenu(OptionsMenus.compose(menus));
  }

  private void configureListView() {
    ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    list.setMultiChoiceModeListener(MultiChoiceActions.asListener(
        this,
        newCountSelectedItemsAction(list),
        newCutAction(list, getBus()),
        newDeleteAction(list, getFragmentManager()),
        newSelectAllAction(list)));
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    this.mode = mode;
  }

  @Override public void onDestroy(ActionMode mode) {
    this.mode = null;
  }

  @Override
  public void onChange(ActionMode mode, int position, long id, boolean checked) {
  }
}
