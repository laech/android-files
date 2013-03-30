package com.example.files.app;

import static com.example.files.app.FilesActivityOnCreate.handleOnCreate;
import static com.example.files.app.FilesActivityOnOptionsItemSelected.handleOnOptionsItemSelected;
import static com.example.files.app.FilesApp.inject;

import javax.inject.Inject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

  private boolean homeActivity;

  @Inject FilesActivityHelper helper;
  @Inject Bus bus;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleOnCreate(this);
    inject(this);
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
    return handleOnOptionsItemSelected(this, item);
  }

  @Override protected void onActivityResult(int request, int result, Intent i) {
    super.onActivityResult(request, result, i);
    if (!isHomeActivity() && result == RESULT_SHOW_HOME) goHome();
  }

  @Override public void startActivityForResult(Intent intent, int requestCode) {
    super.startActivityForResult(intent, requestCode);
    overridePendingTransition(R.anim.activity_appear, R.anim.still);
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

  boolean isHomeActivity() {
    return homeActivity;
  }

  void setHomeActivity(boolean homeActivity) {
    this.homeActivity = homeActivity;
  }

  void goHome() {
    setResult(RESULT_SHOW_HOME);
    finish();
  }
}
