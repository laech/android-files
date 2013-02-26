package com.example.files.ui.fragments;

import java.io.File;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.ui.adapters.FileListAdapter;

public final class FileListFragment extends ListFragment {

  public static interface OnFileClickListener {
    void onFileClick(File file);
  }

  private static final String ARG_FOLDER = "folder";

  public static FileListFragment create(File dir) {
    return create(dir.getAbsolutePath());
  }

  public static FileListFragment create(String dir) {
    Bundle args = new Bundle(1);
    args.putString(ARG_FOLDER, dir);

    FileListFragment fragment = new FileListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private OnFileClickListener listener;

  public OnFileClickListener getOnFileClickListener() {
    return listener;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    init();
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.file_list_fragment, container, false);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    notifyListenerIfAvailable((File)l.getItemAtPosition(pos));
  }

  public void setOnFileClickListener(OnFileClickListener listener) {
    this.listener = listener;
  }

  private void init() {
    Bundle args = getArguments();
    if (args == null) {
      return;
    }

    String folder = args.getString(ARG_FOLDER);
    if (folder != null) {
      showContent(new File(folder));
    }
  }

  private void notifyListenerIfAvailable(File file) {
    if (listener != null) {
      listener.onFileClick(file);
    }
  }

  private void overrideEmptyText(int resId) {
    ((TextView)getView().findViewById(android.R.id.empty)).setText(resId);
  }

  private void showContent(File folder) {
    File[] files = folder.listFiles();
    if (files == null) {
      overrideEmptyText(folder.exists()
          ? R.string.not_a_folder
          : R.string.folder_doesnt_exist);
    } else {
      setListAdapter(new FileListAdapter(getActivity(), files));
    }
  }
}
