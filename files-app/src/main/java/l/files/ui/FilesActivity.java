package l.files.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import l.files.R;
import l.files.common.app.BaseActivity;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.DrawerListeners;
import l.files.eventbus.Subscribe;
import l.files.fs.Path;
import l.files.operations.Events;
import l.files.ui.browser.FilesPagerFragment;
import l.files.ui.menu.AboutMenu;
import l.files.ui.menu.ActionBarDrawerToggleAction;
import l.files.ui.menu.CloseTabMenu;
import l.files.ui.menu.GoBackOnHomePressedAction;
import l.files.ui.menu.NewTabMenu;
import l.files.ui.menu.ShowPathBarMenu;
import l.files.ui.pathbar.PathBarFragment;
import l.files.ui.tab.TabHandler;
import l.files.ui.tab.TabItem;
import l.files.ui.tab.ViewPagerTabBar;

import static android.app.ActionBar.LayoutParams;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static de.greenrobot.event.ThreadMode.MainThread;

public final class FilesActivity extends BaseActivity
    implements TabHandler, OnSharedPreferenceChangeListener {

  public static final String EXTRA_PATH = "directory";

  private static final String STATE_TAB_ITEMS = "tabTitles";
  private static final String STATE_ID_SEED = "idGenerator";

  EventBus bus;
  Path path;

  ViewPager viewPager;
  ViewPagerTabBar tabs;

  ActionBar actionBar;
  ActionBarDrawerToggle actionBarDrawerToggle;
  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  FilesPagerFragment currentPagerFragment;
  DrawerLayout drawerLayout;
  DrawerListener drawerListener;
  IdGenerator idGenerator;

  private PathBarFragment pathBar;

  final Handler handler = new Handler();

  @Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.files_activity);

    initFields(getSavedId(state));
    setViewPager(getSavedTabItems(state));
    setDrawer();
    setActionBar();
    setPathBar();
    setOptionsMenu(OptionsMenus.compose(
        new ActionBarDrawerToggleAction(actionBarDrawerToggle),
        new GoBackOnHomePressedAction(this),
        new NewTabMenu(this),
        new CloseTabMenu(this),
        new ShowPathBarMenu(this),
        new AboutMenu(this)));
    updateShowTabs();
    Preferences.register(this, this);
  }

  private void setPathBar() {
    if (!Preferences.getShowPathBar(this)) {
      getFragmentManager()
          .beginTransaction()
          .hide(pathBar)
          .commit();
    }
  }

  @Override protected void onDestroy() {
    Preferences.unregister(this, this);
    super.onDestroy();
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
    return Bundles.getInt(state, STATE_ID_SEED, 0);
  }

  private ArrayList<TabItem> getSavedTabItems(Bundle state) {
    return Bundles.getParcelableArrayList(state, STATE_TAB_ITEMS, TabItem.class);
  }

  private Path getInitialPath() {
    Path path = getIntent().getParcelableExtra(EXTRA_PATH);
    return path == null ? UserDirs.DIR_HOME : path;
  }

  private void initFields(int idSeed) {
    idGenerator = new IdGenerator(idSeed);
    bus = Events.get();
    path = getInitialPath();
    viewPager = (ViewPager) findViewById(R.id.pager);
    tabs = new ViewPagerTabBar(this, bus);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerListener = new DrawerListener();
    pathBar = (PathBarFragment) getFragmentManager().findFragmentById(R.id.path_bar_fragment);
    actionBar = getActionBar();
    actionBarDrawerToggle = new ActionBarDrawerToggle(
        this, drawerLayout, 0, 0);
  }

  private void setViewPager(ArrayList<TabItem> items) {
    if (items.isEmpty()) {
      items = newArrayList(new TabItem(idGenerator.get(), path,
          FileLabels.get(getResources(), path)));
    }
    viewPager.setAdapter(new FilesPagerAdapter(items));
    viewPager.setOffscreenPageLimit(2);
    tabs.setViewPager(viewPager);
  }

  private void setDrawer() {
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    drawerLayout.setDrawerListener(DrawerListeners.compose(actionBarDrawerToggle, drawerListener));
  }

  private void setActionBar() {
    actionBar.setDisplayShowHomeEnabled(false);
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
    bus.unregister(this);
    super.onPause();
  }

  @Override protected void onSaveInstanceState(
      @SuppressWarnings("NullableProblems") Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_ID_SEED, idGenerator.get());
    outState.putParcelableArrayList(STATE_TAB_ITEMS,
        newArrayList(getPagerAdapter().getItems()));
  }

  @Override public void onBackPressed() {
    if (isSidebarOpen()) {
      closeSidebar();
      return;
    }
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
    if (isSidebarOpen()) {
      drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
    } else {
      drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
    }
  }

  private boolean isSidebarOpen() {
    return drawerLayout.isDrawerOpen(Gravity.START);
  }

  private void closeSidebar() {
    drawerLayout.closeDrawer(Gravity.START);
  }

  @Override
  public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
    currentActionModeCallback = callback;
    return super.onWindowStartingActionMode(callback);
  }

  public ActionBarDrawerToggle getActionBarDrawerToggle() {
    return actionBarDrawerToggle;
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

  @Subscribe(MainThread)
  public void onEventMainThread(CloseActionModeRequest request) {
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
  }

  @Subscribe(MainThread)
  public void onEventMainThread(final OpenFileRequest request) {
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
    closeDrawerThenRun(new Runnable() {
      @Override public void run() {
        currentPagerFragment.show(request);
      }
    });
  }

  @Subscribe(MainThread)
  public void onEventMainThread(ViewPagerTabBar.OnUpSelected up) {
    if (currentPagerFragment.hasBackStack()) {
      closeDrawerThenRun(new Runnable() {
        @Override public void run() {
          currentPagerFragment.popBackStack();
        }
      });
    } else if (isSidebarOpen()) {
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
    actionBar.setDisplayHomeAsUpEnabled(!showTabs);
    actionBar.setDisplayShowTitleEnabled(!showTabs);
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

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (Preferences.isShowPathBarKey(key)) {
      boolean show = Preferences.getShowPathBar(this);
      FragmentTransaction tx = getFragmentManager().beginTransaction();
      if (show) {
        pathBar.set(currentPagerFragment.getCurrentPath());
        tx.show(pathBar);
      } else {
        tx.hide(pathBar);
      }
      tx.commit();
    }
  }

  class FilesPagerAdapter extends FragmentPagerAdapter {
    private final List<TabItem> items;
    private final Map<Object, Integer> positions;

    FilesPagerAdapter(List<TabItem> items) {
      super(getFragmentManager());
      this.items = items;
      this.positions = newHashMap();
    }

    @Override public long getItemId(int position) {
      return items.get(position).getId();
    }

    @Override public Fragment getItem(int position) {
      TabItem tab = items.get(position);
      Path path = tab.getPath();
      String title = tab.getTitle();
      Fragment fragment = FilesPagerFragment.create(path, title);
      positions.put(fragment, position);
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
      final String title = FileLabels.get(getResources(), currentPagerFragment.getCurrentPath());
      items.get(position).setTitle(title);
      handler.post(new Runnable() {
        @Override public void run() {
          final boolean hasBackStack = currentPagerFragment.hasBackStack();
          actionBar.setTitle(title);
          actionBarDrawerToggle.setDrawerIndicatorEnabled(!hasBackStack);
          pathBar.set(currentPagerFragment.getCurrentPath());
          tabs.updateTab(position, title, hasBackStack);
        }
      });
    }

    @Override public int getCount() {
      return items.size();
    }

    @Override public CharSequence getPageTitle(int position) {
      return items.get(position).getTitle();
    }

    @Override public int getItemPosition(Object object) {
      final Integer position = positions.get(object);
      return position != null ? position : POSITION_NONE;
    }

    @Override public void notifyDataSetChanged() {
      super.notifyDataSetChanged();
      updateShowTabs();
    }

    List<TabItem> getItems() {
      return items;
    }

    void addItem(int id) {
      String title = FileLabels.get(getResources(), UserDirs.DIR_HOME);
      items.add(new TabItem(id, UserDirs.DIR_HOME, title));
      tabs.addTab(title);
      notifyDataSetChanged();
    }

    void removeCurrentItem() {
      tabs.removeTab(viewPager.getCurrentItem());
      items.remove(viewPager.getCurrentItem());
      positions.remove(currentPagerFragment);
      notifyDataSetChanged();
      if (viewPager.getCurrentItem() >= getCount()) {
        viewPager.setCurrentItem(getCount() - 1);
      }
    }
  }

  private static class DrawerListener extends SimpleDrawerListener {

    Runnable mRunOnClosed;

    @Override public void onDrawerClosed(View drawerView) {
      super.onDrawerClosed(drawerView);
      if (mRunOnClosed != null) {
        mRunOnClosed.run();
        mRunOnClosed = null;
      }
    }
  }
}
