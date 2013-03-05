package com.example.files.ui.activities;

import static com.example.files.util.Objects.requires;

import java.io.File;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.files.R;
import com.example.files.ui.fragments.FileListFragment;

public final class FileListActivity extends Activity {

  public static final String ARG_FOLDER = FileListFragment.ARG_FOLDER;

  public static Intent newIntent(Context context, File folder) {
    requires(context, "context");
    requires(folder, "folder");
    return new Intent(context, FileListActivity.class)
        .putExtra(ARG_FOLDER, folder.getAbsolutePath());
  }

  public static void start(Context context, File folder) {
    context.startActivity(newIntent(context, folder));
  }

  private FileListFragment fileListFragment;

  public FileListFragment getFileListFragment() {
    return fileListFragment;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);
    setFileListFragment();
  }

  private void setFileListFragment() {
    String folder = getIntent().getStringExtra(ARG_FOLDER);
    if (folder == null) {
      getIntent().putExtra(ARG_FOLDER, "/"); // TODO test
    }

    String tag = "file_list";
    FragmentManager fm = getFragmentManager();

    fileListFragment = (FileListFragment)fm.findFragmentByTag(tag);
    if (fileListFragment == null) {
      fileListFragment = new FileListFragment();
      fileListFragment.setArguments(getIntent().getExtras());
      fm.beginTransaction()
          .add(android.R.id.content, fileListFragment, tag)
          .commit();
    }
  }
}
