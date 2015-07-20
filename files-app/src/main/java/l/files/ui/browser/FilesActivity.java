package l.files.ui.browser;

import android.app.ActionBar;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toolbar;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nullable;

import de.greenrobot.event.EventBus;
import l.files.R;
import l.files.common.app.BaseActivity;
import l.files.common.app.OptionsMenus;
import l.files.common.view.ActionModeProvider;
import l.files.common.widget.DrawerListeners;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.OpenFileRequest;
import l.files.ui.menu.AboutMenu;
import l.files.ui.menu.ActionBarDrawerToggleAction;
import l.files.ui.menu.GoBackOnHomePressedAction;
import l.files.ui.newtab.NewTabMenu;
import l.files.ui.open.FileOpener;
import l.files.ui.preview.Preview;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.support.v4.view.GravityCompat.START;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.common.view.Views.find;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.IOExceptions.message;
import static l.files.ui.UserDirs.DIR_HOME;

public final class FilesActivity extends BaseActivity implements
    OnBackStackChangedListener,
    ActionModeProvider,
    OnItemSelectedListener {

  private static final Logger log = Logger.get(FilesActivity.class);

  public static final String EXTRA_DIRECTORY = "directory";

  private EventBus bus;

  private ActionBarDrawerToggle drawerToggle;
  private ActionMode currentActionMode;
  private ActionMode.Callback currentActionModeCallback;

  private DrawerLayout drawer;
  private DrawerListener drawerListener;

  private HierarchyAdapter hierarchy;
  private Toolbar toolbar;
  private Spinner title;

  public ImmutableList<Resource> hierarchy() {
    return hierarchy.get();
  }

  public Spinner title() {
    return title;
  }

  public Toolbar toolbar() {
    return toolbar;
  }

  @Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.files_activity);
    Preview.get(this).readCacheAsyncIfNeeded();

    toolbar = find(R.id.toolbar, this);
    hierarchy = new HierarchyAdapter();
    title = find(R.id.title, this);
    title.setAdapter(hierarchy);
    title.setOnItemSelectedListener(this);

    bus = Events.get();
    drawer = find(R.id.drawer_layout, this);
    drawerListener = new DrawerListener();
    drawerToggle = new ActionBarDrawerToggle(this, drawer, 0, 0);

    drawer.setDrawerListener(DrawerListeners.compose(drawerToggle, drawerListener));

    setActionBar(toolbar);
    ActionBar actionBar = getActionBar();
    assert actionBar != null;
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);

    setOptionsMenu(OptionsMenus.compose(
        new ActionBarDrawerToggleAction(drawerToggle),
        new GoBackOnHomePressedAction(this),
        new NewTabMenu(this),
        new AboutMenu(this)));

    getFragmentManager().addOnBackStackChangedListener(this);

    if (state == null) {
      getFragmentManager()
          .beginTransaction()
          .replace(
              R.id.content,
              FilesFragment.create(initialDirectory()),
              FilesFragment.TAG)
          .commit();
    }

    new Handler().post(new Runnable() {
      @Override public void run() {
        updateToolBar();
      }
    });
  }

  @Override public void onItemSelected(
      AdapterView<?> parent, View view, int position, long id) {
    log.debug("onItemSelected");
    Resource item = (Resource) parent.getAdapter().getItem(position);
    if (!Objects.equals(item, fragment().directory())) {
      open(OpenFileRequest.create(item));
    } else {
      log.debug("Already show requested directory.");
    }
  }

  @Override public void onNothingSelected(AdapterView<?> parent) {
  }

  @Override protected void onDestroy() {
    getFragmentManager().removeOnBackStackChangedListener(this);
    super.onDestroy();
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  private Resource initialDirectory() {
    Resource dir = getIntent().getParcelableExtra(EXTRA_DIRECTORY);
    return dir == null ? DIR_HOME : dir;
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override protected void onPause() {
    bus.unregister(this);
    Preview.get(this).writeCacheAsyncIfNeeded();
    super.onPause();
  }

  @Override public void onBackPressed() {
    if (isSidebarOpen()) {
      closeSidebar();
    } else {
      super.onBackPressed();
    }
  }

  @Override public void onBackStackChanged() {
    updateToolBar();
  }

  private void updateToolBar() {
    FilesFragment fragment = fragment();
    int backStacks = getFragmentManager().getBackStackEntryCount();
    drawerToggle.setDrawerIndicatorEnabled(backStacks == 0);
    hierarchy.set(fragment.directory());
    title.setSelection(hierarchy.indexOf(fragment().directory()));
  }

  @Override
  public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    if (keyCode == KEYCODE_BACK) {
      while (getFragmentManager().getBackStackEntryCount() > 0) {
        getFragmentManager().popBackStackImmediate();
      }
      return true;
    }
    return super.onKeyLongPress(keyCode, event);
  }

  @Override public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    log.debug("onActionModeFinished");

    currentActionMode = null;
    currentActionModeCallback = null;
    drawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
  }

  @Override public void onActionModeStarted(ActionMode mode) {
    super.onActionModeStarted(mode);

    if (isSidebarOpen()) {
      drawer.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
    } else {
      drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
    }
  }

  @Override public ActionMode startActionMode(ActionMode.Callback callback) {
    log.debug("startActionMode");
    currentActionModeCallback = callback;
    return (currentActionMode = super.startActionMode(callback));
  }

  private boolean isSidebarOpen() {
    return drawer.isDrawerOpen(START);
  }

  private void closeSidebar() {
    drawer.closeDrawer(START);
  }

  public ActionBarDrawerToggle drawerToggle() {
    return drawerToggle;
  }

  @Nullable
  @Override public ActionMode currentActionMode() {
    return currentActionMode;
  }

  @Nullable public ActionMode.Callback currentActionModeCallback() {
    return currentActionModeCallback;
  }

  public DrawerLayout drawerLayout() {
    return drawer;
  }

  public void onEventMainThread(CloseActionModeRequest request) {
    log.debug("onEventMainThread(%s)", request);
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
  }

  public void onEventMainThread(OpenFileRequest request) {
    log.debug("onEventMainThread(%s)", request);
    if (currentActionMode != null) {
      currentActionMode.finish();
    }
    open(request);
  }

  private void open(final OpenFileRequest request) {
    log.debug("open(%s)", request);
    closeDrawerThenRun(new Runnable() {
      @Override public void run() {
        show(request);
      }
    });
  }

  private void closeDrawerThenRun(Runnable runnable) {
    if (drawer.isDrawerOpen(START)) {
      drawerListener.mRunOnClosed = runnable;
      drawer.closeDrawers();
    } else {
      runnable.run();
    }
  }

  private void show(final OpenFileRequest request) {
    new AsyncTask<Void, Void, Object>() {
      @Override protected Object doInBackground(Void... params) {
        try {
          return request.getResource().stat(FOLLOW);
        } catch (IOException e) {
          log.debug(e, "%s", request);
          return e;
        }
      }

      @Override protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if (!isDestroyed() && !isFinishing()) {
          if (result instanceof Stat) {
            show(request.getResource(), (Stat) result);
          } else {
            String msg = message((IOException) result);
            makeText(FilesActivity.this, msg, LENGTH_SHORT).show();
          }
        }
      }
    }.execute();
  }

  private void show(Resource resource, Stat stat) {
    if (!isReadable(resource)) { // TODO Check in background
      showPermissionDenied();
    } else if (stat.isDirectory()) {
      showDirectory(resource);
    } else {
      showFile(resource);
    }
  }

  private boolean isReadable(Resource resource) {
    try {
      return resource.readable();
    } catch (IOException e) {
      return false;
    }
  }

  private void showPermissionDenied() {
    makeText(this, R.string.permission_denied, LENGTH_SHORT).show();
  }

  private void showDirectory(Resource resource) {
    FilesFragment fragment = fragment();
    if (fragment.directory().equals(resource)) {
      return;
    }
    FilesFragment f = FilesFragment.create(resource);
    getFragmentManager()
        .beginTransaction()
        .replace(R.id.content, f, FilesFragment.TAG)
        .addToBackStack(null)
        .setBreadCrumbTitle(resource.name())
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();
  }

  private void showFile(Resource resource) {
    FileOpener.get(this).apply(resource);
  }

  public FilesFragment fragment() {
    return (FilesFragment) getFragmentManager()
        .findFragmentByTag(FilesFragment.TAG);
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
