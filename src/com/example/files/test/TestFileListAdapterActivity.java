package com.example.files.test;

import java.io.File;

import android.app.ListActivity;
import android.os.Bundle;

import com.example.files.ui.adapters.FileListAdapter;

public final class TestFileListAdapterActivity extends ListActivity {
  public static final String EXTRA_FOLDER = "folder";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String folder = getIntent().getStringExtra(EXTRA_FOLDER);
    setListAdapter(new FileListAdapter(this, new File(folder).listFiles()));
  }
}
