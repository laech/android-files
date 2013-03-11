package com.example.files.ui.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.ui.adapters.FileListAdapter;
import com.example.files.ui.events.FileClickEvent;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import java.io.File;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.example.files.FilesApp.inject;

public final class FileListFragment
    extends ListFragment implements MultiChoiceModeListener {

  public static final String ARG_FOLDER = "folder";

  @Inject Bus bus;
  @Inject FileSystem fs;
  @Inject ImageMap images;

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getListView().setMultiChoiceModeListener(this);
    showContent();
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    inject(this);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.file_list_fragment, container, false);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    bus.post(new FileClickEvent(getActivity(), (File) l.getItemAtPosition(pos)));
  }

  private void showContent() {
    Bundle args = getArguments();
    if (args == null) {
      return;
    }

    String folder = args.getString(ARG_FOLDER);
    if (folder != null) {
      showContent(new File(folder));
    }
  }

  private void overrideEmptyText(int resId) {
    ((TextView) getView().findViewById(android.R.id.empty)).setText(resId);
  }

  private void showContent(File folder) {
    File[] files = folder.listFiles();
    if (files == null) {
      overrideEmptyText(folder.exists()
          ? R.string.not_a_folder
          : R.string.folder_doesnt_exist);
    } else {
      setListAdapter(new FileListAdapter(getActivity(), files, fs, images));
    }
  }

  @Override
  public void onItemCheckedStateChanged(
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
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
  }

  private void updateActionModeTitle(ActionMode mode) {
    int n = getListView().getCheckedItemCount();
    mode.setTitle(getString(R.string.n_selected, n));
  }
}
