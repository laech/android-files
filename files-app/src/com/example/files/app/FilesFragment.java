package com.example.files.app;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import com.example.files.event.FileSelectedEvent;
import com.example.files.util.DebugTimer;
import com.example.files.widget.FilesAdapter;
import com.example.files.widget.ListViews;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import java.io.File;

import static com.example.files.app.FilesApp.inject;
import static com.example.files.util.FileSort.BY_NAME;
import static com.google.common.base.Preconditions.checkNotNull;

public final class FilesFragment
    extends ListFragment implements MultiChoiceModeListener {

  public static final String ARG_DIRECTORY = "directory";

  @Inject FilesAdapter adapter;
  @Inject Bus bus;

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getListView().setMultiChoiceModeListener(this);
    showContent(getDirectory());
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

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    File file = (File) l.getItemAtPosition(pos);
    bus.post(new FileSelectedEvent(file));
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.file_list, menu);
  }

  private void overrideEmptyText(int resId) {
    ((TextView) getView().findViewById(android.R.id.empty)).setText(resId);
  }

  private void showContent(File directory) {
    File[] children = directory.listFiles();
    if (children == null) {
      overrideEmptyText(directory.exists()
          ? R.string.not_a_directory
          : R.string.directory_doesnt_exist);
    } else {
      adapter.addAll(children);
      adapter.sort(BY_NAME);
      setListAdapter(adapter);
    }
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    updateActionModeTitle(mode);
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(R.menu.file_list_contextual, menu);
    updateActionModeTitle(mode);
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.move_to_trash:
        ListViews.removeCheckedItems(getListView(), adapter);
        mode.finish();
        return true;
    }
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
  }

  private void updateActionModeTitle(ActionMode mode) {
    int n = getListView().getCheckedItemCount();
    mode.setTitle(getString(R.string.n_selected, n));
  }
}
