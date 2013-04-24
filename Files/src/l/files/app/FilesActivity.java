package l.files.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.FileSelectedEvent;
import l.files.event.MediaDetectedEvent;
import l.files.util.FileSystem;

import java.io.File;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static l.files.app.FilesPagerAdapter.POSITION_FILES;

public class FilesActivity extends FragmentActivity {

  public static final String EXTRA_DIRECTORY = FilesFragment.ARG_DIRECTORY;

  FileSystem fileSystem;
  FilesActivityHelper helper;
  Bus bus;
  ViewPager pager;
  File directoryInDisplay;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Optional<File> directory = getDirectoryToDisplay();
    if (!directory.isPresent()) {
      finish();
      return;
    }

    fileSystem = FileSystem.INSTANCE;
    helper = FilesActivityHelper.INSTANCE;
    bus = FilesApp.BUS;
    directoryInDisplay = directory.get();
    pager = createViewPager(directoryInDisplay);
    setTitle(fileSystem.getDisplayName(directoryInDisplay, getResources()));
    setContentView(pager);
  }

  protected Optional<File> getDirectoryToDisplay() {
    String path = getIntent().getStringExtra(EXTRA_DIRECTORY);
    return path == null ? Optional.<File>absent() : Optional.of(new File(path));
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
    int itemId = item.getItemId();
    if (itemId == R.id.settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Subscribe public void handle(FileSelectedEvent event) {
    if (directoryInDisplay.equals(event.file())) {
      boolean smoothScroll = true;
      pager.setCurrentItem(POSITION_FILES, smoothScroll);
    } else {
      helper.handle(event, FilesActivity.this);
    }
  }

  @Subscribe public void handle(MediaDetectedEvent event) {
    helper.handle(event, FilesActivity.this);
  }
}
