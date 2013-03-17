package com.example.files.ui.fragments;

import static com.example.files.util.FileSort.BY_NAME;

import java.io.File;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
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
import com.example.files.ui.adapters.FileListAdapter;
import com.example.files.widget.ListViews;

public final class FileListFragment
    extends ListFragment implements MultiChoiceModeListener {

  public static interface FileClickListener {
    void onFileClick(Activity activity, File file);
  }

  public static final String ARG_DIRECTORY = "directory";

  private FileClickListener listener;
  private FileListAdapter adapter;

  public void setListener(FileClickListener listener) {
    this.listener = listener;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getListView().setMultiChoiceModeListener(this);
    showContent();
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.file_list_fragment, container, false);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    if (listener != null) {
      listener.onFileClick(getActivity(), (File) l.getItemAtPosition(pos));
    }
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.file_list, menu);
  }

  private void showContent() {
    Bundle args = getArguments();
    if (args == null) {
      return;
    }

    String directory = args.getString(ARG_DIRECTORY);
    if (directory != null) {
      showContent(new File(directory));
    }
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
      adapter = new FileListAdapter(getActivity().getApplicationContext());
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
