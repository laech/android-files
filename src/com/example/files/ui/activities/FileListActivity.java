package com.example.files.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import com.example.files.R;
import com.example.files.ui.fragments.FileListFragment;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

public final class FileListActivity extends Activity {

  private static final File HOME = getExternalStorageDirectory();

  public static final String ARG_FOLDER = FileListFragment.ARG_FOLDER;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);
    setFileListFragment();
  }

  private void setFileListFragment() {
    String folder = getIntent().getStringExtra(ARG_FOLDER);
    if (folder == null) {
      folder = HOME.getAbsolutePath();
      getIntent().putExtra(ARG_FOLDER, folder);
    }

    updateTitle(folder);

    String tag = "file_list";
    if (getFragmentManager().findFragmentByTag(tag) == null) {
      FileListFragment fragment = new FileListFragment();
      fragment.setArguments(getIntent().getExtras());
      getFragmentManager()
          .beginTransaction()
          .add(android.R.id.content, fragment, tag)
          .commit();
    }
  }

  private void updateTitle(String path) {
    File file = new File(path);
    if (HOME.equals(file)) {
      setTitle(R.string.home);
    } else {
      setTitle(file.getName());
    }
  }
}
