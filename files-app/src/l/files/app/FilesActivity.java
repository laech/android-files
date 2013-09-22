package l.files.app;

import static android.app.ActionBar.*;
import static android.graphics.Color.TRANSPARENT;
import static android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.Window.FEATURE_PROGRESS;
import static l.files.app.ActionBars.*;
import static l.files.app.FilesApp.getBus;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.format.Formats.label;
import static l.files.app.menu.Menus.newTabMenu;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import com.google.common.base.Function;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.List;
import l.files.R;
import l.files.common.app.BaseFragmentActivity;

public final class FilesActivity extends BaseFragmentActivity implements TabOpener {

  public static final String EXTRA_DIR = FilesPagerFragment.ARG_DIRECTORY;

  private static final int DEFAULT_TAB_COUNT = 1;

  private class FilesPagerAdapter extends FragmentPagerAdapter {

    private int size;

    FilesPagerAdapter(int size) {
      super(getSupportFragmentManager());
      this.size = size;
    }

    @Override public Fragment getItem(int position) {
      return FilesPagerFragment.create(dir);
    }

    @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
      super.setPrimaryItem(container, position, object);
      if (currentPage != null && currentPage != object) {
        currentPage.setHasOptionsMenu(false);
      }
      currentPage = (FilesPagerFragment) object;
      currentPage.setHasOptionsMenu(true);
      updateActionBar(currentPage, position);
    }

    @Override public int getCount() {
      return size;
    }

    public void addItem() {
      size += 1;
      notifyDataSetChanged();
    }
  }

  Bus bus;
  File dir;
  ViewPager pager;

  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  private FilesPagerFragment currentPage;

  private ActionBar actionBar;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  private DrawerLayout drawerLayout;

  private Function<File, String> labels;

  private final Handler handler = new Handler();

  private final ActionBar.TabListener tabListener = new SimpleTabListener() {
    @Override public void onTabSelected(final Tab tab, FragmentTransaction ft) {
      pager.setCurrentItem(tab.getPosition(), true);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(FEATURE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.files_activity);
    setProgressBarIndeterminate(true);

    int tabCount = getSavedActionBarTabCount(savedInstanceState, DEFAULT_TAB_COUNT);
    List<String> tabTitles = getSavedActionBarTabTitles(savedInstanceState);
    initFields();
    setPager(tabCount);
    setDrawer();
    setActionBar(tabCount, tabTitles);
    setOptionsMenu(newTabMenu(this));
  }

  private File getInitialDirectory() {
    String path = getIntent().getStringExtra(EXTRA_DIR);
    return path == null ? DIR_HOME : new File(path);
  }

  private void initFields() {
    labels = label(getResources());
    actionBar = getActionBar();
    bus = getBus(this);
    dir = getInitialDirectory();
    pager = (ViewPager) findViewById(R.id.pager);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 0, 0);
  }

  private void setPager(int tabCount) {
    pager.setAdapter(new FilesPagerAdapter(tabCount));
    pager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
      @Override public void onPageSelected(int position) {
//        if (actionBar.getSelectedNavigationIndex() != position) {
        actionBar.setSelectedNavigationItem(position);
//        }
      }
    });
  }

  private void setDrawer() {
    drawerLayout.setDrawerListener(actionBarDrawerToggle);
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    drawerLayout.setScrimColor(TRANSPARENT);
  }

  private void setActionBar(int tabCount, List<String> tabTitles) {
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    addTabs(tabCount, tabTitles);
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    actionBarDrawerToggle.syncState();
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveActionBarTabCount(outState, actionBar);
    saveActionBarTabTitles(outState, actionBar);
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    actionBarDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    } else if (android.R.id.home == item.getItemId()) {
      currentPage.popBackStack();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    if (!currentPage.popBackStack()) {
      /*
       * Overriding the default behaviour here. When user presses back to go back
       * to the home screen, the activity is by default consider destroyed forever,
       * next time it will be created fresh without any state, which means all tabs
       * and back stack will be lost, therefore this default behavior is overridden
       * to simple put this activity to the back instead of destroying it, if it's
       * later destroyed by the system, that it's okay as the system will restore
       * it's state the next time it's created, the tabs and back stacks will be
       * restored.
       */
      moveTaskToBack(true);
    }
  }

  @Override public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    currentActionMode = null;
    currentActionModeCallback = null;
    pager.setEnabled(true);
    drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
  }

  @Override public void onActionModeStarted(ActionMode mode) {
    super.onActionModeStarted(mode);
    currentActionMode = mode;
    pager.setEnabled(false);
    drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
  }

  @Override public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
//    currentActionMode = super.onWindowStartingActionMode(callback);
    currentActionModeCallback = callback;
//    return this.currentActionMode;
    return super.onWindowStartingActionMode(callback);
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
//        setProgressBarVisibility(true);
        break;
      case REFRESH_END:
//        setProgressBarVisibility(false);
        break;
    }
  }

  @Subscribe public void handle(final OpenFileRequest request) {
    currentPage.show(request.value());
    drawerLayout.closeDrawers();
  }

  @Override public void openNewTab() {
    addTab(null);
    FilesPagerAdapter adapter = (FilesPagerAdapter) pager.getAdapter();
    adapter.addItem();
    pager.setCurrentItem(adapter.getCount() - 1, true);
  }

  private void addTabs(int n, List<String> tabTitles) {
    for (int i = 0; i < n; i++) {
      addTab(tabTitles.size() > i ? tabTitles.get(i) : null);
    }
  }

  private void addTab(String title) {
    actionBar.addTab(actionBar.newTab().setText(title).setTabListener(tabListener));
    updateActionBarNavigationMode();
  }

  private void updateActionBar(final FilesPagerFragment page, final int position) {
    /*
     * Break updates to action bar into discrete commands, otherwise sometimes
     * menu action items and action mode action items will not show. See
     * https://code.google.com/p/android/issues/detail?id=29472 and
     * http://stackoverflow.com/questions/9338122/action-items-from-viewpager-initial-fragment-not-being-displayed
     * for more details.
     */
    final String title = labels.apply(page.getCurrentDirectory());
    handler.post(new Runnable() {
      @Override public void run() {
        actionBar.getTabAt(position).setText(title);
      }
    });
    handler.post(new Runnable() {
      @Override public void run() {
        actionBar.setTitle(title);
      }
    });

    actionBarDrawerToggle.setDrawerIndicatorEnabled(!page.hasBackStack());
  }

  private void updateActionBarNavigationMode() {
    boolean moreThanOneTab = actionBar.getTabCount() > 1;
    actionBar.setDisplayShowTitleEnabled(!moreThanOneTab);
    int mode = moreThanOneTab ? NAVIGATION_MODE_TABS : NAVIGATION_MODE_STANDARD;
    if (actionBar.getNavigationMode() != mode) {
      actionBar.setNavigationMode(mode);
    }
  }
}
