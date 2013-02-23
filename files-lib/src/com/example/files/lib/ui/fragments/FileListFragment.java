package com.example.files.lib.ui.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.files.lib.R;
import com.example.files.lib.ui.adapters.FileListAdapter;

public final class FileListFragment extends ListFragment {

  private static final String ARG_FOLDER = "folder";

  public static FileListFragment fileListFragment(File dir) {
    return fileListFragment(dir.getAbsolutePath());
  }

  public static FileListFragment fileListFragment(String dir) {
    Bundle args = new Bundle(1);
    args.putString(ARG_FOLDER, dir);

    FileListFragment fragment = new FileListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setContent();
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.file_list_fragment, container, false);
  }

  private File getFolder() {
    Bundle args = checkNotNull(getArguments(), "args");
    String folder = checkNotNull(args.getString(ARG_FOLDER), ARG_FOLDER);
    return new File(folder);
  }

  private void overrideEmptyText(int resId) {
    ((TextView)getView().findViewById(android.R.id.empty)).setText(resId);
  }

  private void setContent() {
    File folder = getFolder();
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
