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
import com.example.files.ui.fragments.FileListFragment.OnFileClickListener;

public final class FileListActivity
    extends Activity implements OnFileClickListener {

  static final String EXTRA_FOLDER = "folder";

  public static Intent newIntent(Context context, File folder) {
    requires(context, "context");
    requires(folder, "folder");
    return new Intent(context, FileListActivity.class)
        .putExtra(EXTRA_FOLDER, folder.getAbsolutePath());
  }

  public static void start(Context context, File folder) {
    context.startActivity(newIntent(context, folder));
  }

  private FileListFragment fileListFragment;

  public FileListFragment getFileListFragment() {
    return fileListFragment;
  }

  @Override public void onFileClick(File file) {
    if (file.isDirectory()) {
      start(FileListActivity.this, file);
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);
    setFileListFragment();
  }

  @Override protected void onPause() {
    super.onPause();
    fileListFragment.setListener(null);
  }

  @Override protected void onResume() {
    super.onResume();
    fileListFragment.setListener(this);
  }

  private void setFileListFragment() {
    String folder = getIntent().getStringExtra(EXTRA_FOLDER);
    if (folder == null) {
      folder = "/";
    }

    String tag = "file_list";
    FragmentManager fm = getFragmentManager();

    fileListFragment = (FileListFragment)fm.findFragmentByTag(tag);
    if (fileListFragment == null) {
      fileListFragment = FileListFragment.create(folder);
      fm.beginTransaction()
          .add(android.R.id.content, fileListFragment, tag) // TODO
          .commit();
    }
  }
}
