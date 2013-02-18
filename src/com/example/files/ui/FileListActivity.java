package com.example.files.ui;

import android.app.ListActivity;
import android.os.Bundle;

import com.example.files.R;
import com.example.files.ui.fragments.FileListFragment;

public final class FileListActivity extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.file_list_activity);
    setFileListFragment();
  }

  private void setFileListFragment() {
    getFragmentManager()
        .beginTransaction()
        .add(R.id.file_list, FileListFragment.create("/"))
        .commit();
  }

}