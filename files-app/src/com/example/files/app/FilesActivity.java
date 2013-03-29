package com.example.files.app;

import static android.os.Environment.getExternalStorageDirectory;
import static android.text.TextUtils.isEmpty;
import static com.example.files.app.FilesApp.inject;
import static com.example.files.app.FilesPagerAdapter.POSITION_FILE_LIST;

import javax.inject.Inject;
import java.io.File;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.event.MediaDetectedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class FilesActivity extends FragmentActivity {

  public static final String EXTRA_DIRECTORY = FilesFragment.ARG_DIRECTORY;

  private static final int RESULT_SHOW_HOME = 100;
  private static final File HOME = getExternalStorageDirectory();

  private boolean isHome;

  @Inject FilesActivityHelper helper;
  @Inject Bus bus;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String path = getIntent().getStringExtra(EXTRA_DIRECTORY);
    initFields(path);
    initUi(path);
  }

  private void initFields(String path) {
    isHome = isEmpty(path);
    inject(this);
  }

  private void initUi(String path) {
    File directory = isHome ? HOME : new File(path);
    setContentView(createViewPager(directory));
    updateActionBar(directory);
  }

  private ViewPager createViewPager(File directory) {
    FragmentManager fm = getSupportFragmentManager();
    ViewPager pager = new ViewPager(this);
    pager.setId(R.id.content);
    pager.setAdapter(new FilesPagerAdapter(fm, directory.getAbsolutePath()));
    pager.setCurrentItem(POSITION_FILE_LIST);
    return pager;
  }

  private void updateActionBar(File directory) {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(!isHome);
    actionBar.setHomeButtonEnabled(!isHome);
    setTitle(isHome ? getString(R.string.home) : directory.getName());
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
        showHome();
        return true;
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onActivityResult(int request, int result, Intent i) {
    super.onActivityResult(request, result, i);
    if (!isHome && result == RESULT_SHOW_HOME) showHome();
  }

  private void showHome() {
    setResult(RESULT_SHOW_HOME);
    finish();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.still, R.anim.activity_disappear);
  }

  @Subscribe public void handle(FileSelectedEvent event) {
    helper.handle(event, this);
  }

  @Subscribe public void handle(MediaDetectedEvent event) {
    helper.handle(event, this);
  }
}
