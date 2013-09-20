package l.files.app;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.view.Window.FEATURE_PROGRESS;
import static l.files.app.FilesApp.getBus;
import static l.files.app.FilesPagerAdapter.POSITION_FILES;
import static l.files.app.UserDirs.DIR_HOME;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.MenuItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import l.files.R;
import l.files.common.app.BaseFragmentActivity;

public final class FilesActivity extends BaseFragmentActivity {

  public static final String EXTRA_DIR = FileListContainerFragment.ARG_DIRECTORY;

  Bus bus;
  File dir;
  ViewPager pager;

  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  @Override protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(FEATURE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.files_activity);
    setProgressBarIndeterminate(true);

    bus = getBus(this);
    dir = getDir();
    pager = setViewPagerAdapter();
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (android.R.id.home == item.getItemId()) {
      bus.post(OnHomePressedEvent.INSTANCE);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    this.currentActionMode = null;
    this.currentActionModeCallback = null;
  }

  @Override public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
    this.currentActionMode = super.onWindowStartingActionMode(callback);
    this.currentActionModeCallback = callback;
    return this.currentActionMode;
  }

  public ActionMode getCurrentActionMode() {
    return currentActionMode;
  }

  public ActionMode.Callback getCurrentActionModeCallback() {
    return currentActionModeCallback;
  }

  @Subscribe public void handle(CloseActionModeRequest request) {
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
  }

  @Subscribe public void handle(FilesFragment.Event event) {
    switch (event) {
      case REFRESH_START:
        setProgressBarVisibility(true);
        break;
      case REFRESH_END:
        setProgressBarVisibility(false);
        break;
    }
  }

  private ViewPager setViewPagerAdapter() {
    ViewPager pager = (ViewPager) findViewById(R.id.pager);
    pager.setAdapter(new FilesPagerAdapter(getSupportFragmentManager(), dir, isPortrait()));
    pager.setCurrentItem(POSITION_FILES);
    return pager;
  }

  private File getDir() {
    String path = getIntent().getStringExtra(EXTRA_DIR);
    return path == null ? DIR_HOME : new File(path);
  }

  private boolean isPortrait() {
    return getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT;
  }
}
