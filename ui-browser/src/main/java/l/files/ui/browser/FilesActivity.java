package l.files.ui.browser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import java.io.IOException;
import java.util.List;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.app.BaseActivity;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.browser.menu.ActionBarDrawerToggleMenu;
import l.files.ui.browser.menu.GoBackOnHomePressedMenu;
import l.files.ui.browser.menu.NewTabMenu;
import l.files.ui.preview.Preview;

import static android.content.ContentResolver.SCHEME_FILE;
import static android.graphics.Color.WHITE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.support.v4.view.GravityCompat.START;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;
import static l.files.ui.base.view.Views.find;

public final class FilesActivity extends BaseActivity implements
        OnBackStackChangedListener,
        OnItemSelectedListener,
        OnOpenFileListener {

    public static final String EXTRA_DIRECTORY = "directory";
    public static final String EXTRA_WATCH_LIMIT = "watch_limit";

    private DrawerLayout drawer;

    private HierarchyAdapter hierarchy;
    private Toolbar toolbar;
    private Spinner title;
    private DrawerArrowDrawable navigationIcon;

    public List<Path> hierarchy() {
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
        Preview.get(getApplicationContext()).readCacheAsyncIfNeeded();

        navigationIcon = new DrawerArrowDrawable(this);
        navigationIcon.setColor(WHITE);

        toolbar = find(R.id.toolbar, this);
        toolbar.setNavigationIcon(navigationIcon);

        hierarchy = new HierarchyAdapter();
        title = find(R.id.title, this);
        title.setAdapter(hierarchy);
        title.setOnItemSelectedListener(this);

        drawer = find(R.id.drawer_layout, this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        if (SDK_INT <= LOLLIPOP) {
            findViewById(R.id.toolbar_shadow).setVisibility(VISIBLE);
            drawer.setDrawerShadow(R.drawable.drawer_shadow, START);
        }

        setOptionsMenu(OptionsMenus.compose(
                new ActionBarDrawerToggleMenu(drawer, getSupportFragmentManager()),
                new GoBackOnHomePressedMenu(this),
                new NewTabMenu(this)
        ));

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (state == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.content,
                            FilesFragment.create(getInitialDirectory(), getWatchLimit()),
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
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(
            AdapterView<?> parent, View view, int position, long id) {
        Path item = (Path) parent.getAdapter().getItem(position);
        if (!item.equals(fragment().directory())) {
            onOpen(item);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private Path getInitialDirectory() {
        Path dir = getIntent().getParcelableExtra(EXTRA_DIRECTORY);
        if (dir == null
                && getIntent().getData() != null
                && getIntent().getData().getScheme() != null
                && getIntent().getData().getScheme().equals(SCHEME_FILE)) {
            dir = Path.of(getIntent().getData().getPath()); // TODO
        }
        return dir == null ? DIR_HOME : dir;
    }

    private int getWatchLimit() {
        return getIntent().getIntExtra(EXTRA_WATCH_LIMIT, -1);
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
        Path directory = fragment().directory();
        int backStacks = getSupportFragmentManager().getBackStackEntryCount();
        if (backStacks == 0) {
            navigationIcon.setProgress(0);
        } else {
            navigationIcon.setProgress(1);
        }
        hierarchy.set(directory);
        title.setSelection(hierarchy.indexOf(directory));
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) {
            while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        drawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        super.onSupportActionModeStarted(mode);

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

    public DrawerArrowDrawable navigationIcon() {
        return navigationIcon;
    }

    @Override
    public void onOpen(Path file) {
        onOpen(file, null);
    }

    @Override
    public void onOpen(final Path file, @Nullable final Stat stat) {
        ActionMode mode = currentActionMode();
        if (mode != null) {
            mode.finish();
        }

        if (drawer.isDrawerOpen(START)) {
            drawer.closeDrawers();
        }

        show(file, stat);
    }

    private void show(final Path path, @Nullable final Stat stat) {
        if (stat != null && !stat.isSymbolicLink()) {
            doShow(path, stat);
            return;
        }

        new AsyncTask<Void, Void, Object>() {

            @Override
            protected Object doInBackground(Void... params) {
                try {
                    return path.stat(FOLLOW);
                } catch (IOException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                if (!isFinishing()) {
                    if (result instanceof Stat) {
                        doShow(path, (Stat) result);
                    } else {
                        String msg = message((IOException) result);
                        makeText(FilesActivity.this, msg, LENGTH_SHORT).show();
                    }
                }
            }

        }.execute();
    }

    private void doShow(Path path, Stat stat) {
        if (!isReadable(path)) { // TODO Check in background
            showPermissionDenied();
        } else if (stat.isDirectory()) {
            showDirectory(path);
        } else {
            showFile(path, stat);
        }
    }

    private boolean isReadable(Path path) {
        try {
            return path.isReadable();
        } catch (IOException e) {
            return false;
        }
    }

    private void showPermissionDenied() {
        makeText(this, R.string.permission_denied, LENGTH_SHORT).show();
    }

    private void showDirectory(Path path) {
        FilesFragment fragment = fragment();
        if (fragment.directory().equals(path)) {
            return;
        }
        FilesFragment f = FilesFragment.create(path, getWatchLimit());
        getSupportFragmentManager()
                .beginTransaction()
                .setBreadCrumbTitle(String.valueOf(path.toAbsolutePath().getName().orObject(path)))
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, f, FilesFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void showFile(Path file, Stat stat) {
        new OpenFile(this, file, stat).execute();
    }

    public FilesFragment fragment() {
        return (FilesFragment) getSupportFragmentManager()
                .findFragmentByTag(FilesFragment.TAG);
    }
}
