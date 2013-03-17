package com.example.files.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import com.example.files.R;
import com.example.files.ui.fragments.FileListFragment;

public final class TestFileListFragmentActivity extends Activity {

  public static final String DIRECTORY = "directory";

  private FileListFragment fragment;
  private ActionMode mode;

  public FileListFragment getFragment() {
    return fragment;
  }

  public ActionMode getActionMode() {
    return mode;
  }

  @Override public void onActionModeStarted(ActionMode mode) {
    super.onActionModeStarted(mode);
    this.mode = mode;
  }

  @Override public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    this.mode = null;
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
