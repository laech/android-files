package com.example.files.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import com.example.files.R;
import com.example.files.ui.fragments.FileListFragment;
import dagger.Lazy;

import javax.inject.Inject;
import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.FilesApp.inject;

public final class FileListActivity extends Activity {

  private static final File HOME = getExternalStorageDirectory();

  public static final String ARG_DIRECTORY = FileListFragment.ARG_DIRECTORY;

  @Inject Lazy<FileListFragment> lazyFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);
    inject(this);
    setFileListFragment();
  }

  private void setFileListFragment() {
    String directory = getIntent().getStringExtra(ARG_DIRECTORY);
    if (directory == null) {
      directory = HOME.getAbsolutePath();
      getIntent().putExtra(ARG_DIRECTORY, directory);
    }

    updateTitle(directory);

    String tag = "file_list";
    if (getFragmentManager().findFragmentByTag(tag) == null) {
      FileListFragment fragment = lazyFragment.get();
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
