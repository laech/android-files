package l.files.app;

import static android.graphics.Color.TRANSPARENT;
import static android.view.Window.FEATURE_PROGRESS;
import static l.files.app.FilesApp.getBus;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.format.Formats.label;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import l.files.R;
import l.files.common.app.BaseFragmentActivity;

public final class FilesActivity extends BaseFragmentActivity  {

  public static final String EXTRA_DIR = FilesPagerFragment.ARG_DIRECTORY;

  private class FilesPagerAdapter extends FragmentPagerAdapter {
    FilesPagerAdapter() {
      super(getSupportFragmentManager());
    }

    @Override public Fragment getItem(int position) {
      return FilesPagerFragment.create(dir);
    }

    @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
      super.setPrimaryItem(container, position, object);
      currentPage = (FilesPagerFragment) object;
      updateActivityActionBar(currentPage);
    }

    @Override public int getCount() {
      return 2;
    }
  }

  Bus bus;
  File dir;
  ViewPager pager;

  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  private FilesPagerFragment currentPage;

  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;

  @Override protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(FEATURE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.files_activity);
    setProgressBarIndeterminate(true);

    bus = getBus(this);
    dir = getDir();
    pager = setViewPagerAdapter();
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 0, 0);
    drawerLayout.setDrawerListener(drawerToggle);
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    drawerLayout.setScrimColor(TRANSPARENT);

    getActionBar().setHomeButtonEnabled(true);
    getActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    } else if (android.R.id.home == item.getItemId()) {
      currentPage.getChildFragmentManager().popBackStackImmediate();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    currentActionMode = null;
    currentActionModeCallback = null;
  }

  @Override public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
    currentActionMode = super.onWindowStartingActionMode(callback);
    currentActionModeCallback = callback;
    return this.currentActionMode;
  }

  @Override public void onBackPressed() {
    if (!currentPage.getChildFragmentManager().popBackStackImmediate()) {
      super.onBackPressed();
    }
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

  @Subscribe public void handle(final OpenFileRequest request) {
    currentPage.show(request.value());
    drawerLayout.closeDrawers();
  }

  private ViewPager setViewPagerAdapter() {
    ViewPager pager = (ViewPager) findViewById(R.id.pager);
    pager.setAdapter(new FilesPagerAdapter());
    return pager;
  }

  private File getDir() {
    String path = getIntent().getStringExtra(EXTRA_DIR);
    return path == null ? DIR_HOME : new File(path);
  }

  private void updateActivityActionBar(FilesPagerFragment fragment) { // TODO
    getActionBar().setTitle(label(getResources()).apply(fragment.getCurrentDirectory()));
    drawerToggle.setDrawerIndicatorEnabled(
        fragment.getChildFragmentManager().getBackStackEntryCount() == 0);
  }
}
