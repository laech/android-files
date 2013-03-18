package com.example.files.app;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.app.FragmentManagers.popAllBackStacks;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;

public class FileListActivity extends Activity
    implements OnBackStackChangedListener, OnFileSelectedListener {

  private static final File HOME = getExternalStorageDirectory();

  public static final String ARG_DIRECTORY = FileListFragment.ARG_DIRECTORY;

  private OnFileSelectedListener fileSelectedHandler = new FileClickHandler(this);

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content);
    String directory = getDirectory();
    if (savedInstanceState == null)
      show(directory);

    updateActionBarUpButton();
    getFragmentManager().addOnBackStackChangedListener(this);
  }

  private String getDirectory() {
    String directory = getIntent().getStringExtra(ARG_DIRECTORY);
    return directory != null ? directory : HOME.getAbsolutePath();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      popAllBackStacks(getFragmentManager());
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackStackChanged() {
    updateActionBarUpButton();
  }

  private void updateActionBarUpButton() {
    ActionBar actionBar = getActionBar();
    boolean canGoUp = getFragmentManager().getBackStackEntryCount() > 0;
    actionBar.setDisplayHomeAsUpEnabled(canGoUp);
    actionBar.setHomeButtonEnabled(canGoUp);
  }

  @Override public void onFileSelected(File file) {
    fileSelectedHandler.onFileSelected(file);
  }

  void show(String directory) {
    Bundle bundle = new Bundle(1);
    bundle.putString(ARG_DIRECTORY, directory);

    FileListFragment fragment = new FileListFragment();
    fragment.setArguments(bundle);

    getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .addToBackStack(null)
        .setTransition(TRANSIT_FRAGMENT_FADE)
        .commitAllowingStateLoss();

    updateTitle(directory);
  }

  private void updateTitle(String path) {
    File file = new File(path);
    setTitle(HOME.equals(file) ? getString(R.string.home) : file.getName());
  }
}
