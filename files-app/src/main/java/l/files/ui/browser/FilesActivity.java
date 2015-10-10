package l.files.ui.browser;

import android.app.ActionBar;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toolbar;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import l.files.R;
import l.files.common.app.BaseActivity;
import l.files.common.app.OptionsMenus;
import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.menu.AboutMenu;
import l.files.ui.menu.ActionBarDrawerToggleAction;
import l.files.ui.menu.GoBackOnHomePressedAction;
import l.files.ui.newtab.NewTabMenu;
import l.files.preview.Preview;

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
import static l.files.operations.ui.IOExceptions.message;
import static l.files.ui.UserDirs.DIR_HOME;

public final class FilesActivity extends BaseActivity implements
        OnBackStackChangedListener,
        OnItemSelectedListener,
        OnOpenFileListener {

    public static final String EXTRA_DIRECTORY = "directory";

    private DrawerLayout drawer;
    private DrawerListener drawerListener;

    private HierarchyAdapter hierarchy;
    private Toolbar toolbar;
    private Spinner title;

    public List<File> hierarchy() {
        return hierarchy.get();
    }

    public Spinner title() {
        return title;
    }

    public Toolbar toolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.files_activity);
        Preview.get(this).readCacheAsyncIfNeeded();

        toolbar = find(R.id.toolbar, this);
        hierarchy = new HierarchyAdapter();
        title = find(R.id.title, this);
        title.setAdapter(hierarchy);
        title.setOnItemSelectedListener(this);

        drawer = find(R.id.drawer_layout, this);
        drawerListener = new DrawerListener();

        drawer.setDrawerListener(drawerListener);

        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        setOptionsMenu(OptionsMenus.compose(
                new ActionBarDrawerToggleAction(drawer, getFragmentManager()),
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
            @Override
            public void run() {
                updateToolBar();
            }
        });
    }

    @Override
    public void onItemSelected(
            AdapterView<?> parent, View view, int position, long id) {
        File item = (File) parent.getAdapter().getItem(position);
        if (!Objects.equals(item, fragment().directory())) {
            onOpen(item);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onDestroy() {
        getFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    private File initialDirectory() {
        File dir = getIntent().getParcelableExtra(EXTRA_DIRECTORY);
        return dir == null ? DIR_HOME : dir;
    }

    @Override
    protected void onPause() {
        Preview.get(this).writeCacheAsyncIfNeeded();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (isSidebarOpen()) {
            closeSidebar();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        updateToolBar();
    }

    private void updateToolBar() {
        FilesFragment fragment = fragment();
        int backStacks = getFragmentManager().getBackStackEntryCount();
        if (backStacks == 0) {
            toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
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

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        drawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);

        if (isSidebarOpen()) {
            drawer.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
        } else {
            drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private boolean isSidebarOpen() {
        return drawer.isDrawerOpen(START);
    }

    private void closeSidebar() {
        drawer.closeDrawer(START);
    }

    public DrawerLayout drawerLayout() {
        return drawer;
    }

    @Override
    public void onOpen(File file) {
        onOpen(file, null);
    }

    @Override
    public void onOpen(final File file, @Nullable final Stat stat) {
        ActionMode mode = currentActionMode();
        if (mode != null) {
            mode.finish();
        }
        closeDrawerThenRun(new Runnable() {
            @Override
            public void run() {
                show(file, stat);
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

    private void show(final File file, @Nullable final Stat stat) {
        if (stat != null && !stat.isSymbolicLink()) {
            doShow(file, stat);
            return;
        }

        new AsyncTask<Void, Void, Object>() {

            @Override
            protected Object doInBackground(Void... params) {
                try {
                    return file.stat(FOLLOW);
                } catch (IOException e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                if (!isDestroyed() && !isFinishing()) {
                    if (result instanceof Stat) {
                        doShow(file, (Stat) result);
                    } else {
                        String msg = message((IOException) result);
                        makeText(FilesActivity.this, msg, LENGTH_SHORT).show();
                    }
                }
            }

        }.execute();
    }

    private void doShow(File file, Stat stat) {
        if (!isReadable(file)) { // TODO Check in background
            showPermissionDenied();
        } else if (stat.isDirectory()) {
            showDirectory(file);
        } else {
            showFile(file, stat);
        }
    }

    private boolean isReadable(File file) {
        try {
            return file.isReadable();
        } catch (IOException e) {
            return false;
        }
    }

    private void showPermissionDenied() {
        makeText(this, R.string.permission_denied, LENGTH_SHORT).show();
    }

    private void showDirectory(File file) {
        FilesFragment fragment = fragment();
        if (fragment.directory().equals(file)) {
            return;
        }
        FilesFragment f = FilesFragment.create(file);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, f, FilesFragment.TAG)
                .addToBackStack(null)
                .setBreadCrumbTitle(file.name())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void showFile(File file, Stat stat) {
        new OpenFile(this, file, stat).execute();
    }

    public FilesFragment fragment() {
        return (FilesFragment) getFragmentManager()
                .findFragmentByTag(FilesFragment.TAG);
    }

    private static class DrawerListener extends SimpleDrawerListener {

        Runnable mRunOnClosed;

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            if (mRunOnClosed != null) {
                mRunOnClosed.run();
                mRunOnClosed = null;
            }
        }
    }
}
