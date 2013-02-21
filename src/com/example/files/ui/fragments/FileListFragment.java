package com.example.files.ui.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.files.R;

public final class FileListFragment extends ListFragment {

  public static FileListFragment create(String dir) {
    Bundle args = new Bundle(1);
    args.putString("dir", dir);

    FileListFragment fragment = new FileListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.file_list_fragment, container, false);
  }
}
