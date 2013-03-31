package com.example.files.app;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.example.files.app.FilesPagerAdapter.POSITION_FILES;
import static com.example.files.util.FileSystem.DIRECTORY_HOME;

import java.io.File;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.event.MediaDetectedEvent;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class FilesActivity extends BaseActivity {

  public static final String EXTRA_DIRECTORY = FilesFragment.ARG_DIRECTORY;

  FileSystem fileSystem;
  FilesActivityHelper helper;
  Bus bus;

  ViewPager pager;
  File directoryInDisplay;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fileSystem = FileSystem.INSTANCE;
    helper = FilesActivityHelper.INSTANCE;
    bus = FilesApp.BUS;

    String path = getIntent().getStringExtra(EXTRA_DIRECTORY);
    directoryInDisplay = path == null ? DIRECTORY_HOME : new File(path);
    pager = createViewPager(directoryInDisplay);
    setContentView(pager);
    updateActionBarWithDirectoryIf(path != null, directoryInDisplay);
  }

  private ViewPager createViewPager(File dir) {
    String path = dir.getAbsolutePath();
    FragmentManager fm = getSupportFragmentManager();
    ViewPager pager = new ViewPager(this);
    pager.setId(R.id.content);
    pager.setAdapter(new FilesPagerAdapter(fm, path, isPortrait()));
    pager.setCurrentItem(POSITION_FILES);
    return pager;
  }

  private boolean isPortrait() {
    return getResources().getConfiguration()
        .orientation == ORIENTATION_PORTRAIT;
  }

  private void updateActionBarWithDirectoryIf(boolean update, File directory) {
    if (update) {
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setTitle(fileSystem.getDisplayName(directory, getResources()));
    }
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.files_activity, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        goHome();
        return true;
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onActivityResult(int request, int result, Intent i) {
    super.onActivityResult(request, result, i);
    goHome();
  }

  @Subscribe public void handle(FileSelectedEvent event) {
    if (directoryInDisplay.equals(event.file())) {
      boolean smoothScroll = true;
      pager.setCurrentItem(POSITION_FILES, smoothScroll);
    } else {
      helper.handle(event, this);
    }
  }

  @Subscribe public void handle(MediaDetectedEvent event) {
    helper.handle(event, this);
  }

  private void goHome() {
    boolean hasParentActivity = getIntent().hasExtra(EXTRA_DIRECTORY);
    if (hasParentActivity) finish();
  }
}
