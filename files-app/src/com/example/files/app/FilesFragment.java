package com.example.files.app;

import static com.example.files.app.FilesApp.inject;
import static com.example.files.util.FileFilters.HIDE_HIDDEN_FILES;
import static com.example.files.util.FileSort.BY_NAME;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;

import javax.inject.Inject;
import java.io.File;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.util.DebugTimer;
import com.squareup.otto.Bus;

public final class FilesFragment extends ListFragment {

  public static final String ARG_DIRECTORY = "directory";

  @Inject FilesAdapter adapter;
  @Inject Bus bus;
  @Inject Settings settings;

  private boolean showingHiddenFiles;

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getListView().setMultiChoiceModeListener(
        new FilesFragmentMultiChoiceModeListener(this));
    refresh(settings.shouldShowHiddenFiles());
    setListAdapter(adapter);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    inject(this);
    setHasOptionsMenu(true);
  }

  private File getDirectory() {
    Bundle args = checkNotNull(getArguments(), "arguments");
    String path = checkNotNull(args.getString(ARG_DIRECTORY), ARG_DIRECTORY);
    return new File(path);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    DebugTimer timer = DebugTimer.start("FilesFragment");// TODO
    View view = inflater.inflate(R.layout.files_fragment, container, false);
    timer.log("onCreateView");
    return view;
  }

  @Override public void onResume() {
    super.onResume();
    checkShowHiddenFilesPreference();
  }

  void checkShowHiddenFilesPreference() {
    boolean shouldShowHiddenFiles = settings.shouldShowHiddenFiles();
    if (isShowingHiddenFiles() != shouldShowHiddenFiles)
      refresh(shouldShowHiddenFiles);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    File file = (File) l.getItemAtPosition(pos);
    bus.post(new FileSelectedEvent(file));
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.files_fragment, menu);
  }

  private void overrideEmptyText(int resId) {
    ((TextView) getView().findViewById(android.R.id.empty)).setText(resId);
  }

  private void setContent(File directory, boolean showHiddenFiles) {
    File[] children = directory
        .listFiles(showHiddenFiles ? null : HIDE_HIDDEN_FILES);
    if (children == null) {
      overrideEmptyText(directory.exists() // TODO permission denied
          ? R.string.not_a_directory
          : R.string.directory_doesnt_exist);
    } else {
      sort(children, BY_NAME);
      adapter.setNotifyOnChange(false);
      adapter.clear();
      adapter.addAll(asList(children));
      adapter.notifyDataSetChanged();
    }
  }

  @Override public FilesAdapter getListAdapter() {
    return adapter;
  }

  boolean isShowingHiddenFiles() {
    return showingHiddenFiles;
  }

  void refresh(boolean showHiddenFiles) {
    setContent(getDirectory(), showHiddenFiles);
    this.showingHiddenFiles = showHiddenFiles;
  }
}
