package com.example.files.app;

import static com.example.files.BuildConfig.DEBUG;
import static com.example.files.app.FilesApp.getApp;
import static com.example.files.util.FileFilters.HIDE_HIDDEN_FILES;
import static com.example.files.util.FileSort.BY_NAME;
import static com.example.files.widget.ListViews.removeCheckedItems;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;

import java.io.File;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.event.EventBus;
import com.example.files.event.FileSelectedEvent;

public final class FilesFragment
    extends ListFragment implements MultiChoiceModeListener {

  public static final String ARG_DIRECTORY = "directory";

  public static FilesFragment create(String directory) {
    Bundle args = new Bundle(1);
    args.putString(ARG_DIRECTORY, directory);

    FilesFragment fragment = new FilesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  FilesAdapter adapter;
  EventBus bus;
  Settings settings;

  private boolean showingHiddenFiles;
  private File directoryInDisplay;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    adapter = new FilesAdapter(getApp(this));
    settings = getApp(this).getSettings();
    bus = FilesApp.BUS;
    directoryInDisplay = getDirectory();
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
    switch (item.getItemId()) {
    case R.id.favorite:
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
    switch (item.getItemId()) {
    case R.id.move_to_trash:
      return moveCheckedItemsToTrash(mode);
    }
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
  }

  private boolean moveCheckedItemsToTrash(ActionMode mode) {
    removeCheckedItems(getListView(), getListAdapter());
    mode.finish();
    return true;
  }

  private void updateActionModeTitle(ActionMode mode) {
    int n = getListView().getCheckedItemCount();
    mode.setTitle(getString(R.string.n_selected, n));
  }
}
