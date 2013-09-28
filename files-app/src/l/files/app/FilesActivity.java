package l.files.app;

import static android.app.ActionBar.LayoutParams;
import static android.graphics.Color.TRANSPARENT;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.Window.FEATURE_PROGRESS;
import static com.google.common.collect.Lists.newArrayList;
import static l.files.app.Bundles.getInt;
import static l.files.app.Bundles.getParcelableArrayList;
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
import l.files.R;
import l.files.common.app.BaseFragmentActivity;

public final class FilesActivity extends BaseFragmentActivity implements TabOpener {

  public static final String EXTRA_DIR = FilesPagerFragment.ARG_DIRECTORY;

  private static final String STATE_TAB_ITEMS = "tabTitles";
  private static final String STATE_ID_SEED = "idGenerator";

  private class FilesPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<TabItem> items;

    FilesPagerAdapter(ArrayList<TabItem> items) {
      super(getSupportFragmentManager());
      this.items = items;
    }

    @Override public long getItemId(int position) {
      return items.get(position).getId();
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
      items.get(position).setTitle(title);
      handler.post(new Runnable() {
        @Override public void run() {
          tabs.setTabText(position, title);
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
      return POSITION_NONE;
    }

    ArrayList<TabItem> getItems() {
      return items;
    }

    void addItem(int id) {
      String title = labels.apply(dir);
      items.add(new TabItem(id, title));
      tabs.addTab(title);
      notifyDataSetChanged();
    }

    void removeCurrentItem() {
      tabs.removeTab(pager.getCurrentItem());
      items.remove(pager.getCurrentItem());
      getSupportFragmentManager()
          .beginTransaction()
          .remove(currentPage)
          .commit();
      notifyDataSetChanged();
      if (pager.getCurrentItem() >= getCount()) {
        pager.setCurrentItem(getCount() - 1);
      }
    }
  }

  Bus bus;
  File dir;
  ViewPager pager;
  ViewPagerTabBar tabs;

  ActionMode currentActionMode;
  ActionMode.Callback currentActionModeCallback;

  private FilesPagerFragment currentPage;
  private DrawerLayout drawerLayout;
  private Function<File, String> labels;
  private IdGenerator idGenerator;

  private final Handler handler = new Handler();

  @Override protected void onCreate(Bundle state) {
    requestWindowFeature(FEATURE_PROGRESS);
    super.onCreate(state);
    setContentView(R.layout.files_activity);
    setProgressBarIndeterminate(true);

    initFields(getSavedId(state));
    setPager(getSavedTabItems(state));
    setDrawer();
    setActionBar();
    setOptionsMenu(newTabMenu(this));
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
    dir = getInitialDirectory();
    pager = (ViewPager) findViewById(R.id.pager);
    tabs = new ViewPagerTabBar(this);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
  }

  private void setPager(ArrayList<TabItem> items) {
    if (items.isEmpty()) {
      items = newArrayList(new TabItem(idGenerator.get(), labels.apply(dir)));
    }
    pager.setAdapter(new FilesPagerAdapter(items));
    tabs.setViewPager(pager);
  }

  private void setDrawer() {
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    drawerLayout.setScrimColor(TRANSPARENT);
  }

  private void setActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setCustomView(tabs.getRootContainer(), new LayoutParams(MATCH_PARENT, MATCH_PARENT));
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
    outState.putParcelableArrayList(STATE_TAB_ITEMS, getPagerAdapter().getItems());
  }

  @Override public void onBackPressed() {
    if (currentPage.popBackStack()) {
      return;
    }
    if (pager.getAdapter().getCount() > 1) {
      closeCurrentTab();
    } else {
      super.onBackPressed();
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

  private FilesPagerAdapter getPagerAdapter() {
    return (FilesPagerAdapter) pager.getAdapter();
  }

  @Subscribe public void handle(@SuppressWarnings("UnusedParameters") CloseActionModeRequest request) {
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
    FilesPagerAdapter adapter = getPagerAdapter();
    adapter.addItem(idGenerator.get());
    pager.setCurrentItem(adapter.getCount() - 1, true);
  }

  public void closeCurrentTab() {
    getPagerAdapter().removeCurrentItem();
  }
}
