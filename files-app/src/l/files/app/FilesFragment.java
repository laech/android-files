package l.files.app;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.util.Collections.emptyList;
import static l.files.app.menu.Menus.*;
import static l.files.app.mode.Modes.*;
import static l.files.common.app.OptionsMenus.compose;
import static l.files.common.io.Files.listFiles;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.annotations.VisibleForTesting;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import l.files.R;
import l.files.common.base.Value;
import l.files.common.widget.MultiChoiceActions;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;

public final class FilesFragment extends BaseFileListFragment {

  public static final String ARG_DIRECTORY = "directory";

  Executor executor;
  FileObserver observer;

  private File dir;
  private Value<String> sort;
  private Value<Boolean> showHiddenFiles;

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
    executor = AsyncTask.SERIAL_EXECUTOR;
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

  @VisibleForTesting void refresh(final boolean animate) {
    if (showHiddenFiles == null || sort == null) {
      return;
    }
    new RefreshTask(animate).executeOnExecutor(executor);
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
    setOptionsMenu(compose(
        newBookmarkMenu(getBus(), dir),
        newDirMenu(getFragmentManager(), dir),
        newPasteMenu(getBus(), dir),
        newSortMenu(getFragmentManager()),
        newShowHiddenFilesMenu(getBus())
    ));
  }

  private void configureListView() {
    ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    list.setMultiChoiceModeListener(MultiChoiceActions.asListener(
        newCountSelectedItemsAction(list),
        newSelectAllAction(list),
        newCutAction(list, getBus()),
        newCopyAction(list, getBus()),
        newDeleteAction(list, getBus())));
  }

  public static enum Event {
    REFRESH_START,
    REFRESH_END
  }

  private final class RefreshTask extends AsyncTask<Void, Void, List<?>> {
    private final boolean animate;

    public RefreshTask(boolean animate) {
      this.animate = animate;
    }

    @Override protected void onPreExecute() {
      super.onPreExecute();
      getBus().post(Event.REFRESH_START);
    }

    @Override protected List<?> doInBackground(Void... params) {
      File[] children = listFiles(dir, showHiddenFiles.value());
      return children != null
          ? Sorters.apply(sort.value(), getResources(), children)
          : null;
    }

    @Override protected void onPostExecute(List<?> result) {
      super.onPostExecute(result);
      if (result == null) {
        updateUnableToShowDirectoryError(dir);
      } else if (result.isEmpty()) {
        overrideEmptyText(R.string.empty);
      }
      getListAdapter().replace(
          getListView(), result == null ? emptyList() : result, animate);
      getBus().post(Event.REFRESH_END);
    }
  }
}
