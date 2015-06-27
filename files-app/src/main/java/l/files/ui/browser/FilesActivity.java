package l.files.ui.browser;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
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

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Objects;

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
import l.files.ui.FileLabels;
import l.files.ui.OpenFileRequest;
import l.files.ui.menu.AboutMenu;
import l.files.ui.menu.ActionBarDrawerToggleAction;
import l.files.ui.menu.GoBackOnHomePressedAction;
import l.files.ui.newtab.NewTabMenu;
import l.files.ui.open.FileOpener;

import static android.app.ActionBar.NAVIGATION_MODE_LIST;
import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.support.v4.view.GravityCompat.START;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.IOExceptions.message;
import static l.files.ui.UserDirs.DIR_HOME;

public final class FilesActivity extends BaseActivity
        implements OnBackStackChangedListener, OnNavigationListener, ActionModeProvider
{

    private static final Logger log = Logger.get(FilesActivity.class);

    public static final String EXTRA_DIRECTORY = "directory";

    EventBus bus;

    ActionBarDrawerToggle actionBarDrawerToggle;
    ActionMode currentActionMode;
    ActionMode.Callback currentActionModeCallback;

    DrawerLayout drawerLayout;
    DrawerListener drawerListener;

    private HierarchyAdapter hierarchy;

    public ImmutableList<Resource> hierarchy()
    {
        return hierarchy.get();
    }

    @Override
    protected void onCreate(final Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.files_activity);

        hierarchy = new HierarchyAdapter();

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(hierarchy, this);

        initFields();
        setDrawer();
        setOptionsMenu(OptionsMenus.compose(
                new ActionBarDrawerToggleAction(actionBarDrawerToggle),
                new GoBackOnHomePressedAction(this),
                new NewTabMenu(this),
                new AboutMenu(this)));
        getFragmentManager().addOnBackStackChangedListener(this);

        if (state == null)
        {
            getFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.content,
                            FilesFragment.create(getInitialDirectory()),
                            FilesFragment.TAG)
                    .commit();
        }

        new Handler().post(new Runnable()
        {
            @Override
            public void run()
            {
                updateToolBar();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(final int position, final long itemId)
    {
        log.debug("onNavigationItemSelected");
        final Resource item = hierarchy.getItem(position);
        if (!Objects.equals(item, fragment().directory()))
        {
            open(OpenFileRequest.create(item));
        }
        else
        {
            log.debug("Already show requested directory.");
        }
        return true;
    }

    @Override
    protected void onDestroy()
    {
        getFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    private Resource getInitialDirectory()
    {
        final Resource dir = getIntent().getParcelableExtra(EXTRA_DIRECTORY);
        return dir == null ? DIR_HOME : dir;
    }

    private void initFields()
    {
        bus = Events.get();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListener = new DrawerListener();
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, 0, 0);
    }

    private void setDrawer()
    {
        drawerLayout.setDrawerListener(
                DrawerListeners.compose(actionBarDrawerToggle, drawerListener));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause()
    {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public void onBackPressed()
    {
        if (isSidebarOpen())
        {
            closeSidebar();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged()
    {
        updateToolBar();
    }

    private void updateToolBar()
    {
        final FilesFragment fragment = fragment();
        final Resource directory = fragment.directory();
        final String label = FileLabels.get(getResources(), directory);
        setTitle(label);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(
                getFragmentManager().getBackStackEntryCount() == 0);
        hierarchy.set(fragment.directory());
        getActionBar().setSelectedNavigationItem(hierarchy.indexOf(fragment.directory()));
    }

    @Override
    public boolean onKeyLongPress(final int keyCode, final KeyEvent event)
    {
        if (keyCode == KEYCODE_BACK)
        {
            while (getFragmentManager().getBackStackEntryCount() > 0)
            {
                getFragmentManager().popBackStackImmediate();
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onActionModeFinished(final ActionMode mode)
    {
        super.onActionModeFinished(mode);
        log.debug("onActionModeFinished");

        currentActionMode = null;
        currentActionModeCallback = null;
        drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onActionModeStarted(final ActionMode mode)
    {
        super.onActionModeStarted(mode);
        log.debug("onActionModeStarted");

        currentActionMode = mode;
        if (isSidebarOpen())
        {
            drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
        }
        else
        {
            drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private boolean isSidebarOpen()
    {
        return drawerLayout.isDrawerOpen(START);
    }

    private void closeSidebar()
    {
        drawerLayout.closeDrawer(START);
    }

    @Override
    public ActionMode onWindowStartingActionMode(
            final ActionMode.Callback callback)
    {
        currentActionModeCallback = callback;
        return super.onWindowStartingActionMode(callback);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle()
    {
        return actionBarDrawerToggle;
    }

    @Override
    public ActionMode currentActionMode()
    {
        return currentActionMode;
    }

    public ActionMode.Callback getCurrentActionModeCallback()
    {
        return currentActionModeCallback;
    }

    public DrawerLayout getDrawerLayout()
    {
        return drawerLayout;
    }

    public void onEventMainThread(final CloseActionModeRequest request)
    {
        log.debug("onEventMainThread(%s)", request);
        if (currentActionMode != null)
        {
            currentActionMode.finish();
        }
    }

    public void onEventMainThread(final OpenFileRequest request)
    {
        log.debug("onEventMainThread(%s)", request);
        if (currentActionMode != null)
        {
            currentActionMode.finish();
        }
        open(request);
    }

    private void open(final OpenFileRequest request)
    {
        log.debug("open(%s)", request);
        closeDrawerThenRun(new Runnable()
        {
            @Override
            public void run()
            {
                show(request);
            }
        });
    }

    private void closeDrawerThenRun(final Runnable runnable)
    {
        if (drawerLayout.isDrawerOpen(START))
        {
            drawerListener.mRunOnClosed = runnable;
            drawerLayout.closeDrawers();
        }
        else
        {
            runnable.run();
        }
    }

    private void show(final OpenFileRequest request)
    {
        new AsyncTask<Void, Void, Object>()
        {
            @Override
            protected Object doInBackground(final Void... params)
            {
                try
                {
                    return request.getResource().stat(FOLLOW);
                }
                catch (final IOException e)
                {
                    log.debug(e, "%s", request);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(final Object result)
            {
                super.onPostExecute(result);
                if (!isDestroyed() && !isFinishing())
                {
                    if (result instanceof Stat)
                    {
                        show(request.getResource(), (Stat) result);
                    }
                    else
                    {
                        final String msg = message((IOException) result);
                        makeText(FilesActivity.this, msg, LENGTH_SHORT).show();
                    }
                }
            }
        }.execute();
    }

    private void show(final Resource resource, final Stat stat)
    {
        if (!isReadable(resource))
        { // TODO Check in background
            showPermissionDenied();
        }
        else if (stat.isDirectory())
        {
            showDirectory(resource);
        }
        else
        {
            showFile(resource);
        }
    }

    private boolean isReadable(final Resource resource)
    {
        try
        {
            return resource.readable();
        }
        catch (final IOException e)
        {
            return false;
        }
    }

    private void showPermissionDenied()
    {
        makeText(this, R.string.permission_denied, LENGTH_SHORT).show();
    }

    private void showDirectory(final Resource resource)
    {
        final FilesFragment fragment = fragment();
        if (fragment.directory().equals(resource))
        {
            return;
        }
        final FilesFragment f = FilesFragment.create(resource);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, f, FilesFragment.TAG)
                .addToBackStack(null)
                .setBreadCrumbTitle(resource.name())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void showFile(final Resource resource)
    {
        FileOpener.get(this).apply(resource);
    }

    public FilesFragment fragment()
    {
        return (FilesFragment) getFragmentManager()
                .findFragmentByTag(FilesFragment.TAG);
    }

    private static class DrawerListener extends SimpleDrawerListener
    {

        Runnable mRunOnClosed;

        @Override
        public void onDrawerClosed(final View drawerView)
        {
            super.onDrawerClosed(drawerView);
            if (mRunOnClosed != null)
            {
                mRunOnClosed.run();
                mRunOnClosed = null;
            }
        }
    }
}
