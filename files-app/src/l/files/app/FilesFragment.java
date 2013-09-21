package l.files.app;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.util.Collections.emptyList;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.FilesFragment.Event.REFRESH_END;
import static l.files.app.FilesFragment.Event.REFRESH_START;
import static l.files.app.menu.Menus.*;
import static l.files.app.mode.Modes.*;
import static l.files.common.io.Files.listFiles;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.List;
import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.MultiChoiceModeListeners;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;

public final class FilesFragment extends BaseFileListFragment {

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIRECTORY = "directory";

  public static FilesFragment create(File dir) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, dir.getAbsolutePath());
    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  AsyncTaskExecutor executor;
  FileObserver observer;
  private File dir;
  private Refresher refresher;

  private boolean started = false;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  public File getDirectory() {
    return dir;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    dir = new File(getArguments().getString(ARG_DIRECTORY));
    executor = AsyncTaskExecutor.DEFAULT;
    refresher = new Refresher();

    setupListView();
    setupOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (observer != null) {
      observer.stopWatching();
    }
  }

  @Override public void onStart() {
    refresher.setAnimate(false);
    super.onStart();
    refresher.refreshIfDirty();
    refresher.setAnimate(true);
    started = true;
    if (observer == null) {
      observer = new DirObserver(dir, new Handler(), new Runnable() {
        @Override public void run() {
          if (started) {
            refresher.refresh();
          } else {
            refresher.markDirty();
          }
        }
      });
      observer.startWatching();
    }
  }

  @Override public void onStop() {
    super.onStop();
    started = false;
  }

  @Override public FilesAdapter getListAdapter() {
    return (FilesAdapter) super.getListAdapter();
  }

  @Subscribe public void handle(ShowHiddenFilesSetting show) {
    refresher.setShowHiddenFiles(show.value());
  }

  @Subscribe public void handle(SortSetting sort) {
    refresher.setSort(sort.value());
  }

  @VisibleForTesting void refresh() {
    refresher.refresh();
  }

  private void setupOptionsMenu() {
    FragmentManager manager = getActivityFragmentManager();
    setOptionsMenu(OptionsMenus.compose(
        newBookmarkMenu(getBus(), dir),
        newDirMenu(manager, dir),
        newPasteMenu(getBus(), dir),
        newSortMenu(manager),
        newShowHiddenFilesMenu(getBus())
    ));
  }

  private void setupListView() {
    ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
    FragmentManager manager = getActivityFragmentManager();
    list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
        newCountSelectedItemsAction(list),
        newSelectAllAction(list),
        newCutAction(list, getBus()),
        newCopyAction(list, getBus()),
        newDeleteAction(list, getBus()),
        newRenameAction(list, manager)));
  }

  private FragmentManager getActivityFragmentManager() {
    return getActivity().getSupportFragmentManager();
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
    if (root != null) {
      ((TextView) root.findViewById(android.R.id.empty)).setText(resId);
    }
  }

  private void setEmptyContent() {
    overrideEmptyText(R.string.empty);
    getListAdapter().replace(getListView(), emptyList(), false);
  }

  private final class Refresher {
    private Boolean showHiddenFiles;
    private String sort;
    private boolean animate = false;
    private boolean dirty = false;

    void setShowHiddenFiles(Boolean showHiddenFiles) {
      if (!Objects.equal(this.showHiddenFiles, showHiddenFiles)) {
        this.showHiddenFiles = showHiddenFiles;
        refresh();
      }
    }

    void setSort(String sort) {
      if (!Objects.equal(this.sort, sort)) {
        this.sort = sort;
        refresh();
      }
    }

    void markDirty() {
      dirty = true;
    }

    void refreshIfDirty() {
      if (dirty) {
        refresh();
        dirty = false;
      }
    }

    void setAnimate(boolean animate) {
      this.animate = animate;
    }

    void refresh() {
      if (this.showHiddenFiles == null || this.sort == null) {
        return;
      }
      if (DEBUG) {
        Log.d("FilesFragment", "refresh");
      }
      executor.execute(new RefreshTask(dir, showHiddenFiles, sort, animate));
    }
  }

  public static enum Event {
    REFRESH_START,
    REFRESH_END
  }

  final class RefreshTask extends AsyncTask<Void, Void, List<?>> {
    private final File dir;
    private final boolean showHiddenFiles;
    private final String sort;
    private final boolean animate;

    RefreshTask(File dir, boolean showHiddenFiles, String sort, boolean animate) {
      this.dir = dir;
      this.showHiddenFiles = showHiddenFiles;
      this.sort = sort;
      this.animate = animate;
    }

    @Override protected void onPreExecute() {
      super.onPreExecute();
      getBus().post(REFRESH_START);
    }

    @Override protected List<?> doInBackground(Void... params) {
      // Sorting takes some time on large directory, do it only if necessary
      Activity activity = getActivity();
      if (activity == null) {
        cancel(true);
        return null;
      }
      File[] files = listFiles(dir, showHiddenFiles);
      if (files != null) {
        return Sorters.apply(sort, activity.getResources(), files);
      } else {
        return null;
      }
    }

    @Override protected void onCancelled() {
      super.onCancelled();
      getBus().post(REFRESH_END);
    }

    @Override protected void onPostExecute(List<?> result) {
      super.onPostExecute(result);
      getBus().post(REFRESH_END);
      if (getView() == null) {
        return;
      }
      if (result == null) {
        updateUnableToShowDirectoryError(dir);
      } else if (result.size() == 0) {
        setEmptyContent();
      } else {
        getListAdapter().replace(getListView(), result, animate);
      }
    }
  }
}
