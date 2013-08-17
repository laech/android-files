package l.files.app;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.common.app.BaseFragmentActivity;
import l.files.common.base.Consumer;

import java.io.File;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static l.files.app.FilesApp.getBus;
import static l.files.app.FilesPagerAdapter.POSITION_FILES;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.format.Formats.label;

public final class FilesActivity extends BaseFragmentActivity {

  public static final String EXTRA_DIR = FilesFragment.ARG_DIRECTORY;

  Consumer<OpenFileRequest> helper;
  Bus bus;
  File dir;
  ViewPager pager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.files_activity);

    bus = getBus(this);
    dir = getDir();
    helper = OpenFileRequestConsumer.get(this);
    pager = setViewPagerAdapter();
    setTitle(label(getResources()).apply(dir));
    setActionBarAppearance();
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
    super.onOptionsItemSelected(item);
    if (android.R.id.home == item.getItemId()) {
      finish();
      return true;
    }
    return false;
  }

  @Subscribe public void handle(OpenFileRequest request) {
    // TODO register helper directly and checkout onNewIntent
    File file = request.value();
    if (dir.equals(file)) {
      pager.setCurrentItem(POSITION_FILES, true);
    } else {
      helper.take(request);
    }
  }

  @Subscribe public void handle(DeleteFilesRequest request) {
    TrashService.delete(request.value(), this);
  }

  private ViewPager setViewPagerAdapter() {
    ViewPager pager = (ViewPager) findViewById(R.id.pager);
    pager.setAdapter(new FilesPagerAdapter(getSupportFragmentManager(), dir, isPortrait()));
    pager.setCurrentItem(POSITION_FILES);
    return pager;
  }

  private void setActionBarAppearance() {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(getIntent().hasExtra(EXTRA_DIR));
    }
  }

  private File getDir() {
    String path = getIntent().getStringExtra(EXTRA_DIR);
    return path == null ? DIR_HOME : new File(path);
  }

  private boolean isPortrait() {
    return getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT;
  }
}
