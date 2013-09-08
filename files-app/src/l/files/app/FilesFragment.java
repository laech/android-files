package l.files.app;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.menu.Menus.*;
import static l.files.app.mode.Modes.*;
import static l.files.common.app.OptionsMenus.compose;
import static l.files.common.io.Files.listFiles;
import static l.files.common.widget.ListViews.getItems;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.List;
import java.util.Set;
import l.files.R;
import l.files.common.widget.MultiChoiceActions;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;

public final class FilesFragment extends BaseFileListFragment {

  public static final String ARG_DIRECTORY = "directory";

  public static FilesFragment create(File dir) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, dir.getAbsolutePath());

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  FileObserver observer;
  private File dir;
  private Refresher refresher;

  public FilesFragment() {
    super(R.layout.files_fragment);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    dir = new File(getArguments().getString(ARG_DIRECTORY));
    refresher = new Refresher();
    observer = new DirObserver(dir, new Handler(), new Runnable() {
      @Override public void run() {
        refresh();
      }
    });

    configureListView();
    configureOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));
  }

  @Override public void onResume() {
    refresher.setAnimate(false);
    super.onResume();
    observer.startWatching();
    refresher.setAnimate(true);
    refresher.setContentIfDirChanged();
  }

  @Override public void onPause() {
    super.onPause();
    observer.stopWatching();
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

  private final class Refresher {
    private Boolean showHiddenFiles;
    private String sort;
    private boolean animate = false;

    void setShowHiddenFiles(Boolean showHiddenFiles) {
      if (!Objects.equal(this.showHiddenFiles, showHiddenFiles)) {
        this.showHiddenFiles = showHiddenFiles;
        refresh(true);
      }
    }

    void setSort(String sort) {
      if (!Objects.equal(this.sort, sort)) {
        this.sort = sort;
        refresh(true);
      }
    }

    void setAnimate(boolean animate) {
      this.animate = animate;
    }

    void refresh() {
      refresh(false);
    }

    private void refresh(boolean triggeredByPropertyChange) {
      if (this.showHiddenFiles == null || this.sort == null) {
        return;
      }

      final File[] files = listFiles(dir, showHiddenFiles);
      if (files == null) {
        updateUnableToShowDirectoryError(dir);
      } else if (files.length == 0) {
        setEmptyContent();
      } else if (triggeredByPropertyChange) {
        setContent(files);
      } else {
        setContentIfDirChanged(files);
      }
    }

    void setContentIfDirChanged() {
      File[] files = listFiles(dir, showHiddenFiles);
      if (files != null && files.length != 0) {
        setContentIfDirChanged(files);
      }
    }

    private void setContentIfDirChanged(File[] files) {
      Set<File> newFiles = newHashSet(files);
      Set<File> oldFiles = newHashSet(getItems(getListView(), File.class));
      if (!newFiles.equals(oldFiles)) {
        setContent(files);
      }
      // TODO refresh visible items that may have last updated timestamp changed
    }

    private void setEmptyContent() {
      overrideEmptyText(R.string.empty);
      getListAdapter().replace(getListView(), emptyList(), animate);
    }

    private void setContent(File[] files) {
      if (DEBUG) {
        Log.d("FilesFragment", "setting new content");
      }
      // Sorting takes some time on large directory, do it only if necessary
      List<Object> result = Sorters.apply(sort, getResources(), files);
      getListAdapter().replace(getListView(), result, animate);
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
  }
}
