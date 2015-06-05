package l.files.ui.browser;

import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import l.files.R;
import l.files.common.app.BaseActivity;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.DrawerListeners;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileLabels;
import l.files.ui.OpenFileRequest;
import l.files.ui.Preferences;
import l.files.ui.menu.AboutMenu;
import l.files.ui.menu.ActionBarDrawerToggleAction;
import l.files.ui.menu.GoBackOnHomePressedAction;
import l.files.ui.newtab.NewTabMenu;
import l.files.ui.menu.ShowPathBarMenu;
import l.files.ui.pathbar.PathBarFragment;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
        implements OnSharedPreferenceChangeListener, OnBackStackChangedListener
{

    private static final Logger log = Logger.get(FilesActivity.class);

    public static final String EXTRA_DIRECTORY = "directory";

    EventBus bus;
    Resource directory;

    ActionBarDrawerToggle actionBarDrawerToggle;
    ActionMode currentActionMode;
    ActionMode.Callback currentActionModeCallback;

    DrawerLayout drawerLayout;
    DrawerListener drawerListener;

    private PathBarFragment pathBar;

    @Override
    protected void onCreate(final Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.files_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        initFields();
        setDrawer();
        setPathBar();
        setOptionsMenu(OptionsMenus.compose(
                new ActionBarDrawerToggleAction(actionBarDrawerToggle),
                new GoBackOnHomePressedAction(this),
                new NewTabMenu(this),
                new ShowPathBarMenu(this),
                new AboutMenu(this)));
        Preferences.register(this, this);
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

    private void setPathBar()
    {
        if (!Preferences.getShowPathBar(this))
        {
            getFragmentManager()
                    .beginTransaction()
                    .hide(pathBar)
                    .commit();
        }
    }

    @Override
    protected void onDestroy()
    {
        Preferences.unregister(this, this);
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
        directory = getInitialDirectory();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListener = new DrawerListener();
        pathBar = (PathBarFragment) getFragmentManager()
                .findFragmentById(R.id.path_bar_fragment);
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
        currentActionMode = null;
        currentActionModeCallback = null;
        drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onActionModeStarted(final ActionMode mode)
    {
        super.onActionModeStarted(mode);
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
        return drawerLayout.isDrawerOpen(Gravity.START);
    }

    private void closeSidebar()
    {
        drawerLayout.closeDrawer(Gravity.START);
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

    public ActionMode getCurrentActionMode()
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
        if (currentActionMode != null)
        {
            currentActionMode.finish();
        }
    }

    public void onEventMainThread(final OpenFileRequest request)
    {
        if (currentActionMode != null)
        {
            currentActionMode.finish();
        }
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
        if (drawerLayout.isDrawerOpen(Gravity.START))
        {
            drawerListener.mRunOnClosed = runnable;
            drawerLayout.closeDrawers();
        }
        else
        {
            runnable.run();
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences pref, final String key)
    {
        if (Preferences.isShowPathBarKey(key))
        {
            final boolean show = Preferences.getShowPathBar(this);
            final FragmentTransaction tx = getFragmentManager().beginTransaction();
            if (show)
            {
                pathBar.set(fragment().directory());
                tx.show(pathBar);
            }
            else
            {
                tx.hide(pathBar);
            }
            tx.commit();
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
