package l.files.app;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l.files.R;
import l.files.analytics.AnalyticsActivity;
import l.files.app.menu.AboutMenu;
import l.files.app.menu.ActionBarDrawerToggleAction;
import l.files.app.menu.CloseTabMenu;
import l.files.app.menu.GoBackOnHomePressedAction;
import l.files.app.menu.NewTabMenu;
import l.files.app.menu.SendFeedbackMenu;
import l.files.app.menu.ShowPathBarMenu;
import l.files.common.app.OptionsMenus;

import static android.app.ActionBar.LayoutParams;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static l.files.app.Bundles.getInt;
import static l.files.app.Bundles.getParcelableArrayList;
import static l.files.app.FilesApp.getBus;
import static l.files.app.Preferences.getShowPathBar;
import static l.files.app.Preferences.isShowPathBarKey;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.provider.FilesContract.getFileLocation;

public final class FilesActivity extends AnalyticsActivity
    implements TabHandler, OnSharedPreferenceChangeListener {

  public static final String EXTRA_DIRECTORY = "directory";

  private static final String STATE_TAB_ITEMS = "tabTitles";
  private static final String STATE_ID_SEED = "idGenerator";

  Bus bus;
  String directoryLocation;

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

  AdView ad;

  @Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.files_activity);

    initFields(getSavedId(state));
    setViewPager(getSavedTabItems(state));
    setDrawer();
    setActionBar();
    setPathBar();
    setOptionsMenu(OptionsMenus.compose(
        ActionBarDrawerToggleAction.create(this, actionBarDrawerToggle),
        GoBackOnHomePressedAction.create(this),
        NewTabMenu.create(this, this),
        CloseTabMenu.create(this, this),
        ShowPathBarMenu.create(this),
        SendFeedbackMenu.create(this),
        AboutMenu.create(this)));
    updateShowTabs();
    Preferences.register(this, this);

    checkGooglePlayServices();
    setAdView();
  }

  private void setAdView() {
    ad.loadAd(new AdRequest.Builder().build());
  }

  private void checkGooglePlayServices() {
    int result = isGooglePlayServicesAvailable(this);
    Dialog dialog = getErrorDialog(result, this, 0);
    if (dialog != null) { // Null if services are okay
      dialog.show();
    }
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
    ad.destroy();
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
    return getInt(state, STATE_ID_SEED, 0);
  }

  private ArrayList<TabItem> getSavedTabItems(Bundle state) {
    return getParcelableArrayList(state, STATE_TAB_ITEMS, TabItem.class);
  }

  private String getInitialDirectoryLocation() {
    String directoryLocation = getIntent().getStringExtra(EXTRA_DIRECTORY);
    return directoryLocation == null
        ? getFileLocation(DIR_HOME)
        : directoryLocation;
  }

  private void initFields(int idSeed) {
    ad = (AdView) findViewById(R.id.ad_view);
    idGenerator = new IdGenerator(idSeed);
    bus = getBus(this);
    directoryLocation = getInitialDirectoryLocation();
    viewPager = (ViewPager) findViewById(R.id.pager);
    tabs = new ViewPagerTabBar(this, bus);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerListener = new DrawerListener();
    pathBar = (PathBarFragment) getFragmentManager().findFragmentById(R.id.path_bar_fragment);
    actionBar = getActionBar();
    actionBarDrawerToggle = new ActionBarDrawerToggle(
        this, drawerLayout, R.drawable.ic_drawer, 0, 0);
  }

  private void setViewPager(ArrayList<TabItem> items) {
    if (items.isEmpty()) {
      items = newArrayList(new TabItem(
          idGenerator.get(),
          directoryLocation,
          FileLabels.get(getResources(), directoryLocation, "")));
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
    actionBar.setIcon(R.drawable.ic_action_storage);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setCustomView(tabs.getRootContainer(),
        new LayoutParams(MATCH_PARENT, MATCH_PARENT));
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
    ad.resume();
  }

  @Override protected void onPause() {
    bus.unregister(this);
    ad.pause();
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
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
    closeDrawerThenRun(new Runnable() {
      @Override public void run() {
        currentPagerFragment.show(request);
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

  @Override public int getTabCount() {
    return getPagerAdapter().getCount();
  }

  private void updateShowTabs() {
    setShowTabs(getPagerAdapter().getCount() > 1);
  }

  private void setShowTabs(boolean showTabs) {
    actionBar.setDisplayHomeAsUpEnabled(!showTabs);
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

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (isShowPathBarKey(key)) {
      boolean show = getShowPathBar(this);
      FragmentTransaction tx = getFragmentManager().beginTransaction();
      if (show) {
        pathBar.set(currentPagerFragment.getCurrentDirectoryLocation());
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
      String location = tab.getDirectoryLocation();
      String title = tab.getTitle();
      Fragment fragment = FilesPagerFragment.create(location, title);
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
      final String title = FileLabels.get(
          getResources(),
          currentPagerFragment.getCurrentDirectoryLocation(),
          currentPagerFragment.getCurrentDirectoryName());
      items.get(position).setTitle(title);
      handler.post(new Runnable() {
        @Override public void run() {
          final boolean hasBackStack = currentPagerFragment.hasBackStack();
          actionBar.setTitle(title);
          actionBarDrawerToggle.setDrawerIndicatorEnabled(!hasBackStack);
          pathBar.set(currentPagerFragment.getCurrentDirectoryLocation());
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
      String title = FileLabels.get(
          getResources(), getFileLocation(DIR_HOME), DIR_HOME.getName());
      items.add(new TabItem(id, getFileLocation(DIR_HOME), title));
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
