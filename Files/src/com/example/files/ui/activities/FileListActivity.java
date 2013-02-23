package com.example.files.ui.activities;

import static com.example.files.lib.ui.fragments.FileListFragment.fileListFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.example.files.R;

public final class FileListActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);
    setFileListFragment();
  }

  private void setFileListFragment() {
    String tag = "file_list";
    FragmentManager fm = getFragmentManager();
    Fragment fragment = fm.findFragmentByTag(tag);
    if (fragment == null) {
      fm.beginTransaction()
          .add(android.R.id.content, fileListFragment("/"), tag) // TODO
          .commit();
    }
  }

}
