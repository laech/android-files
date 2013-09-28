package l.files.app;

import static android.app.ActionBar.LayoutParams;
import static android.graphics.Color.TRANSPARENT;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.Window.FEATURE_PROGRESS;
import static com.google.common.collect.Lists.newArrayList;
import static l.files.app.Bundles.getInt;
import static l.files.app.Bundles.getStringList;
import static l.files.app.FilesApp.getBus;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.format.Formats.label;
import static l.files.app.menu.Menus.newTabMenu;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.ViewGroup;
import com.google.common.base.Function;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import l.files.R;
import l.files.common.app.BaseFragmentActivity;

public final class FilesActivity extends BaseFragmentActivity implements TabOpener {

  public static final String EXTRA_DIR = FilesPagerFragment.ARG_DIRECTORY;

  private static final String STATE_TAB_COUNT = "tabCount";
  private static final String STATE_TAB_TITLES = "tabTitles";
  private static final int DEFAULT_TAB_COUNT = 1;

  private class FilesPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<String> titles;
    private int size;

    FilesPagerAdapter(int size, List<String> titles) {
      super(getSupportFragmentManager());
      this.size = size;
      this.titles = newArrayList();
      this.titles.addAll(titles);
      while (this.titles.size() < size) {
        this.titles.add("");
      }
    }

    @Override public Fragment getItem(int position) {
      return FilesPagerFragment.create(dir);
    }

    @Override public void setPrimaryItem(ViewGroup container, final int position, Object object) {
      super.setPrimaryItem(container, position, object);
      if (currentPage != null && currentPage != object) {
        currentPage.setHasOptionsMenu(false);
      }

      currentPage = (FilesPagerFragment) object;
      currentPage.setHasOptionsMenu(true);
      updateTabTitle(position);
    }

    private void updateTabTitle(final int position) {
      final String title = labels.apply(currentPage.getCurrentDirectory());
      titles.set(position, title);
      handler.post(new Runnable() {
        @Override public void run() {
          tabBar.setTabText(position, title);
        }
      });
    }

    @Override public int getCount() {
      return size;
    }

    @Override public CharSequence getPageTitle(int position) {
      if (titles.size() > position) {
        return titles.get(position);
      }
      return "";
    }

    ArrayList<String> getTitles() {
      return titles;
    }

    public void addItem() {
      size += 1;
      String title = labels.apply(dir);
      titles.add(title);
      tabBar.addTab(title);
      notifyDataSetChanged();
    }
  }

  Bus bus;
  File dir;
  ViewPager pager;
  ViewPagerTabBar tabBar;

  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  private FilesPagerFragment currentPage;
  private DrawerLayout drawerLayout;
  private Function<File, String> labels;

  private final Handler handler = new Handler();

  @Override protected void onCreate(Bundle state) {
    requestWindowFeature(FEATURE_PROGRESS);
    super.onCreate(state);
    setContentView(R.layout.files_activity);
    setProgressBarIndeterminate(true);

    int tabCount = getInt(state, STATE_TAB_COUNT, DEFAULT_TAB_COUNT);
    List<String> tabTitles = getStringList(state, STATE_TAB_TITLES);
    initFields();
    setPager(tabCount, tabTitles);
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
    bus = getBus(this);
    dir = getInitialDirectory();
    pager = (ViewPager) findViewById(R.id.pager);
    tabBar = new ViewPagerTabBar(this);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
  }

  private void setPager(int tabCount, List<String> titles) {
    pager.setAdapter(new FilesPagerAdapter(tabCount, titles));
    tabBar.setViewPager(pager);
  }

  private void setDrawer() {
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    drawerLayout.setScrimColor(TRANSPARENT);
  }

  private void setActionBar(int tabCount, List<String> tabTitles) {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setCustomView(tabBar.getRootContainer(), new LayoutParams(MATCH_PARENT, MATCH_PARENT));
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
    FilesPagerAdapter adapter = (FilesPagerAdapter) pager.getAdapter();
    outState.putInt(STATE_TAB_COUNT, adapter.getCount());
    outState.putStringArrayList(STATE_TAB_TITLES, adapter.getTitles());
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
    currentActionModeCallback = callback;
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
    FilesPagerAdapter adapter = (FilesPagerAdapter) pager.getAdapter();
    adapter.addItem();
    pager.setCurrentItem(adapter.getCount() - 1, true);
  }
}
