package com.example.files.test;

import android.app.Activity;
import android.os.Bundle;

import com.example.files.R;
import com.example.files.ui.fragments.FileListFragment;

public final class TestFileListFragmentActivity extends Activity {

  public static final String FOLDER = "folder";

  private FileListFragment fragment;

  public FileListFragment getFragment() {
    return fragment;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);

    fragment = new FileListFragment();
    fragment.setArguments(getIntent().getExtras());
    getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .commit();
  }
}
