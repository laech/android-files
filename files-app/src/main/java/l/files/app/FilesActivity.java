package l.files.app;

import android.app.ActionBar;
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Function;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l.files.R;
import l.files.common.app.BaseFragmentActivity;
import l.files.common.app.OptionsMenus;

import static android.app.ActionBar.LayoutParams;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static l.files.app.Bundles.getInt;
import static l.files.app.Bundles.getParcelableArrayList;
import static l.files.app.FilesApp.getBus;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.format.Formats.label;
import static l.files.app.menu.Menus.newCloseTabMenu;
import static l.files.app.menu.Menus.newTabMenu;

public final class FilesActivity
    extends BaseFragmentActivity implements TabHandler {

  public static final String EXTRA_DIR = FilesPagerFragment.ARG_DIRECTORY;

  private static final String STATE_TAB_ITEMS = "tabTitles";
  private static final String STATE_ID_SEED = "idGenerator";

  Bus bus;
  File directory;

  ViewPager viewPager;
  ViewPagerTabBar tabs;

  ActionBar actionBar;
  ActionBarDrawerToggle actionBarDrawerToggle;
  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  FilesPagerFragment currentPagerFragment;
  DrawerLayout drawerLayout;
  DrawerListener drawerListener;
  Function<File, String> labels;
  IdGenerator idGenerator;

  final Handler handler = new Handler();

  @Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.files_activity);

    initFields(getSavedId(state));
    setViewPager(getSavedTabItems(state));
    setDrawer();
    setActionBar();
    setOptionsMenu(OptionsMenus.compose(
        newTabMenu(this),
        newCloseTabMenu(this)));
    updateShowTabs();
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    actionBarDrawerToggle.syncState();
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    actionBarDrawerToggle.onConfigurationChanged(newConfig);
  }

  private int getSavedId(Bundle state) {
    return getInt(state, STATE_ID_SEED, 0);
  }

  private ArrayList<TabItem> getSavedTabItems(Bundle state) {
    return getParcelableArrayList(state, STATE_TAB_ITEMS, TabItem.class);
  }

  private File getInitialDirectory() {
    String path = getIntent().getStringExtra(EXTRA_DIR);
    return path == null ? DIR_HOME : new File(path);
  }

  private void initFields(int idSeed) {
    idGenerator = new IdGenerator(idSeed);
    labels = label(getResources());
    bus = getBus(this);
    directory = getInitialDirectory();
    viewPager = (ViewPager) findViewById(R.id.pager);
    tabs = new ViewPagerTabBar(this, bus);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerListener = new DrawerListener();
    actionBar = getActionBar();
    actionBarDrawerToggle = new ActionBarDrawerToggle(
        this, drawerLayout, R.drawable.ic_drawer, 0, 0);
  }

  private void setViewPager(ArrayList<TabItem> items) {
    if (items.isEmpty()) {
      items = newArrayList(new TabItem(idGenerator.get(), directory,
          labels.apply(directory)));
    }
    viewPager.setAdapter(new FilesPagerAdapter(items));
    viewPager.setOffscreenPageLimit(2);
    tabs.setViewPager(viewPager);
  }

  private void setDrawer() {
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    drawerLayout.setDrawerListener(drawerListener);
  }

  private void setActionBar() {
    actionBar.setIcon(R.drawable.ic_storage);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setCustomView(tabs.getRootContainer(),
        new LayoutParams(MATCH_PARENT, MATCH_PARENT));
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
    outState.putInt(STATE_ID_SEED, idGenerator.get());
    outState.putParcelableArrayList(STATE_TAB_ITEMS,
        newArrayList(getPagerAdapter().getItems()));
  }

  @Override public void onBackPressed() {
    if (currentPagerFragment.popBackStack()) {
      return;
    }
    if (viewPager.getAdapter().getCount() > 1) {
      closeCurrentTab();
    } else {
      super.onBackPressed();
    }
  }

  @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    if (keyCode == KEYCODE_BACK) {
      while (currentPagerFragment.hasBackStack()) {
        currentPagerFragment.popBackStack();
      }
      return true;
    }
    return super.onKeyLongPress(keyCode, event);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // TODO update to use OptionsMenu
    if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    currentActionMode = null;
    currentActionModeCallback = null;
    viewPager.setEnabled(true);
    drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
  }

  @Override public void onActionModeStarted(ActionMode mode) {
    super.onActionModeStarted(mode);
    currentActionMode = mode;
    viewPager.setEnabled(false);
    drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
  }

  @Override
  public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
    currentActionModeCallback = callback;
    return super.onWindowStartingActionMode(callback);
  }

  public ActionMode getCurrentActionMode() {
    return currentActionMode;
  }

  public ActionMode.Callback getCurrentActionModeCallback() {
    return currentActionModeCallback;
  }

  public DrawerLayout getDrawerLayout() {
    return drawerLayout;
  }

  public ViewPager getViewPager() {
    return viewPager;
  }

  public ViewPagerTabBar getViewPagerTabBar() {
    return tabs;
  }

  public FilesPagerFragment getCurrentPagerFragment() {
    return currentPagerFragment;
  }

  private FilesPagerAdapter getPagerAdapter() {
    return (FilesPagerAdapter) viewPager.getAdapter();
  }

  @Subscribe public void handle(CloseActionModeRequest request) {
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
  }

  @Subscribe public void handle(final OpenFileRequest request) {
    closeDrawerThenRun(new Runnable() {
      @Override public void run() {
        currentPagerFragment.show(request.value());
      }
    });
  }

  @Subscribe public void handle(ViewPagerTabBar.OnUpSelected up) {
    if (currentPagerFragment.hasBackStack()) {
      closeDrawerThenRun(new Runnable() {
        @Override public void run() {
          currentPagerFragment.popBackStack();
        }
      });
    } else if (drawerLayout.isDrawerOpen(Gravity.START)) {
      drawerLayout.closeDrawers();
    } else {
      drawerLayout.openDrawer(Gravity.START);
    }
  }

  @Override public void openNewTab() {
    FilesPagerAdapter adapter = getPagerAdapter();
    adapter.addItem(idGenerator.get());
    viewPager.setCurrentItem(adapter.getCount() - 1, true);
  }

  @Override public void closeCurrentTab() {
    if (getPagerAdapter().getCount() == 1) {
      finish();
    } else {
      getPagerAdapter().removeCurrentItem();
    }
  }

  private void updateShowTabs() {
    setShowTabs(getPagerAdapter().getCount() > 1);
  }

  private void setShowTabs(boolean showTabs) {
    actionBar.setDisplayShowTitleEnabled(!showTabs);
    actionBar.setDisplayShowHomeEnabled(!showTabs);
    actionBar.setDisplayShowCustomEnabled(showTabs);
  }

  private void closeDrawerThenRun(Runnable runnable) {
    if (drawerLayout.isDrawerOpen(Gravity.START)) {
      drawerListener.mRunOnClosed = runnable;
      drawerLayout.closeDrawers();
    } else {
      runnable.run();
    }
  }

  class FilesPagerAdapter extends FragmentPagerAdapter {
    private final List<TabItem> mItems;
    private final Map<Object, Integer> mPositions;

    FilesPagerAdapter(List<TabItem> items) {
      super(getSupportFragmentManager());
      mItems = items;
      mPositions = newHashMap();
    }

    @Override public long getItemId(int position) {
      return mItems.get(position).getId();
    }

    @Override public Fragment getItem(int position) {
      final File directory = mItems.get(position).getDirectory();
      final Fragment fragment = FilesPagerFragment.create(directory);
      mPositions.put(fragment, position);
      return fragment;
    }

    @Override public void setPrimaryItem(
        ViewGroup container, int position, Object object) {
      super.setPrimaryItem(container, position, object);
      if (currentPagerFragment != null && currentPagerFragment != object) {
        currentPagerFragment.setHasOptionsMenu(false);
      }
      currentPagerFragment = (FilesPagerFragment) object;
      currentPagerFragment.setHasOptionsMenu(true);
      updateTabTitle(position);
    }

    private void updateTabTitle(final int position) {
      final String title = labels.apply(currentPagerFragment
          .getCurrentDirectory());
      mItems.get(position).setTitle(title);
      handler.post(new Runnable() {
        @Override public void run() {
          final boolean hasBackStack = currentPagerFragment.hasBackStack();
          actionBar.setTitle(title);
          actionBarDrawerToggle.setDrawerIndicatorEnabled(!hasBackStack);
          tabs.updateTab(position, title, hasBackStack);
        }
      });
    }

    @Override public int getCount() {
      return mItems.size();
    }

    @Override public CharSequence getPageTitle(int position) {
      return mItems.get(position).getTitle();
    }

    @Override public int getItemPosition(Object object) {
      final Integer position = mPositions.get(object);
      return position != null ? position : POSITION_NONE;
    }

    @Override public void notifyDataSetChanged() {
      super.notifyDataSetChanged();
      updateShowTabs();
    }

    List<TabItem> getItems() {
      return mItems;
    }

    void addItem(int id) {
      final String title = labels.apply(DIR_HOME);
      mItems.add(new TabItem(id, DIR_HOME, title));
      tabs.addTab(title);
      notifyDataSetChanged();
    }

    void removeCurrentItem() {
      tabs.removeTab(viewPager.getCurrentItem());
      mItems.remove(viewPager.getCurrentItem());
      mPositions.remove(currentPagerFragment);
      notifyDataSetChanged();
      if (viewPager.getCurrentItem() >= getCount()) {
        viewPager.setCurrentItem(getCount() - 1);
      }
    }
  }

  class DrawerListener implements DrawerLayout.DrawerListener {

    Runnable mRunOnClosed;

    @Override public void onDrawerSlide(View drawerView, float slideOffset) {
      actionBarDrawerToggle.onDrawerSlide(drawerView, slideOffset);
    }

    @Override public void onDrawerOpened(View drawerView) {
      actionBarDrawerToggle.onDrawerOpened(drawerView);
    }

    @Override public void onDrawerStateChanged(int newState) {
      actionBarDrawerToggle.onDrawerStateChanged(newState);
    }

    @Override public void onDrawerClosed(View drawerView) {
      actionBarDrawerToggle.onDrawerClosed(drawerView);
      if (mRunOnClosed != null) {
        mRunOnClosed.run();
        mRunOnClosed = null;
      }
    }
  }
}
